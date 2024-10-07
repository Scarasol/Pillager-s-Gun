package com.scarasol.pillagers_gun.entity.goal;

import java.util.EnumSet;

import com.scarasol.pillagers_gun.PillagersGunMod;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;

public class GunAttackGoal<T extends Mob & RangedAttackMob & CrossbowAttackMob> extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final T mob;
    private GunAttackGoal.GunState gunState = GunAttackGoal.GunState.UNCHARGED;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;
    private int ammoCount;

    public GunAttackGoal(T mob, double speedModifier, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        return this.isHoldingGun() && (this.isValidTarget() || !hasAmmo());
    }

    private boolean isHoldingGun() {
        return this.mob.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof GunItem;
    }

    public boolean canContinueToUse() {
        return this.isHoldingGun() && (this.isValidTarget() || !hasAmmo());
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    public boolean hasAmmo() {
        return GunItem.isCharged(this.mob.getItemInHand(InteractionHand.MAIN_HAND));
    }

    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
            GunItem.setCharged(this.mob.getUseItem(), false);
        }

    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity livingentity = this.mob.getTarget();
        boolean flag = false;
        boolean flag2 = false;
        if (livingentity != null) {
            this.mob.setAggressive(true);
            flag = this.mob.getSensing().hasLineOfSight(livingentity);
            boolean flag1 = this.seeTime > 0;
            if (flag != flag1) {
                this.seeTime = 0;
            }

            if (flag) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            double d0 = this.mob.distanceToSqr(livingentity);
            flag2 = (d0 > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;

            if (flag2) {
                --this.updatePathDelay;
                if (this.updatePathDelay <= 0) {
                    this.mob.getNavigation().moveTo(livingentity, this.canRun() ? this.speedModifier : this.speedModifier * 0.5D);
                    this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
                }
            } else {
                this.updatePathDelay = 0;
                this.mob.getNavigation().stop();
            }
            this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
        }
        if (this.gunState == GunAttackGoal.GunState.UNCHARGED && GunItem.isCharged(this.mob.getItemInHand(InteractionHand.MAIN_HAND))) {
            this.gunState = GunAttackGoal.GunState.CHARGED;
            this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
            ammoCount = GunItem.getCurrentAmmoCount(this.mob.getItemInHand(InteractionHand.MAIN_HAND));
        }
        if (this.gunState == GunAttackGoal.GunState.CHARGED && !GunItem.isCharged(this.mob.getItemInHand(InteractionHand.MAIN_HAND))){
            this.gunState = GunAttackGoal.GunState.UNCHARGED;
        }
        if (this.gunState == GunAttackGoal.GunState.UNCHARGED) {
            if (!flag2) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof GunItem));
                this.gunState = GunAttackGoal.GunState.CHARGING;
                this.mob.setChargingCrossbow(true);
            }
        } else if (this.gunState == GunAttackGoal.GunState.CHARGING) {
            if (!this.mob.isUsingItem()) {
                this.gunState = GunAttackGoal.GunState.UNCHARGED;
            }

            int i = this.mob.getTicksUsingItem();
            ItemStack itemstack = this.mob.getUseItem();
            if (i >= GunItem.getChargeDuration(itemstack)) {
                this.mob.releaseUsingItem();
                this.gunState = GunAttackGoal.GunState.CHARGED;
                this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
                ammoCount = GunItem.getCurrentAmmoCount(this.mob.getItemInHand(InteractionHand.MAIN_HAND));
                this.mob.setChargingCrossbow(false);
            }

        } else if (this.gunState == GunAttackGoal.GunState.CHARGED) {
            --this.attackDelay;
            if (this.attackDelay == 0) {
                this.gunState = GunAttackGoal.GunState.READY_TO_ATTACK;
            }
        } else if (this.gunState == GunAttackGoal.GunState.READY_TO_ATTACK && flag) {
            InteractionHand interactionhand = ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof GunItem);
            ItemStack itemstack = this.mob.getItemInHand(interactionhand);
            if (this.mob.isHolding(is -> is.getItem() instanceof GunItem)) {
                GunItem.performShooting(this.mob.level(), this.mob, interactionhand, itemstack, 5F, (float) (14 - this.mob.level().getDifficulty().getId() * 4));
            }
            this.mob.onCrossbowAttackPerformed();
            ammoCount -= 1;
            ItemStack itemstack1 = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof GunItem));
            if (ammoCount <= 0) {
                GunItem.setCharged(itemstack1, false);
                this.gunState = GunAttackGoal.GunState.UNCHARGED;
            } else {
                this.gunState = GunAttackGoal.GunState.CHARGED;
                this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
            }
        }


    }

    private boolean canRun() {
        return this.gunState == GunAttackGoal.GunState.UNCHARGED;
    }

    enum GunState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}

