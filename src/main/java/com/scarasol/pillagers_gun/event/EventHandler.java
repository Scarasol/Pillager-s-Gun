package com.scarasol.pillagers_gun.event;


import com.scarasol.pillagers_gun.compat.sbw.SbwCompat;
import com.scarasol.pillagers_gun.compat.tacz.TaczCompat;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.scarasol.pillagers_gun.event.server.InaccuracyEvent;
import com.scarasol.pillagers_gun.init.PillagersGunItems;
import com.scarasol.pillagers_gun.util.GunnerEquipmentService;
import com.scarasol.pillagers_gun.util.GunUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod.EventBusSubscriber
public class EventHandler {

    public static final UUID ATTRIBUTE_MODIFIER_UUID = UUID.fromString("6D4802D7-3BA3-EEB9-C88A-94AD48F68BFD");

    public static final TagKey<EntityType<?>> BORN_WITH_GUN = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge:born_with_gun"));
    public static final TagKey<EntityType<?>> PILLAGER_GUNNER = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge:pillager_gunner"));

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        if (entity.getType().is(PILLAGER_GUNNER) && entity instanceof Mob mob) {
            mob.goalSelector.addGoal(1, new GunAttackGoal<>(mob, 1.0D, 64.0F));
            mob.setLeftHanded(false);
        }
        if (entity.getType().is(BORN_WITH_GUN) && entity instanceof Mob mob) {
            if (!event.loadedFromDisk()) {
                GunnerEquipmentService.scheduleBornWithGun(mob);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob
                && mob.getType().is(BORN_WITH_GUN)
                && GunnerEquipmentService.hasScheduledBornWithGun(mob)) {
            GunnerEquipmentService.tickBornWithGun(mob);
        }
    }

    @SubscribeEvent
    public static void changeEquip(LivingEquipmentChangeEvent event) {
        LivingEntity mob = event.getEntity();
        if (mob.getType().is(PILLAGER_GUNNER) && event.getSlot() == EquipmentSlot.MAINHAND) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(event.getTo().getItem());
            if (event.getTo().is(PillagersGunItems.SNIPERS_RIFLE.get())) {
                GunnerEquipmentService.applyVanillaSniperFollowRange(mob, event.getTo());
            } else if (CommonConfig.TACZ_GUN_USE.get() && itemId != null && "tacz".equals(itemId.getNamespace())) {
                TaczCompat.changeEquipmentTo(event);
            } else if (CommonConfig.SBW_GUN_USE.get()
                    && itemId != null
                    && "superbwarfare".equals(itemId.getNamespace())
                    && SbwCompat.isSniperGun(event.getTo())) {
                GunnerEquipmentService.applySniperFollowRange(mob);
            } else {
                GunnerEquipmentService.clearFollowRangeModifier(mob);
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

    @SubscribeEvent
    public static void modifyInaccuracy(InaccuracyEvent event) {
        if (CommonConfig.DYNAMIC_INACCURACY.get() && event.getTarget() != null) {
            float newInaccuracy = GunUtil.getModifiedInaccuracy(event.getShooter(), event.getTarget(), event.getOldInaccuracy());
            event.setNewInaccuracy(newInaccuracy);
        }
    }

}
