package com.scarasol.pillagers_gun.mixin.tacz;

import com.scarasol.pillagers_gun.PillagersGunMod;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.event.EventHandler;
import com.tacz.guns.entity.shooter.LivingEntityAmmoCheck;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Scarasol
 */
@Mixin(LivingEntityAmmoCheck.class)
public abstract class LivingEntityAmmoCheckMixin {

    @Shadow @Final private LivingEntity shooter;

    @Inject(method = "needCheckAmmo", remap = false, cancellable = true, at = @At("HEAD"))
    private void pillagerGun$checkAmmo(CallbackInfoReturnable<Boolean> cir) {
        if (shooter.getType().is(EventHandler.PILLAGER_GUNNER)) {
            cir.setReturnValue(CommonConfig.TACZ_GUNNERS_NEED_AMMO.get());
        }
    }
}
