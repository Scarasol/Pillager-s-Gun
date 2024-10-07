package com.scarasol.pillagers_gun.entity.projectile;

import com.scarasol.pillagers_gun.compat.guardvillagers.GuardUseGun;
import com.scarasol.pillagers_gun.compat.recruits.RecruitUseGun;
import com.scarasol.pillagers_gun.config.CommonConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.network.protocol.Packet;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.monster.Vex;
import net.minecraftforge.fml.ModList;

public abstract class Ammo extends AbstractArrow {

    private int life = 0;

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
    public Packet<?> getAddEntityPacket() {
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
        if (this.inGround || this.isInWaterOrBubble())
            this.discard();
        if (++life > 200)
            this.discard();
    }

    public boolean checkFriendlyFire(Entity target, Entity owner) {
        if (CommonConfig.FRIEND_FIRE.get())
            return false;
        if (owner instanceof Raider)
            return  (target instanceof Raider || target instanceof Vex);
        if (ModList.get().isLoaded("guardvillagers"))
            return GuardUseGun.checkFriendlyFire(target, owner);
        if (ModList.get().isLoaded("recruits"))
            return RecruitUseGun.checkFriendlyFire(target, owner);
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        BlockState blockState = this.level.getBlockState(blockHitResult.getBlockPos());
        if(blockState.is(BlockTags.create(new ResourceLocation("forge:glass"))) || blockState.is(BlockTags.create(new ResourceLocation("forge:glass_panes")))){
            this.level.destroyBlock(blockHitResult.getBlockPos(), false, this);
        }else {
            this.playSound(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("pillagers_gun:bullet_hit_ground")), 0.1f, 1.0f);
        }
    }
}
