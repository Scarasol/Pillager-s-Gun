package com.scarasol.pillagers_gun.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.scarasol.pillagers_gun.compat.sbw.SbwCompat;
import com.scarasol.pillagers_gun.compat.tacz.TaczCompat;
import com.scarasol.pillagers_gun.config.CommonConfig;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Scarasol
 */
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {
    @Shadow protected abstract void renderArmWithItem(LivingEntity p_117185_, ItemStack p_117186_, ItemDisplayContext p_270970_, HumanoidArm p_117188_, PoseStack p_117189_, MultiBufferSource p_117190_, int p_117191_);

    public ItemInHandLayerMixin(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }


    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/ItemInHandLayer;renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void renderItemStack(ItemInHandLayer<T, M> instance, LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (CommonConfig.GUN_MODEL.get()) {
            if (ModList.get().isLoaded("superbwarfare")) {
                if (!ModList.get().isLoaded("tacz") || livingEntity.getId() % 2 == 0) {
                    itemStack = SbwCompat.itemStackSwitch(itemStack);
                } else {
                    itemStack = TaczCompat.itemStackSwitch(itemStack);
                }
            } else if (ModList.get().isLoaded("tacz")) {
                itemStack = TaczCompat.itemStackSwitch(itemStack);
            }
        }
        renderArmWithItem(livingEntity, itemStack, itemDisplayContext, humanoidArm, poseStack, multiBufferSource, packedLight);
    }
}
