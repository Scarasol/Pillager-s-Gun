package com.scarasol.pillagers_gun.compat.sbw;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.google.common.collect.Maps;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.entity.goal.controller.EmptyGunController;
import com.scarasol.pillagers_gun.entity.goal.controller.GunController;
import com.scarasol.pillagers_gun.init.PillagersGunItems;
import com.scarasol.pillagers_gun.util.WeightedRandom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
    public static final Map<String, Double> GUN_INACCURACY = Maps.newHashMap();
    public static final Map<ResourceLocation, Double> SPAWN_GUN = Maps.newHashMap();
    private static boolean spawnGunLoaded = false;

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

    public static boolean spawnWithSbwGun(Mob mob) {
        loadSpawnGunConfig();
        Optional<ResourceLocation> gunId = WeightedRandom.pick(mob.getRandom(), getSpawnGunEntries());
        if (gunId.isEmpty()) {
            return false;
        }

        Item item = ForgeRegistries.ITEMS.getValue(gunId.get());
        if (!(item instanceof GunItem)) {
            return false;
        }

        ItemStack itemStack = new ItemStack(item);
        GunData data = GunData.from(itemStack);
        data.initialize();
        int magazine = Math.max(0, data.get(GunProp.MAGAZINE));
        if (CommonConfig.SBW_GUNNERS_NEED_AMMO.get()) {
            if (magazine > 0) {
                data.ammo.set(mob.getRandom().nextInt(magazine + 1));
            }
            data.virtualAmmo.set(getSpawnBackupAmmo(mob.getRandom(), data, magazine));
        } else if (magazine > 0) {
            data.ammo.set(magazine);
        }
        data.save();

        mob.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        mob.setDropChance(EquipmentSlot.MAINHAND, CommonConfig.DROP_CHANCE.get().floatValue());
        return true;
    }

    public static boolean isSbwGun(ItemStack itemStack) {
        return itemStack.getItem() instanceof GunItem;
    }

    public static String getGunType(ItemStack itemStack) {
        if (!isSbwGun(itemStack)) {
            return "";
        }
        Object gunType = GunData.from(itemStack).get(GunProp.GUN_TYPE);
        return gunType == null ? "" : gunType.toString().toLowerCase(Locale.ROOT);
    }

    public static boolean isSniperGun(ItemStack itemStack) {
        return "sniper".equals(getGunType(itemStack));
    }

    public static double getInaccuracy(String type, double originalValue) {
        if (GUN_INACCURACY.isEmpty()) {
            for (String info : CommonConfig.SBW_GUN_INACCURACY.get()) {
                String[] inaccuracy = info.trim().split(",");
                if (inaccuracy.length < 2) {
                    continue;
                }
                try {
                    GUN_INACCURACY.put(inaccuracy[0].trim().toLowerCase(Locale.ROOT), Double.parseDouble(inaccuracy[1].trim()));
                } catch (RuntimeException ignored) {
                }
            }
        }
        return GUN_INACCURACY.getOrDefault(type.toLowerCase(Locale.ROOT), originalValue);
    }

    public static boolean shouldRenderLaser(ItemStack itemStack) {
        return CommonConfig.SBW_RENDER_LASER.get().contains(getGunType(itemStack));
    }

    public static GunController createGunController(Mob mob) {
        GunController controller = new SbwGunController(mob);
        return controller.isValid() ? controller : EmptyGunController.INSTANCE;
    }

    private static int getSpawnBackupAmmo(RandomSource random, GunData data, int magazine) {
        double ammoMultiplier = nextDouble(random, CommonConfig.SBW_GUN_MIN_AMMO.get(), CommonConfig.SBW_GUN_MAX_AMMO.get());
        int ammoUnit = data.useBackpackAmmo() ? Math.max(1, data.get(GunProp.AMMO_COST_PER_SHOOT)) : Math.max(1, magazine);
        return Math.max(0, (int) Math.round(ammoMultiplier * ammoUnit));
    }

    private static void loadSpawnGunConfig() {
        if (spawnGunLoaded) {
            return;
        }
        spawnGunLoaded = true;
        SPAWN_GUN.clear();
        for (String info : CommonConfig.SBW_GUN_TYPE.get()) {
            String[] gunType = info.trim().split(",");
            if (gunType.length < 2) {
                continue;
            }
            try {
                double weight = Double.parseDouble(gunType[1].trim());
                if (weight > 0 && Double.isFinite(weight)) {
                    ResourceLocation gunId = new ResourceLocation(gunType[0].trim());
                    SPAWN_GUN.put(gunId, weight);
                }
            } catch (RuntimeException ignored) {
            }
        }
    }

    private static List<WeightedRandom.Entry<ResourceLocation>> getSpawnGunEntries() {
        List<WeightedRandom.Entry<ResourceLocation>> entries = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Double> entry : SPAWN_GUN.entrySet()) {
            entries.add(WeightedRandom.entry(entry.getKey(), entry.getValue()));
        }
        return entries;
    }

    private static double nextDouble(RandomSource random, double min, double max) {
        double lower = Math.min(min, max);
        double upper = Math.max(min, max);
        return lower + random.nextDouble() * (upper - lower);
    }
}
