package com.scarasol.pillagers_gun.mixin.sbw;

import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.pillagers_gun.entity.projectile.Ammo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

/**
 * @author Scarasol
 */
@Mixin(GunItem.class)
public abstract class GunItemMixin {
    @WrapOperation(method = "shootRay", remap = false, at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"))
    private EntityHitResult pillagersGun$skipFriendlyFireRayTargets(Entity shooter, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter, double distance, Operation<EntityHitResult> original) {
        Predicate<Entity> friendlyFireFilter = filter;
        if (shooter != null) {
            friendlyFireFilter = filter.and(entity -> !Ammo.checkFriendlyFire(entity, shooter));
        }
        return original.call(shooter, startVec, endVec, boundingBox, friendlyFireFilter, distance);
    }
}
