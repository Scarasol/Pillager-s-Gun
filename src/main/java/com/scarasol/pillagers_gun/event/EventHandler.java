package com.scarasol.pillagers_gun.event;


import com.scarasol.pillagers_gun.PillagersGunMod;
import com.scarasol.pillagers_gun.compat.guardvillagers.GuardUseGun;
import com.scarasol.pillagers_gun.compat.recruits.RecruitUseGun;
import com.scarasol.pillagers_gun.compat.tacz.TaczCompat;
import com.scarasol.pillagers_gun.compat.tacz.TaczGunAttackGoal;
import com.scarasol.pillagers_gun.compat.zombiekit.MobUseFlameThrower;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.scarasol.pillagers_gun.entity.projectile.Ammo;
import com.scarasol.pillagers_gun.entity.projectile.RocketEntity;
import com.scarasol.pillagers_gun.init.PillagersGunItems;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber
public class EventHandler {

    public static final UUID ATTRIBUTE_MODIFIER_UUID = UUID.fromString("6D4802D7-3BA3-EEB9-C88A-94AD48F68BFD");

    public static final TagKey<EntityType<?>> PILLAGER_GUNNER = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge:pillager_gunner"));

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Random random = new Random();
        if (entity == null) {
            return;
        }
        if (entity.getType().is(PILLAGER_GUNNER) && entity instanceof Mob mob) {
            mob.goalSelector.addGoal(1, new GunAttackGoal<>(mob, 1.0D, 64.0F));
            if (ModList.get().isLoaded("tacz") && CommonConfig.TACZ_GUN_USE.get()) {
                mob.goalSelector.addGoal(1, new TaczGunAttackGoal<>(mob, 1.0D));
            }
            mob.setLeftHanded(false);
            if (!event.loadedFromDisk()) {
                String id = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString();
                if (!id.contains("recruits") && !id.contains("guardvillagers")) {
                    if (mob.getItemInHand(InteractionHand.MAIN_HAND).is(Items.CROSSBOW) && random.nextDouble() <= CommonConfig.EQUIP_CHANCE.get()) {
                        if (!ModList.get().isLoaded("tacz") || !CommonConfig.TACZ_GUN_USE.get() || !CommonConfig.TACZ_GUN_SPAWN.get() || !TaczCompat.spawnWithTaczGun(mob)) {
                            double totalWeights = CommonConfig.ASSAULT_CHANCE.get() + CommonConfig.PISTOL_CHANCE.get() + CommonConfig.SHOTGUN_CHANCE.get() + CommonConfig.SNIPERS_RIFLE_CHANCE.get() + CommonConfig.BAZOOKA_CHANCE.get();
                            double i = random.nextDouble();
                            mob.setDropChance(EquipmentSlot.MAINHAND, CommonConfig.DROP_CHANCE.get().floatValue());
                            boolean flag = false;
                            Optional<? extends ModContainer> optional = ModList.get().getModContainerById("zombiekit");
                            if (optional.isPresent()) {
                                ModContainer modContainer = optional.get();
                                ArtifactVersion version = modContainer.getModInfo().getVersion();
                                flag = version.getMajorVersion() >= 2 && version.getMinorVersion() >= 1;
                            }
                            if (flag) {
                                totalWeights += CommonConfig.FLAMETHROWER_CHANCE.get();
                                if (i < CommonConfig.FLAMETHROWER_CHANCE.get() / totalWeights) {
                                    MobUseFlameThrower.makeMobsUseFlameThrower(mob);
                                    return;
                                }
                                totalWeights -= CommonConfig.FLAMETHROWER_CHANCE.get();
                            }
                            ItemStack itemStack;
                            if (i < CommonConfig.PISTOL_CHANCE.get() / totalWeights) {
                                itemStack = new ItemStack(PillagersGunItems.PISTOL.get());
                            } else if (i < (CommonConfig.PISTOL_CHANCE.get() + CommonConfig.ASSAULT_CHANCE.get()) / totalWeights) {
                                itemStack = new ItemStack(PillagersGunItems.ASSAULT_RIFLE.get());
                            } else if (i < (CommonConfig.PISTOL_CHANCE.get() + CommonConfig.ASSAULT_CHANCE.get() + CommonConfig.SHOTGUN_CHANCE.get()) / totalWeights) {
                                itemStack = new ItemStack(PillagersGunItems.SHOTGUN.get());
                            } else if (i < (CommonConfig.PISTOL_CHANCE.get() + CommonConfig.ASSAULT_CHANCE.get() + CommonConfig.SHOTGUN_CHANCE.get() + CommonConfig.SNIPERS_RIFLE_CHANCE.get()) / totalWeights) {
                                itemStack = new ItemStack(PillagersGunItems.SNIPERS_RIFLE.get());
                            } else {
                                itemStack = new ItemStack(PillagersGunItems.BAZOOKA.get());
                            }
                            GunItem.init(itemStack);
                            itemStack.setCount(1);
                            mob.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                        }
                    }
                    if (mob.getMainHandItem().is(PillagersGunItems.SNIPERS_RIFLE.get())) {
                        AttributeInstance attributeInstance = mob.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
                        if (attributeInstance != null) {
                            AttributeModifier attributeModifier = new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "sniper", CommonConfig.SNIPERS_RIFLE_BONUS.get(), AttributeModifier.Operation.MULTIPLY_BASE);
                            attributeInstance.removeModifier(ATTRIBUTE_MODIFIER_UUID);
                            attributeInstance.addPermanentModifier(attributeModifier);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void changeEquip(LivingEquipmentChangeEvent event) {
        LivingEntity mob = event.getEntity();
        if (mob.getType().is(PILLAGER_GUNNER) && event.getSlot() == EquipmentSlot.MAINHAND) {
            if (event.getTo().is(PillagersGunItems.SNIPERS_RIFLE.get())) {
                AttributeInstance attributeInstance = mob.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
                if (attributeInstance != null) {
                    AttributeModifier attributeModifier = new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "sniper", CommonConfig.SNIPERS_RIFLE_BONUS.get(), AttributeModifier.Operation.MULTIPLY_BASE);
                    attributeInstance.removeModifier(ATTRIBUTE_MODIFIER_UUID);
                    attributeInstance.addPermanentModifier(attributeModifier);
                }
            } else if (CommonConfig.TACZ_GUN_USE.get() && "tacz".equals(ForgeRegistries.ITEMS.getKey(event.getTo().getItem()).getNamespace())) {
                TaczCompat.changeEquipmentTo(event);
            } else {
                AttributeInstance attributeInstance = mob.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
                if (attributeInstance != null) {
                    attributeInstance.removeModifier(ATTRIBUTE_MODIFIER_UUID);
                }
            }

        }
    }

//    @SubscribeEvent
//    public static void hit(ProjectileImpactEvent event) {
//        if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult && event.getProjectile() instanceof Ammo ammo) {
//            Entity entity = ammo.getOwner();
//            if (entity != null) {
//                if (Ammo.checkFriendlyFire(entityHitResult.getEntity(), entity)) {
//                    event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
//                }
//            }
//        }
//    }


    @SubscribeEvent
    public static void knockbackCancel(LivingKnockBackEvent event) {
        if (event.getEntity().getPersistentData().getBoolean("ShootByGun")) {
            event.setCanceled(true);
        }
    }

}
