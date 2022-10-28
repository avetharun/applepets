package com.avetharun.applepets.mixin;

import com.avetharun.applepets.Applepets;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.commands.KillCommand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Warden;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NonHostileEntity extends PathfinderMob {
    Player owner;
    public NonHostileEntity(EntityType<? extends PathfinderMob> type, Level world, Location loc, Player owner) {
        super(type, world);
        this.owner = owner;
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        this.setRot(loc.getYaw(), loc.getPitch());
        ((CraftWorld)loc.getWorld()).getHandle().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        Applepets.SPAWNED_PETS.put(owner.getUUID(), this);
        this.goalSelector.getAvailableGoals().forEach(wrappedGoal -> {
            if (wrappedGoal.getGoal() instanceof FollowPlayerGoal playerGoal) {
                playerGoal.SetOwner(owner);
            }
        });
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FollowPlayerGoal(this, 1.0f, 2f, 5f));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class,8.0f));
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot equipmentSlot, @NotNull ItemStack itemStack) {
        return;
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
    int ticksSinceLastLookat;
    int ticksSinceLastMoveNearPlayer;
    @Override
    public void tick() {
        super.tick();
        if (ticksSinceLastLookat++ >= getAmbientSoundInterval()) {
            playAmbientSound();
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource damagesource, float amount) {

        if (damagesource == DamageSource.OUT_OF_WORLD) {
            this.getBukkitEntity().remove();
        }
        if (damagesource.getDirectEntity() instanceof Player p && p == owner) {
            this.getBukkitEntity().remove();
        }
        return false;
    }

    @Override
    public void kill() {
        super.kill();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return false; // pass
    }
}
