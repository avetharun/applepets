package com.avetharun.applepets.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
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

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
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
    @Override
    public void tick() {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            double d = this.mob.getX() - this.owner.getX();
            double e = this.mob.getY() - this.owner.getY();
            double f = this.mob.getZ() - this.owner.getZ();
            double g = d * d + e * e + f * f;
            if (!(g <= (double)(this.stopDistance * this.stopDistance))) {
                this.navigation.moveTo(this.owner, this.speedModifier);
            } else {
                this.navigation.stop();
                if (g <= (double)this.stopDistance) {
                    double h = this.owner.getX() - this.mob.getX();
                    double i = this.owner.getZ() - this.mob.getZ();
                    this.navigation.moveTo(this.mob.getX() - h, this.mob.getY(), this.mob.getZ() - i, this.speedModifier);
                }
            }
        }
    }
}
