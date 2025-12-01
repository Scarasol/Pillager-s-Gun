package com.scarasol.pillagers_gun.mixin;

import com.scarasol.pillagers_gun.init.PillagersGunItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin<T extends LivingEntity> extends TargetGoal {

    @Shadow protected TargetingConditions targetConditions;

    public NearestAttackableTargetGoalMixin(Mob mob, boolean see) {
        super(mob, see);
    }

    @Inject(method = "getTargetSearchArea", cancellable = true, at = @At("RETURN"))
    private void onGetTargetSearchArea(double range, CallbackInfoReturnable<AABB> cir) {
        if (this.mob.getMainHandItem().is(PillagersGunItems.SNIPERS_RIFLE.get())) {
            cir.setReturnValue(this.mob.getBoundingBox().inflate(range, getFollowDistance(), range));
        }
    }

    @Inject(method = "findTarget", at = @At("HEAD"))
    private void onFindTarget(CallbackInfo ci) {
        this.targetConditions = this.targetConditions.range(getFollowDistance());
    }
}
