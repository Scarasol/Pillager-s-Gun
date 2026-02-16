package com.scarasol.pillagers_gun.event.server;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

/**
 * @author Administrator
 */
public class InaccuracyEvent extends Event {

    private float newInaccuracy;

    private final float oldInaccuracy;
    private final Entity shooter;
    private final Entity target;
    private final Vec3 targetLastPosition;

    public InaccuracyEvent(float oldInaccuracy, Entity shooter, Entity target, Vec3 targetLastPosition) {
        this.oldInaccuracy = oldInaccuracy;
        this.shooter = shooter;
        this.target = target;
        this.targetLastPosition = targetLastPosition;
        this.newInaccuracy = oldInaccuracy;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

    public float getNewInaccuracy() {
        return newInaccuracy;
    }

    public void setNewInaccuracy(float newInaccuracy) {
        this.newInaccuracy = newInaccuracy;
    }

    public float getOldInaccuracy() {
        return oldInaccuracy;
    }

    public Entity getShooter() {
        return shooter;
    }

    public Entity getTarget() {
        return target;
    }

    public Vec3 getTargetLastPosition() {
        return targetLastPosition;
    }
}
