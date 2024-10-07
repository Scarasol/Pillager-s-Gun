package com.scarasol.pillagers_gun.entity.projectile;

import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.init.PillagersGunEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;


public class AssaultRifleAmmoEntity extends Ammo{

    public AssaultRifleAmmoEntity(EntityType<? extends AssaultRifleAmmoEntity> entityType, Level level) {
        super(entityType, level);
    }

    public AssaultRifleAmmoEntity(PlayMessages.SpawnEntity packet, Level world) {
        super(PillagersGunEntities.ASSAULT_RIFLE_AMMO.get(), world);
    }


    public AssaultRifleAmmoEntity(Level level, LivingEntity livingEntity) {
        super(PillagersGunEntities.ASSAULT_RIFLE_AMMO.get(), livingEntity, level);
    }

    public AssaultRifleAmmoEntity(Level level, double d, double d2, double d3) {
        super(PillagersGunEntities.ASSAULT_RIFLE_AMMO.get(), d, d2, d3, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        this.playSound(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("pillagers_gun:bullet_hit_body")), 1.0f, 1.0f);
        Entity entity = entityHitResult.getEntity();
        if (entity.invulnerableTime >= 10 && !CommonConfig.BYPASS_INVULNERABLE.get()){
            this.discard();
            return;
        }
        Entity owner = this.getOwner();
        DamageSource ammo1;
        DamageSource ammo2;
        if (owner != null){
            if (checkFriendlyFire(entity, owner)){
                this.discard();
                return;
            }
            ammo1 = this.level().damageSources().arrow(this, owner);
            ammo2 = this.level().damageSources().arrow(this, owner);
        }else {
            ammo1 = this.level().damageSources().arrow(this, this);
            ammo2 = this.level().damageSources().arrow(this, this);
        }
        entity.invulnerableTime = 0;
        entity.hurt(ammo1, CommonConfig.ASSAULT_POWER.get().floatValue() * (1 - CommonConfig.ASSAULT_BYPASS_RATE.get().floatValue()));
        if (entity instanceof LivingEntity livingEntity && !livingEntity.isDamageSourceBlocked(ammo1)){
            entity.invulnerableTime = 0;
            entity.hurt(ammo2, CommonConfig.ASSAULT_POWER.get().floatValue() * CommonConfig.ASSAULT_BYPASS_RATE.get().floatValue());
        }
        super.onHitEntity(entityHitResult);

        this.discard();
    }
}

