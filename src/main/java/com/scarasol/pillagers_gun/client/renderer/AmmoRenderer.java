package com.scarasol.pillagers_gun.client.renderer;

import com.scarasol.pillagers_gun.client.model.AmmoModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.projectile.AbstractArrow;



public class AmmoRenderer extends EntityRenderer<AbstractArrow>{
    private static final ResourceLocation texture = new ResourceLocation("pillagers_gun:textures/entities/ammo.png");
    private final AmmoModel model;

    public AmmoRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new AmmoModel(context.bakeLayer(AmmoModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractArrow AmmoEntityProcedure) {
        return texture;
    }
}


