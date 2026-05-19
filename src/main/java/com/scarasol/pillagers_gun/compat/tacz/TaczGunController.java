package com.scarasol.pillagers_gun.compat.tacz;

import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.entity.goal.controller.DynamicFireRate;
import com.scarasol.pillagers_gun.entity.goal.controller.GunAimUtil;
import com.scarasol.pillagers_gun.entity.goal.controller.GunController;
import com.scarasol.pillagers_gun.entity.goal.controller.GunRole;
import com.scarasol.pillagers_gun.entity.goal.controller.GunReloadResult;
import com.scarasol.pillagers_gun.entity.goal.controller.GunShotResult;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunReloadData;
import com.tacz.guns.resource.pojo.data.gun.GunReloadTime;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Map;

public class TaczGunController implements GunController {
    private final Mob mob;
    private double attackCount;

    public TaczGunController(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean isValid() {
        return getGun() != null;
    }

    @Override
    public void start() {
        this.attackCount = 0;
        if (isValid()) {
            IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.mob);
            gunOperator.draw(this.mob::getMainHandItem);
            gunOperator.getDataHolder().drawTimestamp = System.currentTimeMillis() - 10000;
        }
    }

    @Override
    public void stop() {
        this.attackCount = 0;
        if (!isValid()) {
            return;
        }

        IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.mob);
        gunOperator.cancelReload();
        gunOperator.aim(false);

