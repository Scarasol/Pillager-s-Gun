package com.scarasol.pillagers_gun.compat.recruits.goal;

import com.scarasol.pillagers_gun.compat.recruits.RecruitCompat;
import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.scarasol.pillagers_gun.entity.goal.controller.EmptyGunController;
import com.scarasol.pillagers_gun.entity.goal.controller.GunAimUtil;
import com.scarasol.pillagers_gun.entity.goal.controller.GunController;
import com.scarasol.pillagers_gun.entity.goal.controller.GunControllerFactory;
import com.scarasol.pillagers_gun.entity.goal.controller.GunReloadResult;
import com.scarasol.pillagers_gun.entity.goal.controller.GunShotResult;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class RecruitGunStrategicFireGoal extends Goal {
    private final AbstractRecruitEntity recruit;
    private GunController controller = EmptyGunController.INSTANCE;
    private GunState gunState = GunState.UNCHARGED;
    private int attackDelay;
    private int ammoCount;

    public RecruitGunStrategicFireGoal(AbstractRecruitEntity recruit) {
        this.recruit = recruit;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!canStrategicFire()) {
            return false;
        }
        GunController gunController = controller();
        return gunController.isValid()
                && (gunController.hasAmmo() || gunController.canReload());
    }

    @Override
    public boolean canContinueToUse() {
        if (!canStrategicFire()) {
            return false;
        }
        GunController gunController = controller();
        return gunController.isValid()
                && (gunController.hasAmmo() || gunController.canReload() || this.gunState == GunState.CHARGING);
    }

    @Override
    public void start() {
        this.controller = GunControllerFactory.create(this.recruit);
        this.gunState = GunState.UNCHARGED;
        this.attackDelay = 0;
        this.ammoCount = 0;
        this.controller.start();
        this.recruit.getNavigation().stop();
    }

    @Override
    public void stop() {
        super.stop();
        this.recruit.setAggressive(false);
        this.attackDelay = 0;
        this.ammoCount = 0;
        this.gunState = GunState.UNCHARGED;
        this.controller.stop();
        this.controller = EmptyGunController.INSTANCE;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        GunController gunController = controller();
        BlockPos strategicFirePos = RecruitCompat.getStrategicFirePos(this.recruit);
        if (!gunController.isValid() || strategicFirePos == null) {
            return;
        }

        Vec3 targetPosition = Vec3.atCenterOf(strategicFirePos);
        this.recruit.setAggressive(true);
        this.recruit.getNavigation().stop();
        GunAimUtil.lookAt(this.recruit, targetPosition);

        if (gunController.isAttackBlocked()) {
            this.gunState = GunState.UNCHARGED;
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
                if (gunController.canReload() && !GunAttackGoal.isStunned(this.recruit)) {
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
                    gunController.startAiming(targetPosition);
                    this.gunState = GunState.READY_TO_ATTACK;
                    this.attackDelay = 0;
                }
            }
            case READY_TO_ATTACK -> {
                boolean stunned = GunAttackGoal.isStunned(this.recruit);
                if ((stunned || GunAimUtil.isLookingAt(this.recruit, targetPosition, 10.0D))) {
                    GunShotResult shotResult = gunController.shootAt(targetPosition, stunned, targetPosition, this.ammoCount);
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
    }

    private GunController controller() {
        if (!this.controller.isValid()) {
            this.controller = GunControllerFactory.create(this.recruit);
        }
        return this.controller;
    }

    private boolean canStrategicFire() {
        return this.recruit.getTarget() == null
                && RecruitCompat.shouldStrategicFire(this.recruit)
                && RecruitCompat.getStrategicFirePos(this.recruit) != null
                && this.recruit.getFollowState() != 5
                && !this.recruit.needsToGetFood()
                && !this.recruit.getShouldMount();
    }

    enum GunState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }
}
