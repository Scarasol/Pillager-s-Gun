package com.scarasol.pillagers_gun.item.gun;

import com.scarasol.pillagers_gun.init.PillagersGunItems;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.minecraftforge.registries.ForgeRegistries;


public class AssaultRifleItem extends GunItem {
    private static final int MAX_CHARGE_DURATION = 60;
    public static final int AMMO_COUNT = 10;
    private static final int COOLDOWN = 10;
    private static final int SHOT_COUNT = 3;

    public AssaultRifleItem() {
        super(new Item.Properties().tab(null).stacksTo(1).rarity(Rarity.COMMON));
    }

    @Override
    public SoundEvent getFireSound(){
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("pillagers_gun:assault_rifle_fire"));
    }

    @Override
    public SoundEvent getReloadSound(){
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("pillagers_gun:assault_rifle_reload"));
    }

    @Override
    public Item getAmmo(){
        return PillagersGunItems.ASSAULT_RIFLE_AMMO.get();
    }

    @Override
    public int getAmmoCount() {
        return AMMO_COUNT;
    }

    @Override
    public int getMaxChargeDuration() {
        return MAX_CHARGE_DURATION;
    }

    @Override
    public int getCooldownTime() {
        return COOLDOWN;
    }

    @Override
    public int getShotCount() {
        return SHOT_COUNT;
    }
}

