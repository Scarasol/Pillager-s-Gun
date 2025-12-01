package com.scarasol.pillagers_gun.init;

import com.scarasol.pillagers_gun.client.model.BulletModel;
import com.scarasol.pillagers_gun.client.model.RocketModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class PillagersGunModels {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BulletModel.LAYER_LOCATION, BulletModel::createBodyLayer);
        event.registerLayerDefinition(RocketModel.LAYER_LOCATION, RocketModel::createBodyLayer);
    }

}
