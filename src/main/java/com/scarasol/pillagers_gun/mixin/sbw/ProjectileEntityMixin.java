package com.scarasol.pillagers_gun.mixin.sbw;

import com.atsuishio.superbwarfare.entity.projectile.ProjectileEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.scarasol.pillagers_gun.entity.projectile.Ammo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Scarasol
 */
@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin {
    @WrapOperation(method = {"findEntityOnPath", "findEntitiesOnPath"}, remap = false, at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private List<Entity> pillagersGun$skipFriendlyFireProjectileTargets(Level level, Entity except, AABB area, Predicate<Entity> predicate, Operation<List<Entity>> original) {
        Entity shooter = ((ProjectileEntity) (Object) this).getShooter();
        Predicate<Entity> friendlyFirePredicate = predicate;
        if (shooter != null) {
            friendlyFirePredicate = predicate.and(entity -> !Ammo.checkFriendlyFire(entity, shooter));
        }
        return original.call(level, except, area, friendlyFirePredicate);
    }
}
