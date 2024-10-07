package com.scarasol.pillagers_gun.compat.recruits;

import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.CrossBowmanEntity;
import com.talhanation.recruits.entities.ai.RecruitMoveTowardsTargetGoal;
import com.talhanation.recruits.entities.ai.RecruitRangedCrossbowAttackGoal;
import com.talhanation.recruits.entities.ai.compat.RecruitRangedMusketAttackGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;


public class RecruitUseGun {
    public static void makeCrossBowmanUseGun(Entity entity){
        if (entity instanceof CrossBowmanEntity crossBowmanEntity){

            crossBowmanEntity.goalSelector.addGoal(1, new GunAttackGoal<>(crossBowmanEntity, 1.0D, 8.0F));

        }
    }

    public static boolean checkFriendlyFire(Entity target, Entity owner){
        if (owner instanceof CrossBowmanEntity)
            return (target instanceof AbstractRecruitEntity || target instanceof Villager || target instanceof IronGolem);
        return false;
    }
}
