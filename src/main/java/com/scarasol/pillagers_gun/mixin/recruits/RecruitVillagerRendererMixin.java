package com.scarasol.pillagers_gun.mixin.recruits;

import com.scarasol.pillagers_gun.item.gun.GunItem;
import com.talhanation.recruits.client.render.RecruitVillagerRenderer;
import com.talhanation.recruits.entities.AbstractInventoryEntity;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecruitVillagerRenderer.class)
public abstract class RecruitVillagerRendererMixin extends MobRenderer<AbstractRecruitEntity, HumanoidModel<AbstractRecruitEntity>> {

    public RecruitVillagerRendererMixin(EntityRendererProvider.Context context, HumanoidModel<AbstractRecruitEntity> humanoidModel, float f1) {
        super(context, humanoidModel, f1);
    }

    @Inject(method = "getArmPose", at = @At("RETURN"), cancellable = true, remap = false)
    private static void OnGetArmPose(AbstractInventoryEntity recruit, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir){
        if ((recruit.getItemInHand(hand).getItem() instanceof GunItem) && !recruit.swinging) {
            if (GunItem.isCharged(recruit.getItemInHand(hand))) {
                cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
            } else if (recruit.isUsingItem()) {
                cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_CHARGE);
            }
        }else if ("zombiekit:flamethrower".equals(ForgeRegistries.ITEMS.getKey(recruit.getItemInHand(hand).getItem()).toString())&& !recruit.swinging) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        }
    }
}
