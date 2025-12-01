package com.scarasol.pillagers_gun.init;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PillagersGunDamageTypes {

    public static final ResourceKey<DamageType> AMMO = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("pillagers_gun:ammo"));
    public static final ResourceKey<DamageType> AMMO_BYPASS_ARMOR = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("pillagers_gun:ammo_bypass_armor"));

    public static DamageSource damageSource(Level level, ResourceKey<DamageType> resourceKey, @Nullable Entity direct, @Nullable Entity owner){
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(resourceKey), direct, owner);
    }
}
