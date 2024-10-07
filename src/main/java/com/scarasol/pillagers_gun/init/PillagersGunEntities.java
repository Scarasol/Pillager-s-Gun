package com.scarasol.pillagers_gun.init;

import com.scarasol.pillagers_gun.PillagersGunMod;
import com.scarasol.pillagers_gun.entity.projectile.AssaultRifleAmmoEntity;
import com.scarasol.pillagers_gun.entity.projectile.PistolAmmoEntity;
import com.scarasol.pillagers_gun.entity.projectile.ShotgunAmmoEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PillagersGunEntities {

    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, PillagersGunMod.MODID);
    public static final RegistryObject<EntityType<ShotgunAmmoEntity>> SHOTGUN_AMMO = register("shotgun_ammo", EntityType.Builder.<ShotgunAmmoEntity>of(ShotgunAmmoEntity::new, MobCategory.MISC).setCustomClientFactory(ShotgunAmmoEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
    public static final RegistryObject<EntityType<PistolAmmoEntity>> PISTOL_AMMO = register("pistol_ammo", EntityType.Builder.<PistolAmmoEntity>of(PistolAmmoEntity::new, MobCategory.MISC).setCustomClientFactory(PistolAmmoEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
    public static final RegistryObject<EntityType<AssaultRifleAmmoEntity>> ASSAULT_RIFLE_AMMO = register("assault_rifle_ammo", EntityType.Builder.<AssaultRifleAmmoEntity>of(AssaultRifleAmmoEntity::new, MobCategory.MISC).setCustomClientFactory(AssaultRifleAmmoEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
    }
}
