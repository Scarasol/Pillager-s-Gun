package com.scarasol.pillagers_gun.mixin.tacz;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.pillagers_gun.entity.projectile.Ammo;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * @author Scarasol
 */
@Mixin(EntityKineticBullet.class)
public abstract class EntityKineticBulletMixin<T> extends Projectile implements IEntityAdditionalSpawnData {

    protected EntityKineticBulletMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "onBulletTick", remap = false, at = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/EntityUtil;findEntitiesOnPath(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)Ljava/util/List;"))
    private @NotNull List<EntityKineticBullet.EntityResult> pillagersGun$checkFriendlyFireEntities(Projectile projectile, Vec3 startVec, Vec3 endVec, Operation<List<EntityKineticBullet.EntityResult>> original) {
        List<EntityKineticBullet.EntityResult> entityResults = original.call(projectile, startVec, endVec);
        if (getOwner() != null) {
            return entityResults.stream()
                    .filter(entityResult -> !Ammo.checkFriendlyFire(entityResult.getEntity(), getOwner()))
                    .toList();
        }
        return entityResults;
    }
}

