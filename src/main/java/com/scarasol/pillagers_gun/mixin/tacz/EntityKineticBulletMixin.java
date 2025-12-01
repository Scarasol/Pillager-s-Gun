package com.scarasol.pillagers_gun.mixin.tacz;

import com.scarasol.pillagers_gun.entity.projectile.Ammo;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.EntityUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * @author Scarasol
 */
@Mixin(EntityKineticBullet.class)
public abstract class EntityKineticBulletMixin<T> extends Projectile implements IEntityAdditionalSpawnData {

    protected EntityKineticBulletMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "onBulletTick", remap = false, at = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/EntityUtil;findEntitiesOnPath(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)Ljava/util/List;"))
    private @NotNull List<EntityKineticBullet.EntityResult> pillagersGun$checkFriendlyFireEntities(Projectile projectile, Vec3 startVec, Vec3 endVec) {
        if (getOwner() != null) {
            return EntityUtil.findEntitiesOnPath(projectile, startVec, endVec).stream()
                    .filter(entityResult -> !Ammo.checkFriendlyFire(entityResult.getEntity(), getOwner()))
                    .toList();
        }
        return EntityUtil.findEntitiesOnPath(this, startVec, endVec);
    }
}

