package com.scarasol.pillagers_gun.item.ammo;

import com.scarasol.pillagers_gun.entity.projectile.AssaultRifleAmmoEntity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AssaultRifleAmmoItem extends ArrowItem {
    public AssaultRifleAmmoItem() {
        super(new Item.Properties().tab(null).stacksTo(64).rarity(Rarity.COMMON));
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity livingEntity) {
        return new AssaultRifleAmmoEntity(level, livingEntity);
    }
}
