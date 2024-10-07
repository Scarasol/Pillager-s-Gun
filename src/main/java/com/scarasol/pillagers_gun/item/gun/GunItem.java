package com.scarasol.pillagers_gun.item.gun;

import com.google.common.collect.Lists;
import com.scarasol.pillagers_gun.PillagersGunMod;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public abstract class GunItem extends ProjectileWeaponItem implements Vanishable {
    private static final float AMMO_POWER = 6F;
    private boolean startSoundPlayed = false;
    private final Predicate<ItemStack> AMMO = (itemStack -> itemStack.is(getAmmo()));

    public GunItem(Properties properties) {
        super(properties);
    }

    public abstract SoundEvent getFireSound();

    public abstract SoundEvent getReloadSound();

    public static int getChargeDuration(ItemStack itemStack) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack);
        if (itemStack.getItem() instanceof GunItem gunItem)
            return gunItem.getMaxChargeDuration() - 5 * i;
        return 0;
    }

    public UseAnim getUseAnimation(ItemStack p_40935_) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return AMMO;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return AMMO;
    }

    public abstract Item getAmmo();

    public abstract int getAmmoCount();


    public abstract int getMaxChargeDuration();

    public abstract int getCooldownTime();

    public abstract int getShotCount();


    @Override
    public int getUseDuration(ItemStack itemStack) {
        return getChargeDuration(itemStack) + 3;
    }

    @Override
    public boolean useOnRelease(ItemStack itemStack) {
        return itemStack.is(this);
    }

    private static float getShootingPower(ItemStack itemStack) {
        return AMMO_POWER;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (isCharged(itemstack)) {
            performShooting(level, player, interactionHand, itemstack, getShootingPower(itemstack), 1.0F);
            player.getCooldowns().addCooldown(itemstack.getItem(), getCooldownTime());
            int ammo = itemstack.getOrCreateTag().getInt("ammo");
            if (ammo == 0)
                setCharged(itemstack, false);
            return InteractionResultHolder.consume(itemstack);
        } else if (!player.getProjectile(itemstack).isEmpty()) {
            if (!isCharged(itemstack)) {
                this.startSoundPlayed = false;
                player.startUsingItem(interactionHand);
            }
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int p_40913_) {
        if (!level.isClientSide) {
            float f = (float) (itemStack.getUseDuration() - p_40913_) / (float) getChargeDuration(itemStack);
            if (f < 0.1F) {
                this.startSoundPlayed = false;
            }
            if (f == 0.1F && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), getReloadSound(), SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }

    }

    private static void addChargedProjectile(ItemStack gun, ItemStack ammo, int ammoCount) {
        if (gun.getItem() instanceof GunItem gunItem){
            CompoundTag compoundtag = gun.getOrCreateTag();
            ListTag listtag;
            if (compoundtag.contains("ChargedProjectiles", 9)) {
                listtag = compoundtag.getList("ChargedProjectiles", 10);
            } else {
                listtag = new ListTag();
            }
            if (ammo.is(Items.ARROW)) {
                ammo = new ItemStack(gunItem.getAmmo());
            }
            CompoundTag compoundtag1 = new CompoundTag();
            ammo.save(compoundtag1);
            listtag.add(compoundtag1);
            compoundtag.put("ChargedProjectiles", listtag);
            compoundtag.putInt("ammo", ammoCount);
        }

    }

    public static void init(ItemStack gun) {
        if (gun.getItem() instanceof GunItem gunItem){
            int i = new Random().nextInt(gunItem.getAmmoCount() + 1);
            if (i != 0) {
                ItemStack itemstack = new ItemStack(gunItem.getAmmo());
                addChargedProjectile(gun, itemstack, i);
                setCharged(gun, true);
            }
        }

    }

    public static int getCurrentAmmoCount(ItemStack itemStack) {
        CompoundTag compoundtag = itemStack.getTag();
        if (compoundtag != null) {
            return compoundtag.getInt("ammo");
        }
        return 0;
    }

    private boolean loadProjectile(LivingEntity livingEntity, ItemStack gun, ItemStack ammo, boolean p_40866_, boolean p_40867_) {
        if (ammo.isEmpty()) {
            return false;
        } else {
            ItemStack itemstack;
            if (!p_40867_ && !p_40866_) {
                itemstack = ammo.split(1);
                if (ammo.isEmpty() && livingEntity instanceof Player) {
                    ((Player) livingEntity).getInventory().removeItem(ammo);
                }
            } else {
                itemstack = ammo.copy();
            }
            if (gun.getItem() instanceof GunItem gunItem)
            addChargedProjectile(gun, itemstack, gunItem.getAmmoCount());
            return true;
        }
    }

    private boolean tryLoadProjectiles(LivingEntity livingEntity, ItemStack gun) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, gun);
        int j = i == 0 ? 0 : 2;
        int shotCount = getShotCount() + j;
        boolean flag = (!(livingEntity instanceof Player player)) || player.getAbilities().instabuild;
        ItemStack itemstack = livingEntity.getProjectile(gun);
        ItemStack itemstack1 = itemstack.copy();
        for (int k = 0; k < shotCount; ++k) {
            if (k > 0) {
                itemstack = itemstack1.copy();
            }
            if (itemstack.isEmpty() && flag) {
                itemstack = new ItemStack(getAmmo());
                itemstack1 = itemstack.copy();
            }
            if (!loadProjectile(livingEntity, gun, itemstack, k > 0, flag)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int useTime) {
        int i = this.getUseDuration(itemStack) - useTime;
        float f = getPowerForTime(i, itemStack);
        if (f >= 1.0F && !isCharged(itemStack) && tryLoadProjectiles(livingEntity, itemStack)) {
            setCharged(itemStack, true);
        }else {
            if (livingEntity instanceof ServerPlayer serverPlayer)
                serverPlayer.connection.send(new ClientboundStopSoundPacket(getReloadSound().getLocation(), SoundSource.PLAYERS));
        }

    }

    private float getPowerForTime(int lastTime, ItemStack itemStack) {
        float f = (float) lastTime / (float) getChargeDuration(itemStack);
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    public static void performShooting(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, float p_40892_, float p_40893_) {
        if (itemStack.getItem() instanceof GunItem gunItem){
            List<ItemStack> list = getChargedProjectiles(itemStack);
            float[] afloat = getShotPitches(livingEntity.getRandom());
            for (int i = 0; i < list.size(); ++i) {
                ItemStack itemstack = list.get(i);
                boolean flag = livingEntity instanceof Player && ((Player) livingEntity).getAbilities().instabuild;
                if (!itemstack.isEmpty()) {
                    if (i == 0) {
                        shootProjectile(level, livingEntity, interactionHand, itemStack, itemstack, flag, p_40892_, p_40893_, 0.0F);
                    } else {
                        if (level.getRandom().nextDouble() < 0.5){
                            shootProjectile(level, livingEntity, interactionHand, itemStack, itemstack, flag, p_40892_, p_40893_, new Random().nextFloat() * 5F);
                        }else {
                            shootProjectile(level, livingEntity, interactionHand, itemStack, itemstack, flag, p_40892_, p_40893_, new Random().nextFloat() * -5F);
                        }
                    }
                }

            }
            level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), gunItem.getFireSound(), SoundSource.PLAYERS, 0.6F, afloat[0]);
            onGunShot(level, livingEntity, itemStack);
        }
        
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack itemStack) {
        List<ItemStack> list = Lists.newArrayList();
        CompoundTag compoundtag = itemStack.getTag();
        if (compoundtag != null && compoundtag.contains("ChargedProjectiles", 9)) {
            ListTag listtag = compoundtag.getList("ChargedProjectiles", 10);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag1 = listtag.getCompound(i);
                list.add(ItemStack.of(compoundtag1));
            }
        }

        return list;
    }

    private static float getRandomShotPitch(boolean p_150798_, RandomSource p_150799_) {
        float f = p_150798_ ? 0.63F : 0.43F;
        return 1.0F / (p_150799_.nextFloat() * 0.5F + 1.8F) + f;
    }

    private static float[] getShotPitches(RandomSource p_40924_) {
        boolean flag = p_40924_.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(flag, p_40924_), getRandomShotPitch(!flag, p_40924_)};
    }

    private static void onGunShot(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            if (!level.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, itemStack);
            }

            serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        }
        int ammo = itemStack.getOrCreateTag().getInt("ammo");
        ammo -= 1;
        itemStack.getOrCreateTag().putInt("ammo", ammo);
        if (ammo == 0)
            clearChargedProjectiles(itemStack);
    }

    private static void clearChargedProjectiles(ItemStack itemStack) {
        CompoundTag compoundtag = itemStack.getTag();
        if (compoundtag != null) {
            ListTag listtag = compoundtag.getList("ChargedProjectiles", 9);
            listtag.clear();
            compoundtag.put("ChargedProjectiles", listtag);
        }

    }

    private static void shootProjectile(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack gun, ItemStack ammo, boolean p_40901_, float p_40902_, float p_40903_, float p_40904_) {
        if (!level.isClientSide) {
            AbstractArrow projectile = getAmmo(level, livingEntity, gun, ammo);
            if (p_40901_ || p_40904_ != 0.0F) {
                projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            Vec3 vec31 = livingEntity.getUpVector(1.0F);
            Quaternionf quaternionf = (new Quaternionf()).setAngleAxis((double)(p_40904_ * ((float)Math.PI / 180F)), vec31.x, vec31.y, vec31.z);
            Vec3 vec3 = livingEntity.getViewVector(1.0F);
            Vector3f vector3f = vec3.toVector3f().rotate(quaternionf);
            projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), p_40902_, p_40903_);
            gun.hurtAndBreak(1, livingEntity, (p_40858_) -> {
                p_40858_.broadcastBreakEvent(interactionHand);
            });
            level.addFreshEntity(projectile);
        }
    }

    private static AbstractArrow getAmmo(Level level, LivingEntity livingEntity, ItemStack gun, ItemStack ammo) {
        if (gun.getItem() instanceof GunItem gunItem){
            ArrowItem arrowItem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : gunItem.getAmmo());
            AbstractArrow abstractarrow = arrowItem.createArrow(level, ammo, livingEntity);
            abstractarrow.setShotFromCrossbow(true);
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, gun);
            if (i > 0) {
                abstractarrow.setPierceLevel((byte) i);
            }
            return abstractarrow;
        }
        return null;
    }

    public static boolean isCharged(ItemStack itemStack) {
        CompoundTag compoundtag = itemStack.getTag();
        return compoundtag != null && compoundtag.getBoolean("Charged");
    }

    public static void setCharged(ItemStack itemStack, boolean charged) {
        CompoundTag compoundtag = itemStack.getOrCreateTag();
        compoundtag.putBoolean("Charged", charged);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 8;
    }
}
