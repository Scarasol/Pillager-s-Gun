package com.scarasol.pillagers_gun.compat.guardvillagers;

import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import net.minecraft.world.entity.Entity;

import tallestegg.guardvillagers.entities.Guard;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.animal.IronGolem;

public class GuardUseGun {
    public static void makeGuardUseGun(Entity entity){
        if (entity instanceof Guard guard){
            guard.goalSelector.addGoal(1, new GunAttackGoal<>(guard, 1.0D, 8.0F));
//	      	}
        }
    }

    public static boolean checkFriendlyFire(Entity target, Entity owner){
        if (owner instanceof Guard)
            return (target instanceof Guard || target instanceof Villager || target instanceof IronGolem);
        return false;
    }
}

