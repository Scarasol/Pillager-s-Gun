package com.scarasol.pillagers_gun.mixin.tacz;

import com.scarasol.pillagers_gun.compat.tacz.TaczCompat;
import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.scarasol.pillagers_gun.event.EventHandler;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.item.ModernKineticGunItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(ModernKineticGunItem.class)
public abstract class ModernKineticGunItemMixin extends Item implements IGun, IAnimationItem {
    public ModernKineticGunItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "lambda$doBulletSpread$29", cancellable = true, at = @At(value = "INVOKE", target = "Lcom/tacz/guns/entity/EntityKineticBullet;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    private static void pillagersGun$gunnerShoot(EntityKineticBullet bullet, LivingEntity shooter, float pitch, float yaw, float processedSpeed, float inaccuracy, CallbackInfo ci) {
        if (shooter.getType().is(EventHandler.PILLAGER_GUNNER)) {
            ItemStack gunItem = shooter.getItemInHand(InteractionHand.MAIN_HAND);
            IGun iGun = IGun.getIGunOrNull(gunItem);
            if (iGun != null) {

                TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem)).ifPresent((commonGunIndex) -> {
                    double inaccuracyNew = TaczCompat.getInaccuracy(commonGunIndex.getType(), inaccuracy);
                    if (GunAttackGoal.isStunned(shooter)) {
                        inaccuracyNew += 8;
                    }
                    bullet.shootFromRotation(shooter, pitch, yaw, 0.0F, processedSpeed, (float) Math.max(inaccuracy, inaccuracyNew));
                    ci.cancel();
                });
            }
        }

    }

    @Inject(method = "lambda$doBulletSpread$28", cancellable = true, at = @At(value = "INVOKE", target = "Lcom/tacz/guns/entity/EntityKineticBullet;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFLorg/joml/Vector2d;)V"))
    private static void pillagersGun$gunnerShoot(EntityKineticBullet bullet, LivingEntity shooter, float pitch, float yaw, float processedSpeed, Vector2d vector2d, CallbackInfo ci) {
        if (shooter.getType().is(EventHandler.PILLAGER_GUNNER)) {
            ItemStack gunItem = shooter.getItemInHand(InteractionHand.MAIN_HAND);
            IGun iGun = IGun.getIGunOrNull(gunItem);
            if (iGun != null) {
                double inaccuracy = vector2d.length();
                TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem)).ifPresent((commonGunIndex) -> {
                    double inaccuracyNew = TaczCompat.getInaccuracy(commonGunIndex.getType(), inaccuracy);
                    if (GunAttackGoal.isStunned(shooter)) {
                        inaccuracyNew += 8;
                    }

                    Vector2d vector2dNew = new Vector2d(Math.max(inaccuracyNew * shooter.getRandom().nextGaussian(), vector2d.x), Math.max(inaccuracyNew * shooter.getRandom().nextGaussian(), vector2d.y));
                    bullet.shootFromRotation(shooter, pitch, yaw, 0.0F, processedSpeed, vector2dNew);
                    ci.cancel();
                });
            }
        }

    }
}
