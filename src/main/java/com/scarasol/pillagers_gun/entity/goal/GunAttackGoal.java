package com.scarasol.pillagers_gun.entity.goal;

import java.util.EnumSet;

import com.scarasol.pillagers_gun.PillagersGunMod;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Scarasol
 */
public class GunAttackGoal<T extends Mob> extends Goal {

    public static MobEffect CONFUSION = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("sona:confusion"));
    public static MobEffect BLIND = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("lrtactical:blinded"));

    private final T mob;
    private GunAttackGoal.GunState gunState = GunAttackGoal.GunState.UNCHARGED;
    private final double speedModifier;
//    private final float attackRadius;
    private int seeTime;
    private int attackDelay;
    private int ammoCount;
    private boolean away;
    private boolean stopped;

    public GunAttackGoal(T mob, double speedModifier, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
//        this.attackRadius = attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public static boolean isStunned(LivingEntity mob) {
        return mob.hasEffect(CONFUSION) || mob.hasEffect(BLIND);
    }

    @Override
    public boolean canUse() {
        return this.isHoldingGun() && (this.isValidTarget() || !hasAmmo() || isStunned(this.mob));
    }

    private boolean isHoldingGun() {
        return this.mob.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof GunItem;
    }

    @Override
    public boolean canContinueToUse() {
        return this.isHoldingGun() && (this.isValidTarget() || !hasAmmo() || isStunned(this.mob));
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    public boolean hasAmmo() {
        return GunItem.isCharged(this.mob.getItemInHand(InteractionHand.MAIN_HAND));
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

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
//            this.mob.setChargingCrossbow(false);
            GunItem.setCharged(this.mob.getUseItem(), false);
        }
        this.mob.getNavigation().stop();

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
        double attackRadius = this.mob.getAttributeBaseValue(Attributes.FOLLOW_RANGE);
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

        if (this.gunState == GunAttackGoal.GunState.UNCHARGED && GunItem.isCharged(this.mob.getItemInHand(InteractionHand.MAIN_HAND))) {
            this.gunState = GunAttackGoal.GunState.CHARGED;
            this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
            ammoCount = GunItem.getCurrentAmmoCount(this.mob.getItemInHand(InteractionHand.MAIN_HAND));
        }
        if (this.gunState == GunAttackGoal.GunState.CHARGED && !GunItem.isCharged(this.mob.getItemInHand(InteractionHand.MAIN_HAND))){
            this.gunState = GunAttackGoal.GunState.UNCHARGED;
        }
        if (this.gunState == GunAttackGoal.GunState.UNCHARGED) {
            if (!flag2 && !isStunned) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof GunItem));
                this.gunState = GunAttackGoal.GunState.CHARGING;
                if (this.mob instanceof CrossbowAttackMob crossbowAttackMob) {
                    crossbowAttackMob.setChargingCrossbow(true);
                }
            }
        } else if (this.gunState == GunAttackGoal.GunState.CHARGING) {
            if (!this.mob.isUsingItem()) {
                this.gunState = GunAttackGoal.GunState.UNCHARGED;
            }

            int i = this.mob.getTicksUsingItem();
            ItemStack itemstack = this.mob.getUseItem();
            if (itemstack.getItem() instanceof GunItem gun && i >= GunItem.getChargeDuration(itemstack)) {
                this.mob.releaseUsingItem();
                this.gunState = GunAttackGoal.GunState.CHARGED;
                this.attackDelay = 10 + this.mob.getRandom().nextInt(20) + gun.getCooldownTime();
                ammoCount = GunItem.getCurrentAmmoCount(this.mob.getItemInHand(InteractionHand.MAIN_HAND));
                if (this.mob instanceof CrossbowAttackMob crossbowAttackMob) {
                    crossbowAttackMob.setChargingCrossbow(false);
                }
            }

        } else if (this.gunState == GunAttackGoal.GunState.CHARGED) {
            if (--this.attackDelay <= 0) {
                this.gunState = GunAttackGoal.GunState.READY_TO_ATTACK;
            }
        } else if (this.gunState == GunAttackGoal.GunState.READY_TO_ATTACK && (flag || isStunned) && isRightAngle()) {
            InteractionHand interactionhand = ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof GunItem);
            ItemStack itemstack = this.mob.getItemInHand(interactionhand);
            if (itemstack.getItem() instanceof GunItem gunItem) {
                float inaccuracy = gunItem.getInaccuracy();
                if (isStunned) {
                    inaccuracy += 8;
                }
                GunItem.performShooting(this.mob.level(), this.mob, interactionhand, itemstack, inaccuracy);
                ammoCount -= 1;
                ItemStack itemstack1 = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof GunItem));
                if (ammoCount <= 0) {
                    GunItem.setCharged(itemstack1, false);
                    this.gunState = GunAttackGoal.GunState.UNCHARGED;
                } else {
                    this.gunState = GunAttackGoal.GunState.CHARGED;
                    if (itemstack1.getItem() instanceof GunItem gun) {
                        this.attackDelay = gun.getCooldownTime();
                    }

                }
            }
//            this.mob.onCrossbowAttackPerformed();
        }


    }

    private boolean canRun() {
        return this.gunState != GunState.CHARGING;
    }

    public double vectorDegreeCalculate(Vec3 vec1, Vec3 vec2) {
        double cos = vec1.dot(vec2) / vec1.length() / vec2.length();
        return Math.toDegrees(Math.acos(cos));
    }

    enum GunState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}

