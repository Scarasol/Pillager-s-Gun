package com.scarasol.pillagers_gun.mixin.tacz;

import com.tacz.guns.client.animation.third.InnerThirdPersonManager;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(IllagerModel.class)
public abstract class IllagerModelMixin<T extends AbstractIllager> extends HierarchicalModel<T> implements ArmedModel, HeadedModel {

    @Shadow
    @Final
    private ModelPart head;

    private ModelPart body;

    @Shadow
    @Final
    private ModelPart leftArm;
    @Shadow
    @Final
    private ModelPart rightArm;

    @Shadow @Final private ModelPart arms;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void pillagersGun$getBody(ModelPart modelPart, CallbackInfo ci) {
        this.body = modelPart.getChild("body");
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/AbstractIllager;FFFFF)V", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/AbstractIllager;getArmPose()Lnet/minecraft/world/entity/monster/AbstractIllager$IllagerArmPose;"))
    private void pillagersGun$setRotationAnglesHead(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if ("tacz".equals(ForgeRegistries.ITEMS.getKey(entityIn.getMainHandItem().getItem()).getNamespace())) {
            this.rightArm.z = 0.0F;
            this.rightArm.x = -5.0F;
            this.leftArm.z = 0.0F;
            this.leftArm.x = 5.0F;
            InnerThirdPersonManager.setRotationAnglesHead(entityIn, this.rightArm, this.leftArm, this.body, this.head, limbSwingAmount);
            this.arms.visible = false;
            this.leftArm.visible = true;
            this.rightArm.visible = true;
            ci.cancel();
        }
    }
}
