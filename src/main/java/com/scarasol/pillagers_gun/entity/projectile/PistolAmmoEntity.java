package com.scarasol.pillagers_gun.entity.projectile;

import com.google.common.collect.Lists;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.init.PillagersGunEntities;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ShieldItem;
import java.util.Random;


public class PistolAmmoEntity extends Ammo{

    public PistolAmmoEntity(EntityType<? extends PistolAmmoEntity> entityType, Level level) {
        super(entityType, level);
    }

    public PistolAmmoEntity(PlayMessages.SpawnEntity packet, Level world) {
        super(PillagersGunEntities.PISTOL_AMMO.get(), world);
    }


    public PistolAmmoEntity(Level level, LivingEntity livingEntity) {
        super(PillagersGunEntities.PISTOL_AMMO.get(), livingEntity, level);
    }

    public PistolAmmoEntity(Level level, double d, double d2, double d3) {
        super(PillagersGunEntities.PISTOL_AMMO.get(), d, d2, d3, level);
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
        if (owner != null){
            if (checkFriendlyFire(entity, owner)){
                this.discard();
                return;
            }
            ammo1 = DamageSource.arrow(this, owner);
        }else {
            ammo1 = DamageSource.arrow(this, this);
        }
        super.onHitEntity(entityHitResult);
        entity.invulnerableTime = 0;
        entity.hurt(ammo1, CommonConfig.PISTOL_POWER.get());
        this.discard();
    }
}

