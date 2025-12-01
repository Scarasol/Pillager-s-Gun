package com.scarasol.pillagers_gun.mixin.tacz;

import com.scarasol.pillagers_gun.entity.projectile.Ammo;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/**
 * @author Scarasol
 */
@Mixin(EntityUtil.class)
public abstract class EntityUtilMixin {

    @Inject(method = "findEntityOnPath", remap = false, at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void pillagersGun$checkFriendlyFireEntity(Projectile bulletEntity, Vec3 startVec, Vec3 endVec, CallbackInfoReturnable<EntityKineticBullet.EntityResult> cir, Vec3 hitVec, Entity hitEntity, boolean headshot, List<Entity> entities, double closestDistance, Entity owner) {
        if (owner != null) {
            entities.removeIf(entity -> Ammo.checkFriendlyFire(entity, owner));
        }

    }
}
