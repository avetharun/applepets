package com.avetharun.applepets.mixin;

import com.avetharun.applepets.ApplePetRegistry;
import com.avetharun.applepets.Applepets;
import com.avetharun.applepets.NBTEditor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftAgeable;
import org.bukkit.entity.Ageable;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class NonHostileEntity extends Mob {
    public Player owner;
    public ApplePetRegistry registry;
    public NonHostileEntity(EntityType<? extends Mob> type, Level world, Location loc, Player owner, ApplePetRegistry r) {
        super(type, world);
        this.registry = r;
        this.owner = owner;
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        this.setRot(loc.getYaw(), loc.getPitch());
        if (this.getBukkitEntity() instanceof CraftAgeable a) {
            if (r.getBaby() >= 1) {
                a.setBaby();
            }
            a.setAgeLock(true);
        }

        CompoundTag t = new CompoundTag();
        t.putBoolean("pet", true);
        t.putInt("variant", registry.getVariant());
        t.putInt("baby", registry.getBaby());
        t.putString("uuid", registry.getUuid());
        NBTEditor.set(this.getBukkitEntity(), "variant", registry.getVariant());
                this.getBukkitEntity().setCustomName(r.getDisplay());

        ((CraftWorld)loc.getWorld()).getHandle().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        Applepets.SPAWNED_PETS.put(owner.getUUID(), this.getUUID());
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
        if (entityNbt == null) {
            entityNbt = new CompoundTag();
        }
        entityData = new Fox.FoxGroupData(Fox.Type.SNOW);
        return entityData;
    }


    boolean isFirstTick = true;

    @Override
    public void tick() {
        if (isFirstTick) {
            isFirstTick = false;

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
