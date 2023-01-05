package com.avetharun.applepets.mixin;

import com.avetharun.applepets.Applepets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;

public class FollowPlayerGoal extends Goal {
    private final Mob mob;
    private final Predicate<Mob> followPredicate;
    @NotNull
    private Player owner;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private float oldWaterCost;
    private final float areaSize;
    public FollowPlayerGoal(Mob mob, double speed, float minDistance, float maxDistance) {
        this.mob = mob;
        this.followPredicate = (target) -> {
            return target != null && mob.getClass() != target.getClass();
        };
        this.speedModifier = speed;
        this.navigation = mob.getNavigation();
        this.stopDistance = minDistance;
        this.areaSize = maxDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(mob.getNavigation() instanceof GroundPathNavigation) && !(mob.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowMobGoal");
        }
    }


    public boolean canUse() {
        LivingEntity entityliving = this.owner;
        if (entityliving.isSpectator() || !this.owner.valid) {
            this.mob.getBukkitMob().remove();
            return false;
        } else return !(this.mob.distanceToSqr(entityliving) < (double) (this.stopDistance * this.stopDistance));
    }
    @Override
    public boolean canContinueToUse() {
        return !this.navigation.isDone() && this.mob.distanceToSqr(this.owner) > (double)(this.stopDistance * this.stopDistance);
    }
    public void SetOwner(Player owner) {
        this.owner = owner;
    }
    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(BlockPathTypes.WATER);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.navigation.stop();
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }
    int ticksSinceLastJump = 0;
    @Override
    public void tick() {
        if (--this.timeToRecalcPath <= 0) {
            double _speed = speedModifier;
            EntityType t = this.mob.getType();
            if (t == EntityType.AXOLOTL || t == EntityType.TADPOLE || t == EntityType.DOLPHIN) {
                _speed *= 0.32f;
            } else if (t == EntityType.PANDA || t == EntityType.ALLAY || t == EntityType.STRIDER) {
                _speed *= 2f;
            }
            if (owner.isSprinting()) {
                _speed *= 1.2f;
            }
            this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float)this.mob.getMaxHeadXRot());
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            double d = this.mob.getX() - this.owner.getX();
            double e = this.mob.getY() - this.owner.getY();
            double f = this.mob.getZ() - this.owner.getZ();
            double g = d * d + f * f;
            if (g + (e*e) >= 128 /*64^2*/ || this.mob.touchingUnloadedChunk()) {
                this.mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
            }
            if (!(g <= (double)(this.stopDistance * this.stopDistance))) {
                this.navigation.moveTo(this.owner, _speed);
                if (this.mob.getType() == EntityType.RABBIT || this.mob.getType() == EntityType.SLIME || this.mob.getType() == EntityType.MAGMA_CUBE){
                    // jumping status
                    this.mob.getJumpControl().jump();
                    this.mob.getLevel().broadcastEntityEvent(this.mob, (byte)1);
                    if (this.mob.getType().is(EntityTypeTags.FROG_FOOD)) {
                        this.mob.playSound(SoundEvents.SLIME_JUMP_SMALL, this.mob.getSoundVolume(), ((this.mob.getRandom().nextFloat() - this.mob.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F);
                    } else {
                        this.mob.playSound(SoundEvents.RABBIT_JUMP, this.mob.getSoundVolume(), ((this.mob.getRandom().nextFloat() - this.mob.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F);
                    }
                }
            } else {
                this.navigation.stop();
            }
        }
    }
}
