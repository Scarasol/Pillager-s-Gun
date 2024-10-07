package com.scarasol.pillagers_gun.entity.projectile;

import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.init.PillagersGunEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.item.ShieldItem;
import java.util.Random;


public class ShotgunAmmoEntity extends Ammo{

    public ShotgunAmmoEntity(EntityType<? extends ShotgunAmmoEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ShotgunAmmoEntity(PlayMessages.SpawnEntity packet, Level world) {
        super(PillagersGunEntities.SHOTGUN_AMMO.get(), world);
    }


    public ShotgunAmmoEntity(Level level, LivingEntity livingEntity) {
        super(PillagersGunEntities.SHOTGUN_AMMO.get(), livingEntity, level);
    }

    public ShotgunAmmoEntity(Level level, double d, double d2, double d3) {
        super(PillagersGunEntities.SHOTGUN_AMMO.get(), d, d2, d3, level);
    }

    @Override
    public void onHitEntity(EntityHitResult entityHitResult) {
        this.playSound(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("pillagers_gun:bullet_hit_body")), 1.0f, 1.0f);
        Entity entity = entityHitResult.getEntity();
        if (entity.invulnerableTime >= 10 && !CommonConfig.BYPASS_INVULNERABLE.get()){
            this.discard();
            return;
        }
        Entity owner = this.getOwner();
        DamageSource ammo1;
        if (owner != null){
            if (checkFriendlyFire(entity, owner)){
                this.discard();
                return;
            }
            ammo1 = DamageSource.arrow(this, owner);
        }else {
            ammo1 = DamageSource.arrow(this, this);
        }
        int time = Mth.nextInt(this.level.getRandom(), 1, CommonConfig.SHOTGUN_COUNT.get());
        super.onHitEntity(entityHitResult);
        for (int i = 0; i < time; i++){
            entity.invulnerableTime = 0;
            entity.hurt(ammo1, CommonConfig.SHOTGUN_POWER.get());
        }
        if(new Random().nextDouble() < 0.1 * time){
            if(entity instanceof Player player){
                if(player.isDamageSourceBlocked(ammo1) && player.getUseItem().getItem() instanceof ShieldItem){
                    player.disableShield(true);
                }
            }else if(entity instanceof LivingEntity livingEntity){
                if(livingEntity.isDamageSourceBlocked(ammo1) && livingEntity.getUseItem().getItem() instanceof ShieldItem){
                    livingEntity.stopUsingItem();
                }
            }
        }
        this.discard();
    }
}

