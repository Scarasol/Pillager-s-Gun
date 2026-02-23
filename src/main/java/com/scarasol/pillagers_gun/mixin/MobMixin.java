package com.scarasol.pillagers_gun.mixin;

import com.scarasol.pillagers_gun.api.IMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;

/**
 * @author Administrator
 */
@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements Targeting, IMob {

    @Unique
    private Vec3 pillagersGun$targetLastPositon;

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    public Vec3 getPillagersGun$targetLastPositon() {
        return Objects.requireNonNullElseGet(pillagersGun$targetLastPositon, this::getEyePosition);
    }

    @Unique
    public void setPillagersGun$targetLastPositon(Vec3 pillagersGun$targetLastPositon) {
        this.pillagersGun$targetLastPositon = pillagersGun$targetLastPositon;
    }
}
