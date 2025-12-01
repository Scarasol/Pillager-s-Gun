package com.scarasol.pillagers_gun.item.ammo;

import com.scarasol.pillagers_gun.entity.projectile.RocketEntity;
import com.scarasol.pillagers_gun.entity.projectile.SnipersRifleAmmoEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class RocketItem extends AmmoItem {
    public RocketItem() {
        super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity livingEntity) {
        return new RocketEntity(level, livingEntity);
    }

    @Override
    public float getSpeed() {
        return 2;
    }
}
