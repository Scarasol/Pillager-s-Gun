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

public class PistolItem extends GunItem {
    private static final int MAX_CHARGE_DURATION = 40;
    public static final int AMMO_COUNT = 8;
    private static final int COOLDOWN = 10;
    private static final int SHOT_COUNT = 1;

    public PistolItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON));
    }

    @Override
    public SoundEvent getFireSound(){
        return PillagersGunSounds.pistol_fire.get();
    }

    @Override
    public SoundEvent getReloadSound(){
        return PillagersGunSounds.pistol_reload.get();
    }

    @Override
    public AmmoItem getAmmo(){
        return (AmmoItem) PillagersGunItems.PISTOL_AMMO.get();
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

    @Override
    public int getInaccuracy(LivingEntity target) {
        return CommonConfig.PISTOL_INACCURACY.get();
    }

    @Override
    public boolean shouldRenderLaser() {
        return CommonConfig.PISTOL_RENDER_LASER.get();
    }
}

