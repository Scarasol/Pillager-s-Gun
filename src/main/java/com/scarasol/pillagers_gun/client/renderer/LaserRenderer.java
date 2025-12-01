package com.scarasol.pillagers_gun.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.scarasol.pillagers_gun.compat.tacz.TaczCompat;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.init.PillagersGunItems;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;

public class LaserRenderer {

    public static void renderLaser(LivingEntity entity, PoseStack poseStack,
                                   MultiBufferSource bufferSource, float partialTicks) {
        if (!shouldRender(entity.getMainHandItem())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        double followRange = getfollowRange(entity);
        if (followRange <= 0) {
            return;
        }
        double followRangeSqr = followRange * followRange;

        if (entity.distanceToSqr(player) >= followRangeSqr) {
            return;
        }

        Vec3 eyePos   = entity.getEyePosition(partialTicks);
        Vec3 viewDir  = entity.getViewVector(partialTicks).normalize();
        Vec3 toPlayer = player.getEyePosition(partialTicks).subtract(eyePos);

        if (toPlayer.lengthSqr() < 1.0E-6D) {
            return;
        }

        double angle = vectorDegreeCalculate(viewDir, toPlayer);
        if (angle > 15.0D) {
            return;
        }

        Vec3 startWorld = entity.getRopeHoldPosition(partialTicks);
        Vec3 endWorld   = getMinPosition(entity, followRange);

        Vec3 startLocal = startWorld.subtract(entity.position());
        Vec3 endLocal   = endWorld.subtract(entity.position());

        renderLineBeam(poseStack, bufferSource,
                startLocal, endLocal,
                3.0F, 50,
                0xFF0000, 0xFF0000,
                partialTicks, 0.0F);
    }

    public static double getfollowRange(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attributes.FOLLOW_RANGE);
        if (attribute == null) {
            return 0;
        }
        double value = attribute.getValue();
        ItemStack itemStack = entity.getMainHandItem();
        if (itemStack.is(PillagersGunItems.SNIPERS_RIFLE.get())) {
            value *= (CommonConfig.SNIPERS_RIFLE_BONUS.get() + 1);
        }else if ("tacz".equals(ForgeRegistries.ITEMS.getKey(itemStack.getItem()).getNamespace())) {
            value *= TaczCompat.getZoomAttribute(itemStack);
        }
        return value;
    }

    public static boolean shouldRender(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof GunItem gunItem) {
            return gunItem.shouldRenderLaser();
        }else if ("tacz".equals(ForgeRegistries.ITEMS.getKey(item).getNamespace())) {
            return TaczCompat.shouldRenderLaser(itemStack);
        }
        return false;
    }

    public static Vec3 getMinPosition(LivingEntity entity, double distance) {
        Level level = entity.level();

        Vec3 eyePos = entity.getEyePosition();
        Vec3 viewDir = entity.getViewVector(1.0F).normalize();
        Vec3 end    = eyePos.add(viewDir.scale(distance));

        BlockHitResult blockHit = level.clip(new ClipContext(
                eyePos,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                entity
        ));

        double blockDistSqr = Double.MAX_VALUE;
        Vec3 blockHitPos = null;
        if (blockHit.getType() != HitResult.Type.MISS) {
            blockHitPos = blockHit.getLocation();
            blockDistSqr = blockHitPos.distanceToSqr(eyePos);
        }

        AABB searchBox = entity.getBoundingBox()
                .expandTowards(viewDir.scale(distance))
                .inflate(1.0D);

        Predicate<Entity> predicate = e ->
                e != entity &&
                        !e.isSpectator() &&
                        e.isPickable();

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level,
                entity,
                eyePos,
                end,
                searchBox,
                predicate
        );

        double entityDistSqr = Double.MAX_VALUE;
        Vec3 entityHitPos = null;

        if (entityHit != null) {
            Entity target = entityHit.getEntity();

            Vec3 targetPos = target.getEyePosition();
            double dist = eyePos.distanceTo(targetPos);

            if (dist > distance) {
                dist = distance;
            }

            entityHitPos = eyePos.add(viewDir.scale(dist));
            entityDistSqr = dist * dist;
        }

        if (blockDistSqr == Double.MAX_VALUE && entityDistSqr == Double.MAX_VALUE) {
            return end;
        } else if (entityDistSqr < blockDistSqr) {
            return entityHitPos;
        } else {
            return blockHitPos;
        }
    }


    public static double vectorDegreeCalculate(Vec3 vec1, Vec3 vec2) {
        double cos = vec1.dot(vec2) / vec1.length() / vec2.length();
        return Math.toDegrees(Math.acos(cos));
    }

    public static void renderLineBeam(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 start, Vec3 end, float thickness, int segments, int baseColor, int tipColor, float animationOffset, float wiggleIntensity) {
        Vec3 delta = end.subtract(start);
        float deltaX = (float)delta.x;
        float deltaY = (float)delta.y;
        float deltaZ = (float)delta.z;
        float totalDist = Mth.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (!(totalDist <= 1.0E-6F)) {
            poseStack.pushPose();
            poseStack.translate((float)start.x, (float)start.y, (float)start.z);
            float horizontalDist = Mth.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            poseStack.mulPose(Axis.YP.rotation((float)(-Math.atan2((double)deltaZ, (double)deltaX)) - 1.5707964F));
            poseStack.mulPose(Axis.XP.rotation((float)(-Math.atan2((double)horizontalDist, (double)deltaY)) - 1.5707964F));
            VertexConsumer builder = bufferSource.getBuffer(ModRenderType.translucentLines((double)thickness));
            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            int steps = Math.max(2, segments * 8);
            steps = Math.min(steps, 512);
            float maxWiggle = totalDist * wiggleIntensity;
            Random random = new Random((long)((double)animationOffset * 12345.6789D));
            float prevX = 0.0F;
            float prevY = 0.0F;
            float prevZ = 0.0F;

            for(int i = 1; i <= steps; ++i) {
                float t = (float)i / (float)steps;
                float z = t * totalDist;
                float x;
                float y;
                if (Math.abs(wiggleIntensity) > 0.0F) {
                    x = (random.nextFloat() - 0.5F) * 2.0F * maxWiggle;
                    y = (random.nextFloat() - 0.5F) * 2.0F * maxWiggle;
                } else {
                    x = 0.0F;
                    y = 0.0F;
                }

                int r1 = (int)Mth.lerp(t - 1.0F / (float)steps, (float)(baseColor >> 16 & 255), (float)(tipColor >> 16 & 255));
                int g1 = (int)Mth.lerp(t - 1.0F / (float)steps, (float)(baseColor >> 8 & 255), (float)(tipColor >> 8 & 255));
                int b1 = (int)Mth.lerp(t - 1.0F / (float)steps, (float)(baseColor & 255), (float)(tipColor & 255));
                int r2 = (int)Mth.lerp(t, (float)(baseColor >> 16 & 255), (float)(tipColor >> 16 & 255));
                int g2 = (int)Mth.lerp(t, (float)(baseColor >> 8 & 255), (float)(tipColor >> 8 & 255));
                int b2 = (int)Mth.lerp(t, (float)(baseColor & 255), (float)(tipColor & 255));
                builder.vertex(matrix, prevX, prevY, prevZ).color(r1, g1, b1, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                builder.vertex(matrix, x, y, z).color(r2, g2, b2, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                prevX = x;
                prevY = y;
                prevZ = z;
            }

            poseStack.popPose();
        }
    }
}
