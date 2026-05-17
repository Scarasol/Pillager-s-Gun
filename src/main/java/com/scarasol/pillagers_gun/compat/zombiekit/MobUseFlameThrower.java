package com.scarasol.pillagers_gun.compat.zombiekit;

import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.util.WeightedRandom;
import com.scarasol.zombiekit.init.ZombieKitItems;
import com.scarasol.zombiekit.item.weapon.Flamethrower;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MobUseFlameThrower {

    public static void makeMobsUseFlameThrower(LivingEntity livingEntity) {
        Flamethrower flamethrower = (Flamethrower) ZombieKitItems.FLAMETHROWER.get();
        ItemStack itemStack = new ItemStack(ZombieKitItems.FLAMETHROWER.get());
        Item canister = WeightedRandom.pick(livingEntity.getRandom(), List.of(
                WeightedRandom.entry(ZombieKitItems.FUEL_CANISTER.get(), CommonConfig.FUEL_CANISTER_CHANCE.get()),
                WeightedRandom.entry(ZombieKitItems.NAPALM_CANISTER.get(), CommonConfig.NAPALM_FUEL_CANISTER_CHANCE.get()),
                WeightedRandom.entry(ZombieKitItems.HIGH_TEMPERATURE_CANISTER.get(), CommonConfig.HIGH_TEMPERATURE_CANISTER_CHANCE.get())
        )).orElse(ZombieKitItems.FUEL_CANISTER.get());
        ItemStack fuelCanister = new ItemStack(canister);
        fuelCanister.setDamageValue(livingEntity.getRandom().nextInt(fuelCanister.getMaxDamage()));
        flamethrower.putCanister(itemStack, fuelCanister);
        flamethrower.changeTexture(itemStack);
        livingEntity.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
    }
}
