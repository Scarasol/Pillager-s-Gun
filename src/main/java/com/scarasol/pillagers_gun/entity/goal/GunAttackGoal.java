package com.scarasol.pillagers_gun.entity.goal;

import com.scarasol.pillagers_gun.api.IMob;
import com.scarasol.pillagers_gun.compat.recruits.RecruitCompat;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.entity.goal.combat.CombatAction;
import com.scarasol.pillagers_gun.entity.goal.combat.CombatContext;
import com.scarasol.pillagers_gun.entity.goal.combat.CombatDirector;
import com.scarasol.pillagers_gun.entity.goal.combat.CombatIntent;
import com.scarasol.pillagers_gun.entity.goal.combat.CombatMovementProfile;
import com.scarasol.pillagers_gun.entity.goal.combat.CrossFireDirector;
import com.scarasol.pillagers_gun.entity.goal.combat.FireTeamCoordinator;
import com.scarasol.pillagers_gun.entity.goal.combat.NoTacticsDirector;
import com.scarasol.pillagers_gun.entity.goal.controller.EmptyGunController;
import com.scarasol.pillagers_gun.entity.goal.controller.GunController;
import com.scarasol.pillagers_gun.entity.goal.controller.GunControllerFactory;
import com.scarasol.pillagers_gun.entity.goal.controller.GunReloadResult;
import com.scarasol.pillagers_gun.entity.goal.controller.GunShotResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * @author Scarasol
 */
public class GunAttackGoal<T extends Mob> extends Goal {

