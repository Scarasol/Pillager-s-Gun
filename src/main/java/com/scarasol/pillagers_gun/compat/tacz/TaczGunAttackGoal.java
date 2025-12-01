package com.scarasol.pillagers_gun.compat.tacz;

import com.scarasol.pillagers_gun.PillagersGunMod;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;

import static com.scarasol.pillagers_gun.entity.goal.GunAttackGoal.isStunned;

/**
 * @author Scarasol
 */
public class TaczGunAttackGoal<T extends Mob> extends Goal {

    private final T mob;
    private TaczGunAttackGoal.GunState gunState = TaczGunAttackGoal.GunState.UNCHARGED;
    private final double speedModifier;
    private int seeTime;
    private int attackDelay;
    private double attackCount;
    private int ammoCount;
    private boolean away;
    private boolean stopped;

    public TaczGunAttackGoal(T mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.isHoldingGun() && (this.isValidTarget() || (!hasAmmo() && canReload()) || isStunned(this.mob));
    }

    private boolean isHoldingGun() {
        return getGun() != null;
    }

    private boolean canReload() {
        if (!CommonConfig.TACZ_GUNNERS_NEED_AMMO.get()) {
            return true;
        }
        if (getGun() instanceof AbstractGunItem gunItem) {
            ItemStack itemStack = this.mob.getItemInHand(InteractionHand.MAIN_HAND);
            if (gunItem.useDummyAmmo(itemStack) && gunItem.getDummyAmmoAmount(itemStack) == 0) {
                itemStack.getOrCreateTag().remove("DummyAmmo");
            }
            return gunItem.canReload(this.mob, itemStack);
        }
        return false;
    }

    private IGun getGun() {
        return IGun.getIGunOrNull(this.mob.getItemInHand(InteractionHand.MAIN_HAND));
    }

    private boolean isValidTarget() {

        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }


    @Override
    public boolean canContinueToUse() {
        return this.isHoldingGun() && (this.isValidTarget() || (!hasAmmo() && canReload()) || isStunned(this.mob));
    }

    private boolean hasAmmo() {
        return getAmmoCount(this.mob.getItemInHand(InteractionHand.MAIN_HAND)) != 0;
    }

    private boolean isRightAngle() {
        if (isStunned(this.mob)) {
            return true;
        }
        if (isValidTarget()) {
            LivingEntity target = this.mob.getTarget();
            return vectorDegreeCalculate(this.mob.getViewVector(1), target.getEyePosition().subtract(this.mob.getEyePosition())) < 10 + Math.max(0, 64 - this.mob.distanceToSqr(target));
        }
        return false;

    }

