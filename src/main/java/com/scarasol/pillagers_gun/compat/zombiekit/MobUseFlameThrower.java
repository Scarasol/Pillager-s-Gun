package com.scarasol.pillagers_gun.compat.zombiekit;

import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.zombiekit.init.ZombieKitItems;
import com.scarasol.zombiekit.item.weapon.Flamethrower;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class MobUseFlameThrower {

    public static void makeMobsUseFlameThrower(LivingEntity livingEntity) {
        Flamethrower flamethrower = (Flamethrower) ZombieKitItems.FLAMETHROWER.get();
        ItemStack itemStack = new ItemStack(ZombieKitItems.FLAMETHROWER.get());
        ItemStack fuelCanister;
        double total = CommonConfig.FUEL_CANISTER_CHANCE.get() + CommonConfig.NAPALM_FUEL_CANISTER_CHANCE.get() + CommonConfig.HIGH_TEMPERATURE_CANISTER_CHANCE.get();
        double chance = livingEntity.getRandom().nextDouble() * total;
        if (chance < CommonConfig.FUEL_CANISTER_CHANCE.get())
            fuelCanister = new ItemStack(ZombieKitItems.FUEL_CANISTER.get());
        else if (chance < CommonConfig.FUEL_CANISTER_CHANCE.get() + CommonConfig.NAPALM_FUEL_CANISTER_CHANCE.get())
            fuelCanister = new ItemStack(ZombieKitItems.NAPALM_CANISTER.get());
        else
            fuelCanister = new ItemStack(ZombieKitItems.HIGH_TEMPERATURE_CANISTER.get());
        fuelCanister.setDamageValue(livingEntity.getRandom().nextInt(fuelCanister.getMaxDamage()));
        flamethrower.putCanister(itemStack, fuelCanister);
        flamethrower.changeTexture(itemStack);
        livingEntity.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
    }
}
