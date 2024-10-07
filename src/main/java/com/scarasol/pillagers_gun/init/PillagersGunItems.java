package com.scarasol.pillagers_gun.init;

import com.scarasol.pillagers_gun.PillagersGunMod;
import com.scarasol.pillagers_gun.item.ammo.AssaultRifleAmmoItem;
import com.scarasol.pillagers_gun.item.ammo.PistolAmmoItem;
import com.scarasol.pillagers_gun.item.ammo.ShotgunAmmoItem;
import com.scarasol.pillagers_gun.item.gun.AssaultRifleItem;
import com.scarasol.pillagers_gun.item.gun.PistolItem;
import com.scarasol.pillagers_gun.item.gun.ShotgunItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PillagersGunItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, PillagersGunMod.MODID);
    public static final RegistryObject<Item> SHOTGUN = REGISTRY.register("shotgun", ShotgunItem::new);
    public static final RegistryObject<Item> SHOTGUN_AMMO = REGISTRY.register("shotgun_ammo", ShotgunAmmoItem::new);
    public static final RegistryObject<Item> PISTOL = REGISTRY.register("pistol", PistolItem::new);
    public static final RegistryObject<Item> PISTOL_AMMO = REGISTRY.register("pistol_ammo", PistolAmmoItem::new);
    public static final RegistryObject<Item> ASSAULT_RIFLE = REGISTRY.register("assault_rifle", AssaultRifleItem::new);
    public static final RegistryObject<Item> ASSAULT_RIFLE_AMMO = REGISTRY.register("assault_rifle_ammo", AssaultRifleAmmoItem::new);
}