    private int getAmmoCount(ItemStack itemStack) {
        IGun iGun = IGun.getIGunOrNull(itemStack);
        if (iGun != null) {
            GunData gunData = TimelessAPI.getCommonGunIndex(iGun.getGunId(itemStack)).map(CommonGunIndex::getGunData).orElse(null);
            if (gunData != null) {
                return iGun.useInventoryAmmo(itemStack) ? -1 : iGun.getCurrentAmmoCount(itemStack) + (iGun.hasBulletInBarrel(itemStack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
            }
        }
        return 0;
    }

    @Override
    public void start() {
        gunState = GunState.UNCHARGED;
        IGunOperator.fromLivingEntity(this.mob).draw(this.mob::getMainHandItem);
        IGunOperator.fromLivingEntity(this.mob).getDataHolder().drawTimestamp = System.currentTimeMillis() - 10000;
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        this.attackDelay = 0;
        this.attackCount = 0;
        this.mob.getNavigation().stop();
        if (isHoldingGun()) {
            IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.mob);
            gunOperator.cancelReload();
            gunOperator.aim(false);
            GunData gunData = TimelessAPI.getCommonGunIndex(getGun().getGunId(this.mob.getMainHandItem())).map(CommonGunIndex::getGunData).orElse(null);
            if (gunData != null && gunData.getBolt() == Bolt.MANUAL_ACTION) {
                gunOperator.bolt();
            }
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {

        LivingEntity livingentity = this.mob.getTarget();
        boolean flag2 = false;
        boolean flag = false;
        double attackRadius = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        ItemStack itemStack = this.mob.getMainHandItem();
        boolean isStunned = isStunned(this.mob);
        if (isValidTarget()) {
            this.mob.setAggressive(true);
            double d0 = livingentity.position().subtract(this.mob.position()).length();
            flag = this.mob.getSensing().hasLineOfSight(livingentity) && !isStunned;
            if (flag) {
                ++this.seeTime;
            } else {
                this.seeTime = 0;
            }

            if (!isStunned) {
                if (d0 <= attackRadius && this.seeTime > 5) {
                    if (!hasAmmo()) {
                        stopped = false;
                        if (d0 < attackRadius / 2) {
                            away = true;
                            Vec3 vec3 = this.mob.position().add(this.mob.position().subtract(livingentity.position().x, this.mob.position().y, livingentity.position().z).normalize().scale(attackRadius / 2));
                            this.mob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier);
                        }
                        if (away && d0 > attackRadius * 2 / 3) {
                            this.mob.getNavigation().stop();
                            away = false;
                        }
                        else if (!away && d0 < attackRadius * 2 / 3) {
                            this.mob.getNavigation().stop();
                        }
                    } else if (hasAmmo() && !stopped) {
                        this.mob.getNavigation().stop();
                        stopped = true;
                    }
                }else {
                    stopped = false;
                    this.mob.getNavigation().moveTo(livingentity, this.canRun() ? this.speedModifier : this.speedModifier * 0.5D);
                }
                this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            }
            flag2 = (d0 > attackRadius || this.seeTime < 5) && this.attackDelay == 0;
        }
        if (this.isHoldingGun()) {
            IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.mob);
            if (isValidTarget() && canMeleeAttack(gunOperator, livingentity)) {
                gunOperator.melee();
                return;
            }
            if (getGun().isOverheatLocked(itemStack)) {
                this.gunState = TaczGunAttackGoal.GunState.UNCHARGED;
                return;
            }
            if (this.gunState == TaczGunAttackGoal.GunState.UNCHARGED && hasAmmo()) {
                this.gunState = TaczGunAttackGoal.GunState.CHARGED;
                ammoCount = getAmmoCount(itemStack);
            }else if (this.gunState == TaczGunAttackGoal.GunState.CHARGED && !hasAmmo()){
                this.gunState = TaczGunAttackGoal.GunState.UNCHARGED;
            }
            if (this.gunState == TaczGunAttackGoal.GunState.UNCHARGED) {
                if (!flag2 && canReload() && !isStunned) {

                    gunOperator.reload();
                    this.gunState = TaczGunAttackGoal.GunState.CHARGING;

                    if (this.mob instanceof CrossbowAttackMob crossbowAttackMob) {
                        crossbowAttackMob.setChargingCrossbow(true);
                    }
                }
            } else if (this.gunState == TaczGunAttackGoal.GunState.CHARGING) {

                if (!isHoldingGun()) {
                    this.gunState = TaczGunAttackGoal.GunState.UNCHARGED;
                }

                if (!gunOperator.getDataHolder().reloadStateType.isReloading()) {
                    if (hasAmmo()) {
                        this.gunState = TaczGunAttackGoal.GunState.CHARGED;
                        this.attackDelay = 10 + this.mob.getRandom().nextInt(20);
                        ammoCount = getAmmoCount(itemStack);
                    }else {
                        this.gunState = TaczGunAttackGoal.GunState.UNCHARGED;
                    }
                    if (this.mob instanceof CrossbowAttackMob crossbowAttackMob) {
                        crossbowAttackMob.setChargingCrossbow(false);
                    }
                }

            } else if (this.gunState == TaczGunAttackGoal.GunState.CHARGED) {
                if (--this.attackDelay <= 0) {
                    gunOperator.aim(true);
                    this.gunState = TaczGunAttackGoal.GunState.READY_TO_ATTACK;
                    this.attackDelay = 0;
                }
            } else if (this.gunState == TaczGunAttackGoal.GunState.READY_TO_ATTACK && (flag || isStunned) && isRightAngle()) {
                IGun iGun = getGun();
                GunData gunData = TimelessAPI.getCommonGunIndex(iGun.getGunId(itemStack)).map(CommonGunIndex::getGunData).orElse(null);
                if (gunData != null) {
                    attackCount += iGun.getFireMode(itemStack) == FireMode.AUTO ? iGun.getRPM(itemStack) / 1200D : iGun.getRPM(itemStack) / (isStunned ? 2400D : Math.max(1200D * (livingentity.distanceTo(this.mob) / 8), 2400D));
                    for (; attackCount >= 1; attackCount--) {
                        if (isHoldingGun()) {
                            gunOperator.shoot(this.mob::getXRot, this.mob::getYHeadRot);

                            if (gunData.getBolt() == Bolt.MANUAL_ACTION) {
                                gunOperator.bolt();
                            }
                            if (ammoCount > 0) {
                                ammoCount -= 1;
                            }

                            if (ammoCount == 0) {
                                gunOperator.aim(false);
                                attackCount = 0;
                                this.gunState = TaczGunAttackGoal.GunState.UNCHARGED;
                                break;
                            }
                        }
                    }
                }

            }
        }



    }

    private boolean canRun() {
        return this.gunState != TaczGunAttackGoal.GunState.CHARGING;
    }

    public double vectorDegreeCalculate(Vec3 vec1, Vec3 vec2) {
        double cos = vec1.dot(vec2) / vec1.length() / vec2.length();
        return Math.toDegrees(Math.acos(cos));
    }

    public boolean canMeleeAttack(IGunOperator gunOperator, LivingEntity target) {
        return System.currentTimeMillis() - gunOperator.getDataHolder().meleeTimestamp > 3000 && isValidTarget() && getAttackReachSqr(target) >= this.mob.distanceToSqr(target);
    }

    public double getAttackReachSqr(LivingEntity target) {
        return this.mob.getBbWidth() * 2.0 * this.mob.getBbWidth() * 2.0 + target.getBbWidth();
    }

    enum GunState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}
