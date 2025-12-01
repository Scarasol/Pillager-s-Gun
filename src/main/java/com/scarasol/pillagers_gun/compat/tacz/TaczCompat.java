package com.scarasol.pillagers_gun.compat.tacz;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.scarasol.pillagers_gun.PillagersGunMod;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.event.EventHandler;
import com.scarasol.pillagers_gun.init.PillagersGunItems;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.FeedType;
import com.tacz.guns.sound.SoundManager;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class TaczCompat {

    public static final ItemStack SHOTGUN;
    public static final ItemStack ASSAULT_RIFLE;
    public static final ItemStack PISTOL;
    public static final ItemStack SNIPERS_RIFLE;
    public static final ItemStack BAZOOKA;

    public static final Map<Item, ItemStack> GUN_SWITCH = Maps.newHashMap();
    public static final Map<String, Double> GUN_INACCURACY = Maps.newHashMap();
    public static final Map<ResourceLocation, Double> SPAWN_GUN = Maps.newHashMap();
    public static double totalWeight = 0;

    static {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
        SHOTGUN = new ItemStack(item);
        ASSAULT_RIFLE = new ItemStack(item);
        PISTOL = new ItemStack(item);
        SNIPERS_RIFLE = new ItemStack(item);
        BAZOOKA = new ItemStack(item);
        if (item instanceof IGun gun) {
            gun.setGunId(PISTOL, new ResourceLocation("tacz:m1911"));
            gun.setGunId(SHOTGUN, new ResourceLocation("tacz:m870"));
            gun.setGunId(ASSAULT_RIFLE, new ResourceLocation("tacz:ak47"));
            gun.setGunId(SNIPERS_RIFLE, new ResourceLocation("tacz:ai_awp"));
            gun.setGunId(BAZOOKA, new ResourceLocation("tacz:rpg7"));
        }
        GUN_SWITCH.put(PillagersGunItems.SHOTGUN.get(), SHOTGUN);
        GUN_SWITCH.put(PillagersGunItems.ASSAULT_RIFLE.get(), ASSAULT_RIFLE);
        GUN_SWITCH.put(PillagersGunItems.PISTOL.get(), PISTOL);
        GUN_SWITCH.put(PillagersGunItems.SNIPERS_RIFLE.get(), SNIPERS_RIFLE);
        GUN_SWITCH.put(PillagersGunItems.BAZOOKA.get(), BAZOOKA);
    }

    public static void playSound(ItemStack itemStack, LivingEntity shooter) {
        if (itemStack.getItem() instanceof IGun gun) {
            String soundId = SoundManager.SHOOT_3P_SOUND;
            SoundManager.sendSoundToNearby(shooter, 64, gun.getGunId(itemStack), gun.getGunDisplayId(itemStack), soundId, 0.8F, 0.9F + shooter.getRandom().nextFloat() * 0.125F);
        }
    }

    public static ItemStack itemStackSwitch(ItemStack itemStack) {
        return GUN_SWITCH.getOrDefault(itemStack.getItem(), itemStack);
    }

    public static void changeEquipmentTo(LivingEquipmentChangeEvent event) {
        zoomAttributeModifier(event.getTo(), event.getEntity());
    }

    public static double getZoomAttribute(ItemStack itemStack) {
        IGun gun = IGun.getIGunOrNull(itemStack);
        if (gun != null) {
            return gun.getAimingZoom(itemStack) / 2f - 1;
        }
        return 0;
    }

    public static void zoomAttributeModifier(ItemStack itemStack, LivingEntity shooter) {
        IGun gun = IGun.getIGunOrNull(itemStack);
        if (gun != null) {
            float aimingZoom;
            try {
                aimingZoom = gun.getAimingZoom(itemStack) / 2f - 1;
            } catch (NoSuchMethodError e) {
                aimingZoom = 0;
            }
            AttributeInstance attributeInstance = shooter.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
            if (attributeInstance != null) {
                attributeInstance.removeModifier(EventHandler.ATTRIBUTE_MODIFIER_UUID);
                if (aimingZoom > 0) {
                    AttributeModifier attributeModifier = new AttributeModifier(EventHandler.ATTRIBUTE_MODIFIER_UUID, "tacz", aimingZoom, AttributeModifier.Operation.MULTIPLY_BASE);
                    attributeInstance.addPermanentModifier(attributeModifier);
                }
            }
        }
    }

    public static String getGunType(ItemStack gunItem) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return "";
        }
        return TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem))
                .map(CommonGunIndex::getType)
                .orElse("");
    }


    public static double getInaccuracy(String type, double originalValue) {
        if (GUN_INACCURACY.isEmpty()) {
            for (String info : CommonConfig.TACZ_GUN_INACCURACY.get()) {
                String[] inaccuracy = info.trim().split(",");
                if (inaccuracy.length < 2) {
                    continue;
                }
                GUN_INACCURACY.put(inaccuracy[0], Double.parseDouble(inaccuracy[1]));
            }
        }
        return GUN_INACCURACY.getOrDefault(type, originalValue);
    }

    public static boolean spawnWithTaczGun(Mob mob) {
        if (SPAWN_GUN.isEmpty()) {
            for (String info : CommonConfig.TACZ_GUN_TYPE.get()) {
                String[] gunType = info.trim().split(",");
                if (gunType.length < 2) {
                    continue;
                }
                totalWeight += Double.parseDouble(gunType[1]);
                SPAWN_GUN.put(new ResourceLocation(gunType[0]), Double.parseDouble(gunType[1]));
            }
        }
        if (totalWeight > 0) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz:modern_kinetic_gun"));
            if (item instanceof IGun iGun) {
                ItemStack itemStack = new ItemStack(item);
                Random random = new Random();
                double weight = random.nextDouble() * totalWeight;
                for (Map.Entry<ResourceLocation, Double> entry : SPAWN_GUN.entrySet()) {
                    if (weight <= entry.getValue()) {
                        return TimelessAPI.getCommonGunIndex(entry.getKey()).map((commonGunIndex -> {
                            iGun.setGunId(itemStack, entry.getKey());
                            int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(itemStack, commonGunIndex.getGunData());
                            iGun.setCurrentAmmoCount(itemStack, random.nextInt(0, maxAmmoCount));
                            iGun.setFireMode(itemStack, commonGunIndex.getGunData().getFireModeSet().get(0));
                            iGun.setBulletInBarrel(itemStack, true);
                            if (CommonConfig.TACZ_GUNNERS_NEED_AMMO.get()) {
                                if (iGun.useInventoryAmmo(itemStack)) {
                                    iGun.setDummyAmmoAmount(itemStack, (int) (random.nextDouble(CommonConfig.GUARD_TACZ_GUN_MIN_AMMO.get(), CommonConfig.GUARD_TACZ_GUN_MAX_AMMO.get()) * 40));
                                } else if (commonGunIndex.getGunData().getReloadData().getType() == FeedType.FUEL) {
                                    iGun.setDummyAmmoAmount(itemStack, (int) (random.nextDouble(CommonConfig.GUARD_TACZ_GUN_MIN_AMMO.get(), CommonConfig.GUARD_TACZ_GUN_MAX_AMMO.get())));
                                }else {
                                    iGun.setDummyAmmoAmount(itemStack, (int) (random.nextDouble(CommonConfig.GUARD_TACZ_GUN_MIN_AMMO.get(), CommonConfig.GUARD_TACZ_GUN_MAX_AMMO.get()) * maxAmmoCount));
                                }
                            }
                            mob.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                            double dropChance = CommonConfig.DROP_CHANCE.get();
                            mob.setDropChance(EquipmentSlot.MAINHAND, (float) dropChance);
                            return true;
                        })).orElse(false);
                    }
                    weight -= entry.getValue();
                }
            }
        }

        return false;
    }

    public static boolean shouldRenderLaser(ItemStack gunItem) {

        return CommonConfig.TACZ_RENDER_LASER.get().contains(getGunType(gunItem));

    }
}
