package com.scarasol.pillagers_gun.item.gun;

import com.scarasol.pillagers_gun.compat.sbw.SbwCompat;
import com.scarasol.pillagers_gun.compat.tacz.TaczCompat;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.init.PillagersGunItems;
import com.scarasol.pillagers_gun.init.PillagersGunSounds;
import com.scarasol.pillagers_gun.item.ammo.AmmoItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.sounds.SoundEvent;

import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

public class ShotgunItem extends GunItem {
    private static final int MAX_CHARGE_DURATION = 110;
    private static final int AMMO_COUNT = 7;
    private static final int COOLDOWN = 20;


    public ShotgunItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON));
    }

    @Override
    public SoundEvent getFireSound() {
        return PillagersGunSounds.shotgun_fire.get();
    }

    @Override
    public SoundEvent getReloadSound() {
        return PillagersGunSounds.shotgun_reload.get();
    }


    @Override
    public AmmoItem getAmmo() {
        return (AmmoItem) PillagersGunItems.SHOTGUN_AMMO.get();
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
        return CommonConfig.SHOTGUN_COUNT.get();
    }

    @Override
    public int getInaccuracy() {
        return CommonConfig.SHOTGUN_INACCURACY.get();
    }

    @Override
    public boolean shouldRenderLaser() {
        return CommonConfig.SHOTGUN_RENDER_LASER.get();
    }

}
