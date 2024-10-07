package com.scarasol.pillagers_gun.event;


import com.scarasol.pillagers_gun.compat.guardvillagers.GuardUseGun;
import com.scarasol.pillagers_gun.compat.recruits.RecruitUseGun;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.scarasol.pillagers_gun.init.PillagersGunItems;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class EventHandler {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        Random random = new Random();
        if (entity == null)
            return;
        if (entity instanceof Pillager mob) {
            mob.goalSelector.addGoal(2, new GunAttackGoal<>(mob, 1.0D, 8.0F));
            if (mob.getItemInHand(InteractionHand.MAIN_HAND).is(Items.CROSSBOW) && !event.loadedFromDisk() && random.nextDouble() <= CommonConfig.EQUIP_CHANCE.get()) {
                double totalWeights = CommonConfig.ASSAULT_CHANCE.get() + CommonConfig.PISTOL_CHANCE.get() + CommonConfig.SHOTGUN_CHANCE.get();
                double i = random.nextDouble() * totalWeights;
                ItemStack itemStack;
                if (i < CommonConfig.PISTOL_CHANCE.get() / totalWeights) {
                    itemStack = new ItemStack(PillagersGunItems.PISTOL.get());
                } else if (i < (CommonConfig.PISTOL_CHANCE.get() + CommonConfig.ASSAULT_CHANCE.get()) / totalWeights) {
                    itemStack = new ItemStack(PillagersGunItems.ASSAULT_RIFLE.get());
                } else {
                    itemStack = new ItemStack(PillagersGunItems.SHOTGUN.get());
                }
                GunItem.init(itemStack);
                itemStack.setCount(1);
                mob.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                mob.setDropChance(EquipmentSlot.MAINHAND, CommonConfig.DROP_CHANCE.get().floatValue());
            }
        }
        if (ModList.get().isLoaded("guardvillagers")) {
            GuardUseGun.makeGuardUseGun(entity);
        }
        if (ModList.get().isLoaded("recruits")){
            RecruitUseGun.makeCrossBowmanUseGun(entity);
        }


    }
}
