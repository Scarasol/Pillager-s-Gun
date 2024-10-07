package com.scarasol.pillagers_gun.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PillagersGunSounds {
    public static Map<ResourceLocation, SoundEvent> REGISTRY = new HashMap<>();
    static {
        REGISTRY.put(new ResourceLocation("pillagers_gun", "bullet_hit_body"), new SoundEvent(new ResourceLocation("pillagers_gun", "bullet_hit_body")));
        REGISTRY.put(new ResourceLocation("pillagers_gun", "bullet_hit_ground"), new SoundEvent(new ResourceLocation("pillagers_gun", "bullet_hit_ground")));
        REGISTRY.put(new ResourceLocation("pillagers_gun", "shotgun_fire"), new SoundEvent(new ResourceLocation("pillagers_gun", "shotgun_fire")));
        REGISTRY.put(new ResourceLocation("pillagers_gun", "shotgun_reload"), new SoundEvent(new ResourceLocation("pillagers_gun", "shotgun_reload")));
        REGISTRY.put(new ResourceLocation("pillagers_gun", "pistol_fire"), new SoundEvent(new ResourceLocation("pillagers_gun", "pistol_fire")));
        REGISTRY.put(new ResourceLocation("pillagers_gun", "assault_rifle_fire"), new SoundEvent(new ResourceLocation("pillagers_gun", "assault_rifle_fire")));
        REGISTRY.put(new ResourceLocation("pillagers_gun", "assault_rifle_reload"), new SoundEvent(new ResourceLocation("pillagers_gun", "assault_rifle_reload")));
        REGISTRY.put(new ResourceLocation("pillagers_gun", "pistol_reload"), new SoundEvent(new ResourceLocation("pillagers_gun", "pistol_reload")));
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        for (Map.Entry<ResourceLocation, SoundEvent> sound : REGISTRY.entrySet())
            event.getRegistry().register(sound.getValue().setRegistryName(sound.getKey()));
    }

}
