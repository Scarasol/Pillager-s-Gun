package com.scarasol.pillagers_gun.compat.guardvillagers;

import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.npc.AbstractVillager;
import tallestegg.guardvillagers.entities.Guard;
import net.minecraft.world.entity.animal.IronGolem;

public class GuardUseGun {

    public static boolean checkFriendlyFire(Entity target, Entity owner){
        if (owner instanceof Guard) {
            return (target instanceof Guard || target instanceof AbstractVillager || target instanceof IronGolem);
        }
        return false;
    }
}

