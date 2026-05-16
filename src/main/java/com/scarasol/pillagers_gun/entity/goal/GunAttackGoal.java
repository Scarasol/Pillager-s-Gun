package com.scarasol.pillagers_gun.entity.goal;

import com.scarasol.pillagers_gun.api.IMob;
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

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * @author Scarasol
 */
public class GunAttackGoal<T extends Mob> extends Goal {

    public static MobEffect CONFUSION = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sona:confusion"));
    public static MobEffect BLIND = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("lrtactical:blinded"));

    private final T mob;
    private final double speedModifier;
    private GunController controller = EmptyGunController.INSTANCE;
    private GunState gunState = GunState.UNCHARGED;
    private int seeTime;
    private int attackDelay;
    private int ammoCount;
    private boolean away;
    private boolean stopped;

    public GunAttackGoal(T mob, double speedModifier, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public static boolean isStunned(LivingEntity mob) {
        return mob.hasEffect(CONFUSION) || mob.hasEffect(BLIND);
    }

    @Override
    public boolean canUse() {
        GunController gunController = controller();
        return gunController.isValid() && (this.isValidTarget() || (!gunController.hasAmmo() && gunController.canReload()) || isStunned(this.mob));
    }

    @Override
    public boolean canContinueToUse() {
        GunController gunController = controller();
        return gunController.isValid() && (this.isValidTarget() || (!gunController.hasAmmo() && gunController.canReload()) || isStunned(this.mob));
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
        this.controller.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        setLastPositon(null);
        this.seeTime = 0;
        this.attackDelay = 0;
        this.ammoCount = 0;
        this.away = false;
        this.stopped = false;
        this.controller.stop();
        this.controller = EmptyGunController.INSTANCE;
        this.mob.getNavigation().stop();
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
        boolean stunned = isStunned(this.mob);

        if (targetValid) {
            this.mob.setAggressive(true);
            double d0 = livingentity.position().subtract(this.mob.position()).length();
            flag = this.mob.getSensing().hasLineOfSight(livingentity) && !stunned;
            if (flag) {
                ++this.seeTime;
            } else {
                this.seeTime = 0;
            }

            if (!stunned) {
                if (d0 <= attackRadius && this.seeTime > 5) {
                    if (!gunController.hasAmmo()) {
                        this.stopped = false;
                        if (d0 < attackRadius / 2) {
                            this.away = true;
                            Vec3 vec3 = this.mob.position().add(this.mob.position().subtract(livingentity.position().x, this.mob.position().y, livingentity.position().z).normalize().scale(attackRadius / 2));
                            this.mob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier);
                        }
                        if (this.away && d0 > attackRadius * 2 / 3) {
                            this.mob.getNavigation().stop();
                            this.away = false;
                        } else if (!this.away && d0 < attackRadius * 2 / 3) {
                            this.mob.getNavigation().stop();
                        }
                    } else if (!this.stopped) {
                        this.mob.getNavigation().stop();
                        this.stopped = true;
                    }
                } else {
                    this.stopped = false;
                    this.mob.getNavigation().moveTo(livingentity, this.canRun() ? this.speedModifier : this.speedModifier * 0.5D);
                }
                this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            }
            flag2 = (d0 > attackRadius || this.seeTime < 5) && this.attackDelay == 0;
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

        switch (this.gunState) {
            case UNCHARGED -> {
                if (!flag2 && gunController.canReload() && !stunned) {
                    gunController.startReload();
                    this.gunState = GunState.CHARGING;
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
                if (--this.attackDelay <= 0) {
                    gunController.startAiming(livingentity);
                    this.gunState = GunState.READY_TO_ATTACK;
                    this.attackDelay = 0;
                }
            }
            case READY_TO_ATTACK -> {
                if (targetValid && (flag || stunned) && isRightAngle()) {
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

    private GunController controller() {
        if (!this.controller.isValid()) {
            this.controller = GunControllerFactory.create(this.mob);
        }
        return this.controller;
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
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

    enum GunState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}
