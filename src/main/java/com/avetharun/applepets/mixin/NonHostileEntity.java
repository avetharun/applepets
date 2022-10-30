package com.avetharun.applepets.mixin;

import com.avetharun.applepets.ApplePetRegistry;
import com.avetharun.applepets.Applepets;
import com.avetharun.applepets.NBTEditor;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.util.Buildable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.commands.KillCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftFox;
import org.bukkit.entity.Warden;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.MetadataValueAdapter;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bukkit.craftbukkit.v1_19_R1.event.CraftEventFactory.callCreatureSpawnEvent;

public class NonHostileEntity extends PathfinderMob {
    Player owner;
    ApplePetRegistry registry;
    public NonHostileEntity(EntityType<? extends AgeableMob> type, Level world, Location loc, Player owner, ApplePetRegistry r) {
        super(type, world);
        this.registry = r;
        this.owner = owner;
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        this.setRot(loc.getYaw(), loc.getPitch());
        Entity e = this.getBukkitEntity().getHandle();
        if (e instanceof AgeableMob a) {
            a.setBaby(r.getBaby() >= 1);
            a.ageLocked = true;
            if (e.getType() == EntityType.FOX) {
                Fox f = ((Fox) this).setFoxType(Fox.Type.SNOW);
            }
        }


        ((CraftWorld)loc.getWorld()).getHandle().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        Applepets.SPAWNED_PETS.put(owner.getUUID(), this);
        this.goalSelector.getAvailableGoals().forEach(wrappedGoal -> {
            if (wrappedGoal.getGoal() instanceof FollowPlayerGoal playerGoal) {
                playerGoal.SetOwner(owner);
            }
        });
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        super.playStepSound(pos, state);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FollowPlayerGoal(this, 1.25f, 2.25f, 10.2f));
        this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class,8.0f));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
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

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag entityNbt) {
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
    }

    boolean isFirstTick = true;

    @Override
    public void tick() {
        if (isFirstTick) {
            isFirstTick = false;

            Arrays.stream(((CraftWorld) this.getBukkitEntity().getWorld()).getHandle().getChunkEntities(this.chunkPosition().x, this.chunkPosition().z)).forEach(
                    entity->{
                        if (entity.getUniqueId() == this.getUUID() && entity.getType() == org.bukkit.entity.EntityType.FOX) {
                            NBTEditor.set(((CraftEntity)entity).getHandle(), "snow", "Type");
                        }
                    }
            );
        }
        super.tick();
        if (ticksSinceLastLookat++ >= getAmbientSoundInterval()) {
            playAmbientSound();
        }
        if (this.getBukkitEntity().getHandle() instanceof Bat b) {
            b.setResting(false);
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
