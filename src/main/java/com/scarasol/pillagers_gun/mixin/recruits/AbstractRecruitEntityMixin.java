package com.scarasol.pillagers_gun.mixin.recruits;

import com.scarasol.pillagers_gun.compat.tacz.TaczCompat;
import com.scarasol.pillagers_gun.compat.zombiekit.MobUseFlameThrower;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.event.EventHandler;
import com.scarasol.pillagers_gun.init.PillagersGunItems;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import com.talhanation.recruits.entities.AbstractInventoryEntity;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AbstractRecruitEntity.class)
public abstract class AbstractRecruitEntityMixin extends AbstractInventoryEntity {

    public AbstractRecruitEntityMixin(EntityType<? extends AbstractInventoryEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (CommonConfig.GUARD_SPAWN_WITH_GUN.get() && !level().isClientSide) {
            if (!this.getPersistentData().getBoolean("spawnWithGun")) {
                if (level().getRandom().nextDouble() < CommonConfig.GUARD_EQUIP_CHANCE.get()) {
                    if (!ModList.get().isLoaded("tacz") || !CommonConfig.TACZ_GUN_USE.get() || !CommonConfig.GUARD_SPAWN_WITH_TACZ_GUN.get() || !TaczCompat.spawnWithTaczGun(this)) {
                        double totalWeights = CommonConfig.GUARD_ASSAULT_CHANCE.get() + CommonConfig.GUARD_PISTOL_CHANCE.get() + CommonConfig.GUARD_SHOTGUN_CHANCE.get() + CommonConfig.GUARD_SNIPERS_RIFLE_CHANCE.get() + CommonConfig.GUARD_BAZOOKA_CHANCE.get();
                        double i = random.nextDouble();
                        boolean flag = false;
                        Optional<? extends ModContainer> optional = ModList.get().getModContainerById("zombiekit");
                        if (optional.isPresent()) {
                            ModContainer modContainer = optional.get();
                            ArtifactVersion version = modContainer.getModInfo().getVersion();
                            flag = version.getMajorVersion() >= 2 && version.getMinorVersion() >= 1;
                        }
                        if (flag) {
                            totalWeights += CommonConfig.GUARD_FLAMETHROWER_CHANCE.get();
                            if (i < CommonConfig.GUARD_FLAMETHROWER_CHANCE.get() / totalWeights) {
                                MobUseFlameThrower.makeMobsUseFlameThrower(this);
                                this.getPersistentData().putBoolean("spawnWithGun", true);
                                return;
                            }
                            totalWeights -= CommonConfig.GUARD_FLAMETHROWER_CHANCE.get();
                        }
                        ItemStack itemStack;
                        if (i < CommonConfig.GUARD_PISTOL_CHANCE.get() / totalWeights) {
                            itemStack = new ItemStack(PillagersGunItems.PISTOL.get());
                        } else if (i < (CommonConfig.GUARD_PISTOL_CHANCE.get() + CommonConfig.GUARD_ASSAULT_CHANCE.get()) / totalWeights) {
                            itemStack = new ItemStack(PillagersGunItems.ASSAULT_RIFLE.get());
                        } else if (i < (CommonConfig.GUARD_PISTOL_CHANCE.get() + CommonConfig.GUARD_ASSAULT_CHANCE.get() + CommonConfig.GUARD_SHOTGUN_CHANCE.get()) / totalWeights){
                            itemStack = new ItemStack(PillagersGunItems.SHOTGUN.get());
                        }else if (i < (CommonConfig.GUARD_PISTOL_CHANCE.get() + CommonConfig.GUARD_ASSAULT_CHANCE.get() + CommonConfig.GUARD_SHOTGUN_CHANCE.get() + CommonConfig.GUARD_SNIPERS_RIFLE_CHANCE.get()) / totalWeights) {
                            itemStack = new ItemStack(PillagersGunItems.SNIPERS_RIFLE.get());
                            AttributeInstance attributeInstance = this.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
                            if (attributeInstance != null) {
                                AttributeModifier attributeModifier = new AttributeModifier(EventHandler.ATTRIBUTE_MODIFIER_UUID, "sniper", CommonConfig.SNIPERS_RIFLE_BONUS.get(), AttributeModifier.Operation.MULTIPLY_BASE);
                                attributeInstance.removeModifier(EventHandler.ATTRIBUTE_MODIFIER_UUID);
                                attributeInstance.addPermanentModifier(attributeModifier);
                            }
                        }else {
                            itemStack = new ItemStack(PillagersGunItems.BAZOOKA.get());
                        }
                        GunItem.init(itemStack);
                        itemStack.setCount(1);
                        this.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                    }
                }
                this.getPersistentData().putBoolean("spawnWithGun", true);
            }
        }
    }
}