    public static MobEffect CONFUSION = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sona:confusion"));
    public static MobEffect BLIND = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("lrtactical:blinded"));
    private static final float HOLD_FIRE_MOVE_SCALE = 0.8F;

    private final T mob;
    private final double speedModifier;
    private GunController controller = EmptyGunController.INSTANCE;
    private GunState gunState = GunState.UNCHARGED;
    private int seeTime;
    private int attackDelay;
    private int ammoCount;
    private boolean away;
    private boolean stopped;
    private int strafingTime = -1;
    private boolean strafingClockwise;

    public GunAttackGoal(T mob, double speedModifier, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public static boolean isStunned(LivingEntity mob) {
        return mob != null && ((CONFUSION != null && mob.hasEffect(CONFUSION)) || (BLIND != null && mob.hasEffect(BLIND)));
    }

    @Override
    public boolean canUse() {
        GunController gunController = controller();
        return canUseRegularGunAttack()
                && gunController.isValid()
                && (this.isValidTarget() || (!gunController.hasAmmo() && gunController.canReload()) || isStunned(this.mob));
    }

    @Override
    public boolean canContinueToUse() {
        GunController gunController = controller();
        return canUseRegularGunAttack()
                && gunController.isValid()
                && (this.isValidTarget() || (!gunController.hasAmmo() && gunController.canReload()) || isStunned(this.mob));
    }

    @Override
    public void start() {
        this.controller = GunControllerFactory.create(this.mob);
        this.gunState = GunState.UNCHARGED;
        this.seeTime = 0;
        this.attackDelay = 0;
        this.ammoCount = 0;
        this.away = false;
        this.stopped = false;
        this.strafingTime = -1;
        this.strafingClockwise = false;
        this.controller.start();
    }

    @Override
    public void stop() {
        super.stop();
        FireTeamCoordinator.INSTANCE.remove(this.mob);
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        setLastPositon(null);
        this.seeTime = 0;
        this.attackDelay = 0;
        this.ammoCount = 0;
        this.away = false;
        this.stopped = false;
        this.strafingTime = -1;
        this.strafingClockwise = false;
        this.controller.stop();
        this.controller = EmptyGunController.INSTANCE;
        stopAllMovement();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        GunController gunController = controller();
        if (!gunController.isValid()) {
            return;
        }

        LivingEntity livingentity = this.mob.getTarget();
        boolean targetValid = this.isValidTarget();
        boolean flag2 = false;
        boolean flag = false;
        double attackRadius = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        double distanceToTarget = 0.0D;
        boolean stunned = isStunned(this.mob);

        if (targetValid) {
            this.mob.setAggressive(true);
            distanceToTarget = livingentity.position().subtract(this.mob.position()).length();
            flag = this.mob.getSensing().hasLineOfSight(livingentity) && !stunned;
            if (flag) {
                ++this.seeTime;
            } else {
                this.seeTime = 0;
            }

            flag2 = (distanceToTarget > attackRadius || this.seeTime < 5) && this.attackDelay == 0;
        }

        if (targetValid && gunController.canMeleeAttack(livingentity)) {
            setLastPositon(livingentity.getEyePosition());
            gunController.meleeAttack(livingentity);
            return;
        }

        if (gunController.isAttackBlocked()) {
            this.gunState = GunState.UNCHARGED;
            if (targetValid) {
                setLastPositon(livingentity.getEyePosition());
            }
            return;
        }

        if (this.gunState == GunState.UNCHARGED && gunController.hasAmmo()) {
            this.gunState = GunState.CHARGED;
            this.attackDelay = gunController.getReadyDelayAfterAmmoFound();
            this.ammoCount = gunController.getAmmoCount();
        }
        if (this.gunState == GunState.CHARGED && !gunController.hasAmmo()) {
            this.gunState = GunState.UNCHARGED;
        }

        CombatContext combatContext = createCombatContext(gunController, livingentity, targetValid, flag, stunned, flag2, distanceToTarget, attackRadius);
        CombatDirector combatDirector = combatDirector();
        CombatIntent combatIntent = combatDirector.selectIntent(combatContext);
        updateTargetMovement(gunController, livingentity, targetValid, flag, stunned, combatIntent, distanceToTarget, attackRadius);

        switch (this.gunState) {
            case UNCHARGED -> {
                if (combatIntent.action() == CombatAction.RELOAD) {
                    startReloading(gunController);
                }
            }
            case CHARGING -> {
                GunReloadResult reloadResult = gunController.tickReloading();
                if (reloadResult == GunReloadResult.FAILED) {
                    this.gunState = GunState.UNCHARGED;
                } else if (reloadResult == GunReloadResult.RELOADED) {
                    this.gunState = GunState.CHARGED;
                    this.attackDelay = gunController.getReadyDelayAfterReload();
                    this.ammoCount = gunController.getAmmoCount();
                }
            }
            case CHARGED -> {
                if (combatIntent.action() == CombatAction.RELOAD) {
                    startReloading(gunController);
                } else if (--this.attackDelay <= 0) {
                    gunController.startAiming(livingentity);
                    this.gunState = GunState.READY_TO_ATTACK;
                    this.attackDelay = 0;
                }
            }
            case READY_TO_ATTACK -> {
                if (combatIntent.action() == CombatAction.RELOAD) {
                    startReloading(gunController);
                } else if (combatIntent.action() != CombatAction.HOLD_FIRE && targetValid && (flag || stunned) && isRightAngle()) {
                    GunShotResult shotResult = gunController.shoot(livingentity, stunned, getLastPositon(), this.ammoCount);
                    this.ammoCount = shotResult.getAmmoCount();
                    if (shotResult.isDepleted()) {
                        this.gunState = GunState.UNCHARGED;
                    } else if (shotResult.shouldCooldown()) {
                        this.gunState = GunState.CHARGED;
                        this.attackDelay = shotResult.getCooldownTicks();
                    }
                }
            }
        }

        if (targetValid) {
            setLastPositon(livingentity.getEyePosition());
        }
    }

    private CombatDirector combatDirector() {
        return CommonConfig.ENABLE_CROSS_FIRE.get() ? CrossFireDirector.INSTANCE : NoTacticsDirector.INSTANCE;
    }

    private void updateTargetMovement(GunController gunController,
                                      @Nullable LivingEntity target,
                                      boolean targetValid,
                                      boolean hasLineOfSight,
                                      boolean stunned,
                                      CombatIntent combatIntent,
                                      double distanceToTarget,
                                      double attackRadius) {
        if (!targetValid || target == null || stunned) {
            stopAllMovement();
            return;
        }

        if (distanceToTarget > attackRadius || this.seeTime <= 5) {
            chaseTarget(target);
            return;
        }

        boolean reloadMovement = !gunController.hasAmmo()
                || this.gunState == GunState.CHARGING
                || combatIntent.action() == CombatAction.RELOAD;
        if (reloadMovement) {
            updateReloadMovement(target, distanceToTarget, attackRadius);
            lookAtTarget(target);
            return;
        }

        updateCombatStrafe(gunController, target, hasLineOfSight, combatIntent, distanceToTarget, attackRadius);
        lookAtTarget(target);
    }

    private void chaseTarget(LivingEntity target) {
        resetCombatMovement();
        this.stopped = false;
        this.away = false;
        this.mob.getNavigation().moveTo(target, this.canRun() ? this.speedModifier : this.speedModifier * 0.5D);
        lookAtTarget(target);
    }

    private void updateReloadMovement(LivingEntity target, double distanceToTarget, double attackRadius) {
        resetCombatMovement();
        this.stopped = false;
        if (distanceToTarget < attackRadius / 2.0D) {
            this.away = true;
            Vec3 retreatTarget = this.mob.position().add(this.mob.position().subtract(target.position().x, this.mob.position().y, target.position().z).normalize().scale(attackRadius / 2.0D));
            this.mob.getNavigation().moveTo(retreatTarget.x, retreatTarget.y, retreatTarget.z, this.speedModifier);
        }
        if (this.away && distanceToTarget > attackRadius * 2.0D / 3.0D) {
            this.mob.getNavigation().stop();
            this.away = false;
        } else if (!this.away && distanceToTarget < attackRadius * 2.0D / 3.0D) {
            this.mob.getNavigation().stop();
        }
    }

    private void holdCurrentPosition() {
        resetCombatMovement();
        this.away = false;
        if (!this.stopped) {
            stopAllMovement();
            this.stopped = true;
        } else {
            clearMoveInput();
        }
    }

    private void updateCombatStrafe(GunController gunController,
                                    LivingEntity target,
                                    boolean hasLineOfSight,
                                    CombatIntent combatIntent,
                                    double distanceToTarget,
                                    double attackRadius) {
        this.away = false;
        this.stopped = true;
        this.mob.getNavigation().stop();

        if (!hasLineOfSight) {
            resetCombatMovement();
            clearMoveInput();
            return;
        }

        if (this.strafingTime < 0) {
            this.strafingTime = 0;
        }
        if (++this.strafingTime >= 20) {
            if (this.mob.getRandom().nextFloat() < 0.3F) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            this.strafingTime = 0;
        }

        CombatMovementProfile profile = CombatMovementProfile.forRole(gunController.getRole());
        double distanceRatio = attackRadius <= 0.0D ? 1.0D : distanceToTarget / attackRadius;
        boolean targetLookingAtMob = isTargetLookingAtMob(target, profile.targetLookAngleDegrees(distanceRatio));
        float stateMoveScale = getStateMoveScale(combatIntent);
        float forward = scaleMovement(profile.forward(distanceRatio), stateMoveScale);
        float strafe = scaleMovement(profile.strafe(targetLookingAtMob, this.strafingClockwise), stateMoveScale);
        if (profile.shouldMove(forward, strafe)) {
            this.mob.getMoveControl().strafe(forward, strafe);
            if (this.mob.getControlledVehicle() instanceof Mob vehicle) {
                vehicle.lookAt(target, 30.0F, 30.0F);
            }
        } else {
            clearMoveInput();
        }
    }

    private float getStateMoveScale(CombatIntent combatIntent) {
        return combatIntent.action() == CombatAction.HOLD_FIRE ? HOLD_FIRE_MOVE_SCALE : 1.0F;
    }

    private float scaleMovement(float movement, float stateMoveScale) {
        return (float) Math.max(-1.0D, Math.min(1.0D, movement * this.speedModifier * stateMoveScale));
    }

    private boolean isTargetLookingAtMob(LivingEntity target, double angleThresholdDegrees) {
        Vec3 targetLook = target.getLookAngle();
        Vec3 targetToMob = this.mob.getEyePosition().subtract(target.getEyePosition());
        if (targetLook.lengthSqr() < 1.0E-6D || targetToMob.lengthSqr() < 1.0E-6D) {
            return false;
        }
        double dot = targetLook.normalize().dot(targetToMob.normalize());
        return dot >= Math.cos(Math.toRadians(angleThresholdDegrees));
    }

    private void resetCombatMovement() {
        this.strafingTime = -1;
    }

    private void stopAllMovement() {
        resetCombatMovement();
        this.mob.getNavigation().stop();
        clearMoveInput();
    }

    private void clearMoveInput() {
        this.mob.getMoveControl().strafe(0.0F, 0.0F);
        this.mob.setXxa(0.0F);
        this.mob.setZza(0.0F);
        this.mob.setSpeed(0.0F);
    }

    private void lookAtTarget(LivingEntity target) {
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.mob.lookAt(target, 30.0F, 30.0F);
    }

    private CombatContext createCombatContext(GunController gunController,
                                              LivingEntity target,
                                              boolean targetValid,
                                              boolean hasLineOfSight,
                                              boolean stunned,
                                              boolean delayReloadForPositioning,
                                              double distanceToTarget,
                                              double attackRadius) {
        return new CombatContext(
                this.mob,
                target,
                gunController,
                this.gunState,
                targetValid,
                hasLineOfSight,
                stunned,
                delayReloadForPositioning,
                distanceToTarget,
                attackRadius,
                this.seeTime,
                this.attackDelay,
                this.ammoCount,
                this.mob.level().getGameTime()
        );
    }

    private void startReloading(GunController gunController) {
        gunController.stopAiming();
        gunController.startReload();
        this.gunState = GunState.CHARGING;
        this.attackDelay = 0;
        this.ammoCount = gunController.getAmmoCount();
    }

    private GunController controller() {
        if (!this.controller.isValid()) {
            this.controller = GunControllerFactory.create(this.mob);
        }
        return this.controller;
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    private boolean canUseRegularGunAttack() {
        return !ModList.get().isLoaded("recruits") || RecruitCompat.canUseRegularGunAttack(this.mob);
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

    private boolean canRun() {
        return this.gunState != GunState.CHARGING;
    }

    public double vectorDegreeCalculate(Vec3 vec1, Vec3 vec2) {
        double cos = vec1.dot(vec2) / vec1.length() / vec2.length();
        return Math.toDegrees(Math.acos(cos));
    }

    @Nullable
    public Vec3 getLastPositon() {
        if (this.mob instanceof IMob iMob) {
            return iMob.getPillagersGun$targetLastPositon();
        }
        return null;
    }

    public void setLastPositon(Vec3 lastPositon) {
        if (this.mob instanceof IMob iMob) {
            iMob.setPillagersGun$targetLastPositon(lastPositon);
        }
    }

    public enum GunState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}
