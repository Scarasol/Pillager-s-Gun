package com.scarasol.pillagers_gun.entity.projectile;

import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.init.PillagersGunEntities;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;

public class RocketEntity extends Ammo {
    public RocketEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public RocketEntity(PlayMessages.SpawnEntity packet, Level world) {
        super(PillagersGunEntities.ROCKET.get(), world);
    }


    public RocketEntity(Level level, LivingEntity livingEntity) {
        super(PillagersGunEntities.ROCKET.get(), livingEntity, level);
    }

    public RocketEntity(Level level, double d, double d2, double d3) {
        super(PillagersGunEntities.ROCKET.get(), d, d2, d3, level);
    }

    @Override
    public void onHit(HitResult hitResult) {
        HitResult.Type hitresult$type = hitResult.getType();
        if (hitresult$type == HitResult.Type.ENTITY) {
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, (BlockState)null));
        } else if (hitresult$type == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult)hitResult;
            BlockPos blockpos = blockhitresult.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
        }
        Vec3 location = hitResult.getLocation();
        Level.ExplosionInteraction explosionInteraction = CommonConfig.BAZOOKA_BREAK.get() ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE;
        this.level().explode(getOwner(), location.x, location.y, location.z, CommonConfig.BAZOOKA_EXPLOSION_LEVEL.get(), explosionInteraction);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            for (int i = 0; i < 10 ; i++) {
                double distanceX = getX() - xo;
                double distanceY = getY() - yo;
                double distanceZ = getZ() - zo;
                level().addAlwaysVisibleParticle(ParticleTypes.MYCELIUM, xo + (double) i / 10 * distanceX, yo + (double) i / 10 * distanceY, zo + (double) i / 10 * distanceZ, 0, 0, 0);
            }

        }
    }
}
