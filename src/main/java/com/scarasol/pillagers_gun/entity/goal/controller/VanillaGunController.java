package com.scarasol.pillagers_gun.entity.goal.controller;

import com.scarasol.pillagers_gun.event.EventFactory;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class VanillaGunController implements GunController {
    private final Mob mob;
    private double attackCount;

    public VanillaGunController(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean isValid() {
        return this.mob.getMainHandItem().getItem() instanceof GunItem;
    }

    @Override
    public void start() {
        this.attackCount = 0;
    }

    @Override
    public void stop() {
        this.attackCount = 0;
        if (this.mob.isUsingItem()) {
            ItemStack useItem = this.mob.getUseItem();
            this.mob.stopUsingItem();
            GunItem.setCharged(useItem, false);
        }
        setChargingCrossbow(false);
    }

    @Override
    public boolean hasAmmo() {
        return GunItem.isCharged(this.mob.getMainHandItem());
    }

    @Override
    public boolean canReload() {
        return isValid();
    }

    @Override
    public int getAmmoCount() {
        return GunItem.getCurrentAmmoCount(this.mob.getMainHandItem());
    }

    @Override
    public int getReadyDelayAfterAmmoFound() {
        return 20 + this.mob.getRandom().nextInt(20);
    }

    @Override
    public int getReadyDelayAfterReload() {
        ItemStack itemStack = this.mob.getMainHandItem();
        if (itemStack.getItem() instanceof GunItem gun) {
            return 10 + this.mob.getRandom().nextInt(20) + gun.getCooldownTime();
        }
        return 10 + this.mob.getRandom().nextInt(20);
    }

    @Override
    public void startReload() {
        if (!isValid()) {
            return;
        }
        this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof GunItem));
        setChargingCrossbow(true);
    }

    @Override
    public GunReloadResult tickReloading() {
        if (!isValid() || !this.mob.isUsingItem()) {
            setChargingCrossbow(false);
            return GunReloadResult.FAILED;
        }

        ItemStack itemStack = this.mob.getUseItem();
        if (itemStack.getItem() instanceof GunItem && this.mob.getTicksUsingItem() >= GunItem.getChargeDuration(itemStack)) {
            this.mob.releaseUsingItem();
            setChargingCrossbow(false);
            return hasAmmo() ? GunReloadResult.RELOADED : GunReloadResult.FAILED;
        }

        return GunReloadResult.RELOADING;
    }

    @Override
    public GunShotResult shoot(LivingEntity target, boolean isStunned, Vec3 lastTargetPosition, int ammoCount) {
        InteractionHand hand = ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof GunItem);
        ItemStack itemStack = this.mob.getItemInHand(hand);
        if (!(itemStack.getItem() instanceof GunItem gunItem)) {
            return GunShotResult.noShot(ammoCount);
        }

        boolean automatic = isAutomatic(gunItem);
        if (!automatic) {
            double rpm = DynamicFireRate.cooldownTicksToRpm(gunItem.getCooldownTime());
            this.attackCount += DynamicFireRate.getSemiAutoStep(rpm, target.distanceTo(this.mob), isStunned);
            if (this.attackCount < 1) {
                return GunShotResult.ready(ammoCount);
            }
            this.attackCount -= 1;
        }

        float inaccuracy = EventFactory.getModifiedInaccuracy(gunItem.getInaccuracy(target), this.mob, target, lastTargetPosition);
        if (isStunned) {
            inaccuracy += 8;
        }
        GunItem.performShooting(this.mob.level(), this.mob, hand, itemStack, inaccuracy);

        int remainingAmmo = ammoCount - 1;
        ItemStack currentGun = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof GunItem));
        if (remainingAmmo <= 0) {
            GunItem.setCharged(currentGun, false);
            this.attackCount = 0;
            return GunShotResult.depleted();
        }
        if (currentGun.getItem() instanceof GunItem gun) {
            return automatic ? GunShotResult.cooldown(remainingAmmo, gun.getCooldownTime()) : GunShotResult.ready(remainingAmmo);
        }
        return automatic ? GunShotResult.cooldown(remainingAmmo, 0) : GunShotResult.ready(remainingAmmo);
    }

    private boolean isAutomatic(GunItem gunItem) {
        return gunItem.getCooldownTime() <= 2 && gunItem.getShotCount() == 1;
    }

    private void setChargingCrossbow(boolean charging) {
        if (this.mob instanceof CrossbowAttackMob crossbowAttackMob) {
            crossbowAttackMob.setChargingCrossbow(charging);
        }
    }
}
