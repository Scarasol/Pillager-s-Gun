package com.scarasol.pillagers_gun.util;

import com.scarasol.pillagers_gun.compat.sbw.SbwCompat;
import com.scarasol.pillagers_gun.compat.tacz.TaczCompat;
import com.scarasol.pillagers_gun.compat.zombiekit.MobUseFlameThrower;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.event.EventHandler;
import com.scarasol.pillagers_gun.init.PillagersGunItems;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class GunnerEquipmentService {
    private static final String BORN_WITH_GUN_CHECKED_TAG = "pillagers_gun:born_with_gun_checked";
    private static final int BORN_WITH_GUN_INITIAL_DELAY = 2;
    private static final int BORN_WITH_GUN_RETRY_TICKS = 80;
    private static final Map<Mob, BornWithGunTask> BORN_WITH_GUN_TASKS = new WeakHashMap<>();

    private GunnerEquipmentService() {
    }

    public static void scheduleBornWithGun(Mob mob) {
        if (mob.level().isClientSide() || !isNewlySpawned(mob)) {
            return;
        }
        if (mob.getPersistentData().getBoolean(BORN_WITH_GUN_CHECKED_TAG) || BORN_WITH_GUN_TASKS.containsKey(mob)) {
            return;
        }

        mob.getPersistentData().putBoolean(BORN_WITH_GUN_CHECKED_TAG, true);
        if (!rollChance(mob, CommonConfig.EQUIP_CHANCE.get())) {
            return;
        }

        BORN_WITH_GUN_TASKS.put(mob, new BornWithGunTask(BORN_WITH_GUN_INITIAL_DELAY, BORN_WITH_GUN_RETRY_TICKS));
    }

    public static void tickBornWithGun(Mob mob) {
        if (mob.level().isClientSide()) {
            return;
        }
        BornWithGunTask task = BORN_WITH_GUN_TASKS.get(mob);
        if (task == null) {
            return;
        }

        if (task.delay > 0) {
            task.delay--;
            return;
        }
        if (task.remainingTicks <= 0 || !mob.isAlive()) {
            BORN_WITH_GUN_TASKS.remove(mob);
            return;
        }

        if (task.selectedStack.isEmpty()) {
            if (!tryEquip(mob)) {
                BORN_WITH_GUN_TASKS.remove(mob);
                return;
            }
            task.selectedStack = mob.getMainHandItem().copy();
        } else {
            restoreBornWithGunIfOverwritten(mob, task.selectedStack);
        }

        task.remainingTicks--;
        if (task.remainingTicks <= 0) {
            BORN_WITH_GUN_TASKS.remove(mob);
        }
    }

    private static void restoreBornWithGunIfOverwritten(Mob mob, ItemStack selectedStack) {
        if (selectedStack.isEmpty() || isSameSelectedWeapon(mob.getMainHandItem(), selectedStack)) {
            return;
        }
        mob.setDropChance(EquipmentSlot.MAINHAND, CommonConfig.DROP_CHANCE.get().floatValue());
        mob.setItemInHand(InteractionHand.MAIN_HAND, selectedStack.copy());
        applyFollowRangeForSelectedWeapon(mob, selectedStack);
    }

    private static boolean isSameSelectedWeapon(ItemStack currentStack, ItemStack selectedStack) {
        if (currentStack.isEmpty() || selectedStack.isEmpty() || !currentStack.is(selectedStack.getItem())) {
            return false;
        }
        if (ModList.get().isLoaded("tacz") && isTaczStack(selectedStack)) {
            return TaczCompat.isSameGun(currentStack, selectedStack);
        }
        return true;
    }

    private static boolean isTaczStack(ItemStack itemStack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        return itemId != null && "tacz".equals(itemId.getNamespace());
    }

    private static boolean tryEquip(Mob mob) {
        if (tryEquipCompatGun(mob)) {
            return true;
        }
        Optional<WeaponType> weaponType = WeightedRandom.pick(mob.getRandom(), getVanillaWeaponWeights());
        return weaponType.map(type -> equipWeapon(mob, type)).orElse(false);
    }

    private static boolean tryEquipCompatGun(Mob mob) {
        boolean canTryTacz = canTryTaczSpawn();
        boolean canTrySbw = canTrySbwSpawn();
        if (canTryTacz && canTrySbw) {
            if (mob.getRandom().nextBoolean()) {
                return TaczCompat.spawnWithTaczGun(mob) || SbwCompat.spawnWithSbwGun(mob);
            }
            return SbwCompat.spawnWithSbwGun(mob) || TaczCompat.spawnWithTaczGun(mob);
        }
        return canTryTacz && TaczCompat.spawnWithTaczGun(mob)
                || canTrySbw && SbwCompat.spawnWithSbwGun(mob);
    }

    private static boolean canTryTaczSpawn() {
        return ModList.get().isLoaded("tacz")
                && CommonConfig.TACZ_GUN_USE.get()
                && CommonConfig.TACZ_GUN_SPAWN.get();
    }

    private static boolean canTrySbwSpawn() {
        return ModList.get().isLoaded("superbwarfare")
                && CommonConfig.SBW_GUN_USE.get()
                && CommonConfig.SBW_GUN_SPAWN.get();
    }

    private static boolean equipWeapon(Mob mob, WeaponType type) {
        mob.setDropChance(EquipmentSlot.MAINHAND, CommonConfig.DROP_CHANCE.get().floatValue());
        if (type == WeaponType.FLAMETHROWER) {
            MobUseFlameThrower.makeMobsUseFlameThrower(mob);
            return true;
        }

        ItemStack itemStack = new ItemStack(type.item().get());
        GunItem.init(itemStack);
        itemStack.setCount(1);
        mob.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        applyFollowRangeForSelectedWeapon(mob, itemStack);
        return true;
    }

    private static List<WeightedRandom.Entry<WeaponType>> getVanillaWeaponWeights() {
        List<WeightedRandom.Entry<WeaponType>> entries = new ArrayList<>();
        entries.add(WeightedRandom.entry(WeaponType.PISTOL, CommonConfig.PISTOL_CHANCE.get()));
        entries.add(WeightedRandom.entry(WeaponType.ASSAULT_RIFLE, CommonConfig.ASSAULT_CHANCE.get()));
        entries.add(WeightedRandom.entry(WeaponType.SHOTGUN, CommonConfig.SHOTGUN_CHANCE.get()));
        entries.add(WeightedRandom.entry(WeaponType.SNIPERS_RIFLE, CommonConfig.SNIPERS_RIFLE_CHANCE.get()));
        entries.add(WeightedRandom.entry(WeaponType.BAZOOKA, CommonConfig.BAZOOKA_CHANCE.get()));
        if (canUseModernZombieKit()) {
            entries.add(WeightedRandom.entry(WeaponType.FLAMETHROWER, CommonConfig.FLAMETHROWER_CHANCE.get()));
        }
        return entries;
    }

    public static boolean hasScheduledBornWithGun(Mob mob) {
        return BORN_WITH_GUN_TASKS.containsKey(mob);
    }

    private static boolean isNewlySpawned(Mob mob) {
        return mob.tickCount <= 1;
    }

    private static boolean rollChance(Mob mob, double chance) {
        return chance >= 1 || chance > 0 && mob.getRandom().nextDouble() < chance;
    }

    private static boolean canUseModernZombieKit() {
        Optional<? extends ModContainer> optional = ModList.get().getModContainerById("zombiekit");
        if (optional.isEmpty()) {
            return false;
        }
        ArtifactVersion version = optional.get().getModInfo().getVersion();
        return version.getMajorVersion() > 2 || version.getMajorVersion() == 2 && version.getMinorVersion() >= 1;
    }

    public static void applyVanillaSniperFollowRange(LivingEntity entity, ItemStack itemStack) {
        if (!itemStack.is(PillagersGunItems.SNIPERS_RIFLE.get())) {
            return;
        }
        applySniperFollowRange(entity);
    }

    public static void applySniperFollowRange(LivingEntity entity) {
        AttributeInstance attributeInstance = entity.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
        if (attributeInstance == null) {
            return;
        }
        attributeInstance.removeModifier(EventHandler.ATTRIBUTE_MODIFIER_UUID);
        AttributeModifier attributeModifier = new AttributeModifier(EventHandler.ATTRIBUTE_MODIFIER_UUID, "sniper", CommonConfig.SNIPERS_RIFLE_BONUS.get(), AttributeModifier.Operation.MULTIPLY_BASE);
        attributeInstance.addPermanentModifier(attributeModifier);
    }

    public static void clearFollowRangeModifier(LivingEntity entity) {
        AttributeInstance attributeInstance = entity.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(EventHandler.ATTRIBUTE_MODIFIER_UUID);
        }
    }

    private static void applyFollowRangeForSelectedWeapon(LivingEntity entity, ItemStack itemStack) {
        if (itemStack.is(PillagersGunItems.SNIPERS_RIFLE.get())) {
            applySniperFollowRange(entity);
        } else if (ModList.get().isLoaded("tacz")
                && CommonConfig.TACZ_GUN_USE.get()
                && isTaczStack(itemStack)) {
            TaczCompat.zoomAttributeModifier(itemStack, entity);
        } else if (ModList.get().isLoaded("superbwarfare")
                && CommonConfig.SBW_GUN_USE.get()
                && SbwCompat.isSbwGun(itemStack)) {
            SbwCompat.zoomAttributeModifier(itemStack, entity);
        }
    }

    private enum WeaponType {
        PISTOL(PillagersGunItems.PISTOL),
        ASSAULT_RIFLE(PillagersGunItems.ASSAULT_RIFLE),
        SHOTGUN(PillagersGunItems.SHOTGUN),
        SNIPERS_RIFLE(PillagersGunItems.SNIPERS_RIFLE),
        BAZOOKA(PillagersGunItems.BAZOOKA),
        FLAMETHROWER(null);

        private final RegistryObject<Item> item;

        WeaponType(RegistryObject<Item> item) {
            this.item = item;
        }

        private RegistryObject<Item> item() {
            return item;
        }
    }

    private static final class BornWithGunTask {
        private int delay;
        private int remainingTicks;
        private ItemStack selectedStack = ItemStack.EMPTY;

        private BornWithGunTask(int delay, int remainingTicks) {
            this.delay = delay;
            this.remainingTicks = remainingTicks;
        }
    }
}
