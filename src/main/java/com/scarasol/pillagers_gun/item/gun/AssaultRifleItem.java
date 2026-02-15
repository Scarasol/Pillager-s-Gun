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


public class AssaultRifleItem extends GunItem {
    private static final int MAX_CHARGE_DURATION = 60;

    public AssaultRifleItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON));
    }

    @Override
    public SoundEvent getFireSound(){
        return CommonConfig.AUTOMATIC_SHOOTING.get() ? PillagersGunSounds.assault_rifle_fire.get() : PillagersGunSounds.assault_rifle_fire_three_round.get();
    }

    @Override
    public SoundEvent getReloadSound(){
        return PillagersGunSounds.assault_rifle_reload.get();
    }

    @Override
    public AmmoItem getAmmo(){
        return (AmmoItem) PillagersGunItems.ASSAULT_RIFLE_AMMO.get();
    }

    @Override
    public int getAmmoCount() {
        return CommonConfig.AUTOMATIC_SHOOTING.get() ? 30 : 10;
    }

    @Override
    public int getMaxChargeDuration() {
        return MAX_CHARGE_DURATION;
    }

    @Override
    public int getCooldownTime() {
        return CommonConfig.AUTOMATIC_SHOOTING.get() ? 2 : 20;
    }

    @Override
    public int getShotCount() {
        return CommonConfig.AUTOMATIC_SHOOTING.get() ? 1 : 3;
    }

    @Override
    public int getInaccuracy(LivingEntity target) {
        return CommonConfig.ASSAULT_INACCURACY.get();
    }

    @Override
    public boolean shouldRenderLaser() {
        return CommonConfig.ASSAULT_RENDER_LASER.get();
    }
}

