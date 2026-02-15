package com.scarasol.pillagers_gun.entity.projectile;

import com.google.common.collect.Lists;
import com.scarasol.pillagers_gun.PillagersGunMod;
import com.scarasol.pillagers_gun.compat.guardvillagers.GuardUseGun;
import com.scarasol.pillagers_gun.compat.recruits.RecruitUseGun;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.init.PillagersGunSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.network.protocol.Packet;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.monster.Vex;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Ammo extends AbstractArrow {

    private int life = 0;
    public static final List<TagKey<EntityType<?>>> FRIENDLY_TAG = Lists.newArrayList();

    public static final TagKey<Block> GLASS = BlockTags.create(new ResourceLocation("forge:glass"));
    public static final TagKey<Block> GLASS_PANES = BlockTags.create(new ResourceLocation("forge:glass_panes"));

    protected Ammo(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.setSilent(true);
        this.setCritArrow(false);
        this.setBaseDamage(0);
        this.setNoGravity(true);
    }

    protected Ammo(EntityType<? extends AbstractArrow> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
        this.setSilent(true);
        this.setCritArrow(false);
        this.setBaseDamage(0);
        this.setNoGravity(true);
    }

    protected Ammo(EntityType<? extends AbstractArrow> entityType, LivingEntity livingEntity, Level level) {
        super(entityType, livingEntity, level);
        this.setSilent(true);
        this.setCritArrow(false);
        this.setBaseDamage(0);
        this.setNoGravity(true);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return (this.getOwner() != null && !checkFriendlyFire(target, getOwner())) && super.canHitEntity(target);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        entity.setArrowCount(entity.getArrowCount() - 1);
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        if (this.inGround || this.isInWaterOrBubble()) {
            this.discard();
        }
        if (++life > 80) {
            this.discard();
        }
    }

    public static boolean checkFriendlyFire(@NotNull Entity target, @NotNull Entity owner) {
        if (CommonConfig.FRIEND_FIRE.get() || owner instanceof Player) {
            return false;
        }
        if (owner.getType() == target.getType() || owner.isAlliedTo(target)) {
            return true;
        }
        if (FRIENDLY_TAG.isEmpty()) {
            for (String tag : CommonConfig.TAG_FRIENDLY_FIRE.get()) {
                FRIENDLY_TAG.add(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(tag)));
            }
        }
        for (TagKey<EntityType<?>> tag : FRIENDLY_TAG) {
            if (owner.getType().is(tag) && target.getType().is(tag)) {
                return true;
            }
        }
        if (owner instanceof Raider) {
            return (target instanceof Raider || target instanceof Vex);
        }
        if (ForgeRegistries.ENTITY_TYPES.getKey(owner.getType()).toString().contains("guardvillagers")) {
            return GuardUseGun.checkFriendlyFire(target, owner);
        }
        if (ForgeRegistries.ENTITY_TYPES.getKey(owner.getType()).toString().contains("recruits")) {
            return RecruitUseGun.checkFriendlyFire(target, owner);
        }

        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        BlockState blockState = this.level().getBlockState(blockHitResult.getBlockPos());
        if (CommonConfig.BREAK_GLASS.get() && (blockState.is(GLASS) || blockState.is(GLASS_PANES))) {
            this.level().destroyBlock(blockHitResult.getBlockPos(), false, this);
        } else {
            this.playSound(PillagersGunSounds.bullet_hit_ground.get(), 1, 1.0f);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (!level().isClientSide) {
            entityHitResult.getEntity().getPersistentData().putBoolean("ShootByGun", true);
        }
    }
}
