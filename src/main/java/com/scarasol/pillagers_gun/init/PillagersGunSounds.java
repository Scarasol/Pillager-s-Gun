package com.scarasol.pillagers_gun.init;

import com.scarasol.pillagers_gun.PillagersGunMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class PillagersGunSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PillagersGunMod.MODID);
    public static final RegistryObject<SoundEvent> bullet_hit_body = REGISTRY.register("bullet_hit_body", () -> new SoundEvent(new ResourceLocation(PillagersGunMod.MODID, "bullet_hit_body")));
    public static final RegistryObject<SoundEvent> bullet_hit_ground = REGISTRY.register("bullet_hit_ground", () -> new SoundEvent(new ResourceLocation(PillagersGunMod.MODID, "bullet_hit_ground")));
    public static final RegistryObject<SoundEvent> shotgun_fire = REGISTRY.register("shotgun_fire", () -> new SoundEvent(new ResourceLocation(PillagersGunMod.MODID, "shotgun_fire")));
    public static final RegistryObject<SoundEvent> shotgun_reload = REGISTRY.register("shotgun_reload", () -> new SoundEvent(new ResourceLocation(PillagersGunMod.MODID, "shotgun_reload")));
    public static final RegistryObject<SoundEvent> pistol_fire = REGISTRY.register("pistol_fire", () -> new SoundEvent(new ResourceLocation(PillagersGunMod.MODID, "pistol_fire")));
    public static final RegistryObject<SoundEvent> assault_rifle_fire = REGISTRY.register("assault_rifle_fire", () -> new SoundEvent(new ResourceLocation(PillagersGunMod.MODID, "assault_rifle_fire")));
    public static final RegistryObject<SoundEvent> assault_rifle_reload = REGISTRY.register("assault_rifle_reload", () -> new SoundEvent(new ResourceLocation(PillagersGunMod.MODID, "assault_rifle_reload")));
    public static final RegistryObject<SoundEvent> pistol_reload = REGISTRY.register("pistol_reload", () -> new SoundEvent(new ResourceLocation(PillagersGunMod.MODID, "pistol_reload")));


}
