package com.scarasol.pillagers_gun.mixin.guardvillagers;

import com.scarasol.pillagers_gun.item.gun.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestegg.guardvillagers.client.renderer.GuardRenderer;
import tallestegg.guardvillagers.entities.Guard;

@Mixin(GuardRenderer.class)
public abstract class GuardRendererMixin extends HumanoidMobRenderer<Guard, HumanoidModel<Guard>> {

    public GuardRendererMixin(EntityRendererProvider.Context context, HumanoidModel<Guard> guardHumanoidModel, float f1) {
        super(context, guardHumanoidModel, f1);
    }

    @Inject(method = "getArmPose", at = @At("RETURN"), cancellable = true, remap = false)
    private void OnGetArmPose(Guard entityIn, ItemStack itemStackMain, ItemStack itemStackOff, InteractionHand handIn, CallbackInfoReturnable<HumanoidModel.ArmPose> cir){
        if (!entityIn.isUsingItem() && entityIn.getItemInHand(handIn).getItem() instanceof GunItem && GunItem.isCharged(entityIn.getItemInHand(handIn)) && !entityIn.swinging) {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_HOLD);
        }
    }


}
