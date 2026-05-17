package com.scarasol.pillagers_gun.mixin.sbw;

import com.atsuishio.superbwarfare.item.gun.GunItem;
import com.atsuishio.superbwarfare.tools.InventoryTool;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.event.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Scarasol
 */
@Mixin(InventoryTool.class)
public abstract class InventoryToolMixin {
    @Inject(method = "hasCreativeAmmoBox(Lnet/minecraft/world/entity/Entity;)Z", remap = false, cancellable = true, at = @At("HEAD"))
    private static void pillagersGun$gunnerHasSbwBackupAmmo(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (CommonConfig.SBW_GUN_USE.get()
                && !CommonConfig.SBW_GUNNERS_NEED_AMMO.get()
                && entity instanceof Mob mob
                && mob.getType().is(EventHandler.PILLAGER_GUNNER)
                && mob.getMainHandItem().getItem() instanceof GunItem) {
            cir.setReturnValue(true);
        }
    }
}