        GunData gunData = getGunData();
        if (gunData != null && gunData.getBolt() == Bolt.MANUAL_ACTION) {
            gunOperator.bolt();
        }
        setChargingCrossbow(false);
    }

    @Override
    public boolean hasAmmo() {
        return getAmmoCount() != 0;
    }

    @Override
    public boolean canReload() {
        if (!CommonConfig.TACZ_GUNNERS_NEED_AMMO.get()) {
            return true;
        }
        IGun gun = getGun();
        if (gun instanceof AbstractGunItem gunItem) {
            ItemStack itemStack = this.mob.getItemInHand(InteractionHand.MAIN_HAND);
            if (gunItem.useDummyAmmo(itemStack) && gunItem.getDummyAmmoAmount(itemStack) == 0) {
                itemStack.getOrCreateTag().remove("DummyAmmo");
            }
            return gunItem.canReload(this.mob, itemStack);
        }
        return false;
    }

    @Override
    public int getAmmoCount() {
        IGun gun = getGun();
        ItemStack itemStack = this.mob.getMainHandItem();
        if (gun != null) {
            GunData gunData = getGunData(gun, itemStack);
            if (gunData != null) {
                return gun.useInventoryAmmo(itemStack) ? -1 : gun.getCurrentAmmoCount(itemStack) + (gun.hasBulletInBarrel(itemStack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
            }
        }
        return 0;
    }

    @Override
    public int getMaxAmmoCount() {
        IGun gun = getGun();
        ItemStack itemStack = this.mob.getMainHandItem();
        if (gun == null || gun.useInventoryAmmo(itemStack)) {
            return -1;
        }
        GunData gunData = getGunData(gun, itemStack);
        if (gunData == null) {
            return 0;
        }
        int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(itemStack, gunData);
        if (maxAmmoCount <= 0) {
            return 0;
        }
        return maxAmmoCount + (gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
    }

    @Override
    public GunRole getRole() {
        String gunType = TaczCompat.getGunType(this.mob.getMainHandItem()).toLowerCase(Locale.ROOT);
        return switch (gunType) {
            case "rifle", "smg", "mg" -> GunRole.RIFLE;
            case "shotgun" -> GunRole.SHOTGUN;
            case "pistol" -> GunRole.PISTOL;
            case "sniper" -> GunRole.SNIPER;
            case "rpg", "launcher" -> GunRole.EXPLOSIVE;
            default -> GunRole.OTHER;
        };
    }

    @Override
    public double getAmmoUsePerTick(double distanceToTarget) {
        IGun gun = getGun();
        ItemStack itemStack = this.mob.getMainHandItem();
        if (gun == null) {
            return GunController.super.getAmmoUsePerTick(distanceToTarget);
        }
        GunData gunData = getGunData(gun, itemStack);
        if (gunData == null) {
            return GunController.super.getAmmoUsePerTick(distanceToTarget);
        }
        return getFireRateStep(gun, itemStack, gunData, distanceToTarget, false);
    }

    @Override
    public int getReloadDurationTicks() {
        GunData gunData = getGunData();
        if (gunData == null) {
            return GunController.super.getReloadDurationTicks();
        }
        int scriptedReloadTicks = estimateScriptedReloadTicks(gunData);
        if (scriptedReloadTicks > 0) {
            return scriptedReloadTicks;
        }
        GunReloadData reloadData = gunData.getReloadData();
        if (reloadData == null) {
            return GunController.super.getReloadDurationTicks();
        }
        GunReloadTime reloadTime = reloadData.getCooldown();
        if (reloadTime == null) {
            reloadTime = reloadData.getFeed();
        }
        if (reloadTime == null) {
            return GunController.super.getReloadDurationTicks();
        }
        float seconds = hasAmmo() ? reloadTime.getTacticalTime() : reloadTime.getEmptyTime();
        return Math.max(1, Math.round(seconds * 20.0F));
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
        if (!isValid()) {
            return;
        }
        IGunOperator.fromLivingEntity(this.mob).reload();
        setChargingCrossbow(true);
    }

    @Override
    public GunReloadResult tickReloading() {
        if (!isValid()) {
            setChargingCrossbow(false);
            return GunReloadResult.FAILED;
        }

        IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.mob);
        if (gunOperator.getDataHolder().reloadStateType.isReloading()) {
            return GunReloadResult.RELOADING;
        }

        setChargingCrossbow(false);
        return hasAmmo() ? GunReloadResult.RELOADED : GunReloadResult.FAILED;
    }

    @Override
    public boolean isAttackBlocked() {
        IGun gun = getGun();
        return gun != null && gun.isOverheatLocked(this.mob.getMainHandItem());
    }

    @Override
    public boolean canMeleeAttack(LivingEntity target) {
        if (!isValid() || target == null) {
            return false;
        }
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.mob);
        return System.currentTimeMillis() - gunOperator.getDataHolder().meleeTimestamp > 3000 && getAttackReachSqr(target) >= this.mob.distanceToSqr(target);
    }

    @Override
    public void meleeAttack(LivingEntity target) {
        IGunOperator.fromLivingEntity(this.mob).melee();
    }

    @Override
    public void startAiming(LivingEntity target) {
        if (isValid()) {
            IGunOperator.fromLivingEntity(this.mob).aim(true);
        }
    }

    @Override
    public void startAiming(Vec3 targetPosition) {
        if (isValid()) {
            IGunOperator.fromLivingEntity(this.mob).aim(true);
        }
    }

    @Override
    public void stopAiming() {
        if (isValid()) {
            IGunOperator.fromLivingEntity(this.mob).aim(false);
        }
    }

    @Override
    public GunShotResult shoot(LivingEntity target, boolean isStunned, Vec3 lastTargetPosition, int ammoCount) {
        if (target == null) {
            return GunShotResult.noShot(ammoCount);
        }
        return shootAtPosition(target.getEyePosition(), target.distanceTo(this.mob), isStunned, ammoCount);
    }

    @Override
    public GunShotResult shootAt(Vec3 targetPosition, boolean isStunned, Vec3 lastTargetPosition, int ammoCount) {
        return shootAtPosition(targetPosition, targetPosition.distanceTo(this.mob.getEyePosition()), isStunned, ammoCount);
    }

    private GunShotResult shootAtPosition(Vec3 targetPosition, double distance, boolean isStunned, int ammoCount) {
        IGun gun = getGun();
        ItemStack itemStack = this.mob.getMainHandItem();
        GunData gunData = gun == null ? null : getGunData(gun, itemStack);
        if (gunData == null) {
            return GunShotResult.noShot(ammoCount);
        }

        this.attackCount += getFireRateStep(gun, itemStack, gunData, distance, isStunned);

        int currentAmmo = ammoCount;
        for (; this.attackCount >= 1; this.attackCount--) {
            if (isValid()) {
                GunAimUtil.lookAt(this.mob, targetPosition);
                IGunOperator.fromLivingEntity(this.mob).shoot(this.mob::getXRot, this.mob::getYHeadRot);

                if (gunData.getBolt() == Bolt.MANUAL_ACTION) {
                    IGunOperator.fromLivingEntity(this.mob).bolt();
                }
                if (currentAmmo > 0) {
                    currentAmmo -= 1;
                }

                if (currentAmmo == 0) {
                    stopAiming();
                    this.attackCount = 0;
                    return GunShotResult.depleted();
                }
            }
        }

        return GunShotResult.ready(currentAmmo);
    }

    private IGun getGun() {
        return IGun.getIGunOrNull(this.mob.getItemInHand(InteractionHand.MAIN_HAND));
    }

    private GunData getGunData() {
        IGun gun = getGun();
        return gun == null ? null : getGunData(gun, this.mob.getMainHandItem());
    }

    private GunData getGunData(IGun gun, ItemStack itemStack) {
        return TimelessAPI.getCommonGunIndex(gun.getGunId(itemStack)).map(CommonGunIndex::getGunData).orElse(null);
    }

    private int estimateScriptedReloadTicks(GunData gunData) {
        Map<String, Object> scriptParam = gunData.getScriptParam();
        if (scriptParam == null || !scriptParam.containsKey("loop")) {
            return -1;
        }

        boolean hasAmmo = hasAmmo();
        String introKey = hasAmmo ? "intro" : (isBurstMode() && scriptParam.containsKey("intro_empty_semi") ? "intro_empty_semi" : "intro_empty");
        float introSeconds = getScriptParamFloat(scriptParam, introKey, -1.0F);
        float loopSeconds = getScriptParamFloat(scriptParam, "loop", -1.0F);
        if (introSeconds < 0.0F || loopSeconds <= 0.0F) {
            return -1;
        }

        int missingAmmo = Math.max(1, getMissingAmmoCount());
        float endingSeconds = Math.max(0.0F, getScriptParamFloat(scriptParam, "ending", 0.0F));
        float loop2Seconds = getScriptParamFloat(scriptParam, "loop_2", -1.0F);
        float seconds = introSeconds + endingSeconds;
        if (loop2Seconds > 0.0F && missingAmmo > 1) {
            seconds += (missingAmmo / 2) * loop2Seconds + (missingAmmo % 2) * loopSeconds;
        } else {
            seconds += missingAmmo * loopSeconds;
        }
        return Math.max(1, Math.round(seconds * 20.0F));
    }

    private int getMissingAmmoCount() {
        int maxAmmoCount = getMaxAmmoCount();
        int ammoCount = getAmmoCount();
        if (maxAmmoCount <= 0 || ammoCount < 0) {
            return 0;
        }
        return Math.max(0, maxAmmoCount - ammoCount);
    }

    private boolean isBurstMode() {
        IGun gun = getGun();
        return gun != null && gun.getFireMode(this.mob.getMainHandItem()) == FireMode.BURST;
    }

    private float getScriptParamFloat(Map<String, Object> scriptParam, String key, float fallback) {
        Object value = scriptParam.get(key);
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Float.parseFloat(stringValue);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private double getFireRateStep(IGun gun, ItemStack itemStack, GunData gunData, double distance, boolean stunned) {
        double baseStep = DynamicFireRate.getStep(gun.getFireMode(itemStack) == FireMode.AUTO, gun.getRPM(itemStack), distance, stunned);
        return limitAmmoUseByAction(baseStep, getActionCycleTicks(gunData));
    }

    private double getActionCycleTicks(GunData gunData) {
        if (gunData.getBolt() != Bolt.MANUAL_ACTION) {
            return 0.0D;
        }

        Map<String, Object> scriptParam = gunData.getScriptParam();
        if (scriptParam != null) {
            float scriptBoltSeconds = getScriptParamFloat(scriptParam, "bolt_time", -1.0F);
            if (scriptBoltSeconds > 0.0F) {
                return scriptBoltSeconds * 20.0D;
            }
        }

        float boltActionSeconds = gunData.getBoltActionTime();
        return boltActionSeconds > 0.0F ? boltActionSeconds * 20.0D : 0.0D;
    }

    private double limitAmmoUseByAction(double ammoUsePerTick, double actionCycleTicks) {
        if (actionCycleTicks <= 0.0D) {
            return ammoUsePerTick;
        }
        return Math.min(ammoUsePerTick, 1.0D / actionCycleTicks);
    }

    private double getAttackReachSqr(LivingEntity target) {
        return this.mob.getBbWidth() * 2.0 * this.mob.getBbWidth() * 2.0 + target.getBbWidth();
    }

    private void setChargingCrossbow(boolean charging) {
        if (this.mob instanceof CrossbowAttackMob crossbowAttackMob) {
            crossbowAttackMob.setChargingCrossbow(charging);
        }
    }
}
