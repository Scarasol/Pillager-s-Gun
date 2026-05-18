package com.scarasol.pillagers_gun.compat.sbw;

import com.atsuishio.superbwarfare.data.gun.FireMode;
import com.atsuishio.superbwarfare.data.gun.FireModeInfo;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.scarasol.pillagers_gun.entity.goal.controller.DynamicFireRate;
import com.scarasol.pillagers_gun.entity.goal.controller.GunAimUtil;
import com.scarasol.pillagers_gun.entity.goal.controller.GunController;
import com.scarasol.pillagers_gun.entity.goal.controller.GunRole;
import com.scarasol.pillagers_gun.entity.goal.controller.GunReloadResult;
import com.scarasol.pillagers_gun.entity.goal.controller.GunShotResult;
import com.scarasol.pillagers_gun.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class SbwGunController implements GunController {
    private final Mob mob;
    private double attackCount;
    private int lastTick = -1;

    public SbwGunController(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean isValid() {
        return this.mob.getMainHandItem().getItem() instanceof GunItem;
    }

    @Override
    public void start() {
        this.attackCount = 0;
        tickGunData();
    }

    @Override
    public void stop() {
        this.attackCount = 0;
        GunData data = getGunData();
        if (data != null) {
            data.resetStatus();
            data.zooming.set(false);
        }
        setChargingCrossbow(false);
    }

    @Override
    public boolean hasAmmo() {
        GunData data = tickGunData();
        return data != null && data.hasEnoughAmmoToShoot(this.mob);
    }

    @Override
    public boolean canReload() {
        GunData data = tickGunData();
        return data != null && !data.useBackpackAmmo() && data.hasBackupAmmo(this.mob);
    }

    @Override
    public int getAmmoCount() {
        GunData data = tickGunData();
        if (data == null) {
            return 0;
        }
        int ammoCount = data.currentAvailableShots(this.mob);
        return ammoCount == Integer.MAX_VALUE ? -1 : ammoCount;
    }

    @Override
    public int getMaxAmmoCount() {
        GunData data = tickGunData();
        if (data == null || data.useBackpackAmmo()) {
            return -1;
        }
        return Math.max(0, getInt(data, GunProp.MAGAZINE));
    }

    @Override
    public GunRole getRole() {
        String gunType = SbwCompat.getGunType(this.mob.getMainHandItem()).toLowerCase(Locale.ROOT);
        return switch (gunType) {
            case "rifle", "smg", "machinegun" -> GunRole.RIFLE;
            case "shotgun" -> GunRole.SHOTGUN;
            case "handgun", "pistol" -> GunRole.PISTOL;
            case "sniper" -> GunRole.SNIPER;
            case "directlauncher", "curvedlauncher", "launcher" -> GunRole.EXPLOSIVE;
            default -> GunRole.OTHER;
        };
    }

    @Override
    public double getAmmoUsePerTick(double distanceToTarget) {
        GunData data = tickGunData();
        if (data == null) {
            return GunController.super.getAmmoUsePerTick(distanceToTarget);
        }
        FireMode fireMode = getFireMode(data);
        int rpm = Math.max(1, getInt(data, GunProp.RPM));
        return DynamicFireRate.getStep(fireMode == FireMode.AUTO, rpm, distanceToTarget, false);
    }

    @Override
    public int getReloadDurationTicks() {
        GunData data = tickGunData();
        if (data == null) {
            return GunController.super.getReloadDurationTicks();
        }
        int reloadMillis = getInt(data, getAmmoCount(data) > 0 ? GunProp.NORMAL_RELOAD_TIME : GunProp.EMPTY_RELOAD_TIME);
        return Math.max(1, Math.round(reloadMillis / 50.0F));
    }

    @Override
    public int getReadyDelayAfterAmmoFound() {
        return 0;
    }

    @Override
    public int getReadyDelayAfterReload() {
        return 10 + this.mob.getRandom().nextInt(20);
    }

    @Override
    public void startReload() {
        GunData data = tickGunData();
        if (data == null) {
            return;
        }
        data.startReload();
        setChargingCrossbow(true);
    }

    @Override
    public GunReloadResult tickReloading() {
        GunData data = tickGunData();
        if (data == null) {
            setChargingCrossbow(false);
            return GunReloadResult.FAILED;
        }

        if (data.reloading()) {
            return GunReloadResult.RELOADING;
        }

        setChargingCrossbow(false);
        return data.hasEnoughAmmoToShoot(this.mob) ? GunReloadResult.RELOADED : GunReloadResult.FAILED;
    }

    @Override
    public void startAiming(LivingEntity target) {
        GunData data = tickGunData();
        if (data != null) {
            data.zooming.set(true);
        }
    }

    @Override
    public void startAiming(Vec3 targetPosition) {
        GunData data = tickGunData();
        if (data != null) {
            data.zooming.set(true);
        }
    }

    @Override
    public void stopAiming() {
        GunData data = tickGunData();
        if (data != null) {
            data.zooming.set(false);
        }
    }

    @Override
    public GunShotResult shoot(LivingEntity target, boolean isStunned, Vec3 lastTargetPosition, int ammoCount) {
        if (target == null) {
            return GunShotResult.noShot(ammoCount);
        }
        return shootAtPosition(target.getEyePosition(), target.distanceTo(this.mob), target, target.getUUID(), isStunned, lastTargetPosition, ammoCount);
    }

    @Override
    public GunShotResult shootAt(Vec3 targetPosition, boolean isStunned, Vec3 lastTargetPosition, int ammoCount) {
        return shootAtPosition(targetPosition, targetPosition.distanceTo(this.mob.getEyePosition()), null, null, isStunned, lastTargetPosition, ammoCount);
    }

    private GunShotResult shootAtPosition(Vec3 targetPosition, double distance, LivingEntity target, java.util.UUID targetEntityUUID, boolean isStunned, Vec3 lastTargetPosition, int ammoCount) {
        GunData data = tickGunData();
        if (data == null) {
            return GunShotResult.noShot(ammoCount);
        }

        FireMode fireMode = getFireMode(data);
        boolean automatic = fireMode == FireMode.AUTO;
        int rpm = Math.max(1, getInt(data, GunProp.RPM));
        this.attackCount += automatic
                ? DynamicFireRate.getStep(true, rpm, distance, isStunned)
                : DynamicFireRate.getSemiAutoStep(rpm, distance, isStunned);

        int currentAmmo = ammoCount;
        while (this.attackCount >= 1) {
            if (!data.canShoot(this.mob)) {
                return data.hasEnoughAmmoToShoot(this.mob) ? GunShotResult.ready(currentAmmo) : GunShotResult.depleted();
            }

            this.attackCount -= 1;
            double baseSpread = SbwCompat.getInaccuracy(SbwCompat.getGunType(this.mob.getMainHandItem()), getDouble(data, GunProp.SPREAD));
            double spread = EventFactory.getModifiedInaccuracy((float) baseSpread, this.mob, target, lastTargetPosition);
            if (isStunned) {
                spread += 8;
            }
            shootAtPosition(data, targetPosition, targetEntityUUID, spread, !isStunned);
            if (data.shouldStartBolt()) {
                data.startBolt();
            }

            currentAmmo = getAmmoCount(data);
            if (currentAmmo == 0) {
                this.attackCount = 0;
                stopAiming();
                return GunShotResult.depleted();
            }
            if (fireMode == FireMode.BURST && data.burstAmount.get() == 0) {
                this.attackCount = 0;
                return GunShotResult.cooldown(currentAmmo, getBurstCooldown(data));
            }
        }

        return GunShotResult.ready(currentAmmo);
    }

    private void shootAtPosition(GunData data, Vec3 targetPosition, java.util.UUID targetEntityUUID, double spread, boolean zoom) {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 shootPosition = new Vec3(this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        Vec3 shootDirection = targetPosition.subtract(shootPosition);
        if (shootDirection.lengthSqr() < 1.0E-6D) {
            shootDirection = this.mob.getLookAngle();
        } else {
            shootDirection = shootDirection.normalize();
        }
        GunAimUtil.lookAt(this.mob, targetPosition);
        data.shoot(new ShootParameters(
                this.mob,
                this.mob,
                serverLevel,
                shootPosition,
                shootDirection,
                data,
                spread,
                zoom,
                targetEntityUUID,
                targetPosition
        ));
    }

    private GunData tickGunData() {
        GunData data = getGunData();
        if (data == null) {
            return null;
        }
        if (this.lastTick != this.mob.tickCount) {
            this.lastTick = this.mob.tickCount;
            data.tick(this.mob, true);
            if (data.shouldStartBolt()) {
                data.startBolt();
            }
        }
        return data;
    }

    private GunData getGunData() {
        ItemStack itemStack = this.mob.getMainHandItem();
        return itemStack.getItem() instanceof GunItem ? GunData.from(itemStack) : null;
    }

    private int getAmmoCount(GunData data) {
        int ammoCount = data.currentAvailableShots(this.mob);
        return ammoCount == Integer.MAX_VALUE ? -1 : ammoCount;
    }

    private int getInt(GunData data, GunProp<?, Integer> prop) {
        return data.get(prop);
    }

    private double getDouble(GunData data, GunProp<?, Double> prop) {
        return data.get(prop);
    }

    private FireMode getFireMode(GunData data) {
        FireModeInfo fireModeInfo = data.selectedFireModeInfo();
        return fireModeInfo == null || fireModeInfo.mode == null ? FireMode.SEMI : fireModeInfo.mode;
    }

    private int getBurstCooldown(GunData data) {
        return Math.max(1, Math.round(getInt(data, GunProp.BURST_COOLDOWN) / 50.0F));
    }

    private void setChargingCrossbow(boolean charging) {
        if (this.mob instanceof CrossbowAttackMob crossbowAttackMob) {
            crossbowAttackMob.setChargingCrossbow(charging);
        }
    }
}
