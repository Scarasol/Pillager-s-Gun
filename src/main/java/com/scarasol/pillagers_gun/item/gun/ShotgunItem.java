package com.scarasol.pillagers_gun.item.gun;

import com.scarasol.pillagers_gun.init.PillagersGunItems;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.minecraftforge.registries.ForgeRegistries;

public class ShotgunItem extends GunItem {
    private static final int MAX_CHARGE_DURATION = 110;
    private static final int AMMO_COUNT = 7;
    private static final int COOLDOWN = 20;
    private static final int SHOT_COUNT = 1;


    public ShotgunItem() {
        super(new Item.Properties().tab(null).stacksTo(1).rarity(Rarity.COMMON));
    }

    @Override
    public SoundEvent getFireSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("pillagers_gun:shotgun_fire"));
    }

    @Override
    public SoundEvent getReloadSound() {
        return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("pillagers_gun:shotgun_reload"));
    }


    @Override
    public Item getAmmo() {
        return PillagersGunItems.SHOTGUN_AMMO.get();
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
