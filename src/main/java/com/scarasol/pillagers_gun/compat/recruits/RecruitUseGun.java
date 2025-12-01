package com.scarasol.pillagers_gun.compat.recruits;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;


public class RecruitUseGun {

    public static boolean checkFriendlyFire(Entity target, Entity owner){
        if (owner instanceof AbstractRecruitEntity)
            return (target instanceof AbstractRecruitEntity || target instanceof AbstractVillager || target instanceof IronGolem);
        return false;
    }
}
