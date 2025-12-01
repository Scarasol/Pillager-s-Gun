package com.scarasol.pillagers_gun.compat.sbw;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.google.common.collect.Maps;
import com.scarasol.pillagers_gun.init.PillagersGunItems;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * @author Scarasol
 */
public class SbwCompat {
    public static final ItemStack SHOTGUN = new ItemStack(ModItems.M_870.get());
    public static final ItemStack ASSAULT_RIFLE = new ItemStack(ModItems.AK_47.get());
    public static final ItemStack PISTOL = new ItemStack(ModItems.M_1911.get());
    public static final ItemStack SNIPERS_RIFLE = new ItemStack(ModItems.M_98B.get());
    public static final ItemStack BAZOOKA = new ItemStack(ModItems.RPG.get());

    public static final Map<Item, ItemStack> GUN_SWITCH = Maps.newHashMap();

    static {
        GUN_SWITCH.put(PillagersGunItems.SHOTGUN.get(), SHOTGUN);
        GUN_SWITCH.put(PillagersGunItems.ASSAULT_RIFLE.get(), ASSAULT_RIFLE);
        GUN_SWITCH.put(PillagersGunItems.PISTOL.get(), PISTOL);
        GUN_SWITCH.put(PillagersGunItems.SNIPERS_RIFLE.get(), SNIPERS_RIFLE);
        GUN_SWITCH.put(PillagersGunItems.BAZOOKA.get(), BAZOOKA);
    }

    public static void playSound(ItemStack itemStack, LivingEntity shooter) {
        GunData data = GunData.from(itemStack);
        if (itemStack.getItem() instanceof GunItem gunItem) {
            gunItem.playFireSounds(data, shooter, true);
        }
    }

    public static ItemStack itemStackSwitch(ItemStack itemStack) {
        return GUN_SWITCH.getOrDefault(itemStack.getItem(), itemStack);
    }
}
