package com.avetharun.applepets;
import com.avetharun.applepets.mixin.FollowPlayerGoal;
import com.mojang.datafixers.kinds.App;
import io.papermc.paper.configuration.transformation.global.LegacyPaperConfig;
import it.unimi.dsi.fastutil.Hash;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.NBTComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.tag.CraftTag;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNBTTagConfigSerializer;
import org.bukkit.craftbukkit.v1_19_R1.util.JsonHelper;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;



import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;

public class ApplePetRegistry {
    private static void EnsureEmptyExists() {
        if (EmptyPetRegistry == null) {
            EmptyPetRegistry = new ApplePetRegistry();
            EmptyPetRegistry.setUuid("BLANK");
            EmptyPetRegistry._stack = new ItemStack(Material.MUSIC_DISC_11);
            EmptyPetRegistry._stack.editMeta(itemMeta -> {
                itemMeta.displayName(Component.text("Nothing but us owls..", TextColor.color(0x665547)));
            });
        }
    }
    public static final HashMap<String, ApplePetRegistry> registry = new HashMap<>();
    public static ApplePetRegistry GetOrDefault(String uuid) {
        EnsureEmptyExists();
        return registry.getOrDefault(uuid,EmptyPetRegistry);
    }
    String type;
    String display;
    String description;
    String uuid;
    String item;
    String data;

    public enum AI_TYPES {
        AI_FOLLOW("FOLLOW"),
        AI_CIRCLE("CIRCLE"),
        AI_FLOAT("FLOAT")
        ;

        private final String text;
        AI_TYPES(final String text) {
            this.text = text;
        }
        @Override
        public String toString() {
            return text;
        }

    }
    int variant;
    String textcolor;
    int baby = 0;

    public int getBaby() {
        return baby;
    }

    public String getTextcolor() {
        return textcolor;
    }

    public String getType() {return type;}
    public String getDisplay() {return display;}
    public String getDescription() {return description;}
    public String getUuid() {return uuid;}
    public int getVariant() {return variant;}
    public String getItem() {return item;}

    public void setType(String t) {type = t;}
    public void setDisplay(String d) {display = d;}
    public void setDescription(String d) {description = d;}
    public ItemStack _stack = new ItemStack(Material.AIR);
    public void setUuid(String u) {
        uuid = u;
        registry.put(u, this);
    }
    public void setVariant(int v) {variant = v;}
    public void setItem(String item) {this.item = item;}

    public void setBaby(int baby) {
        this.baby = baby;
    }

    public void setData(String data) { this.data = data; }

    public String getData() { return data; }

    public CompoundTag mGetData() {
        return alib.getNBTFromString(data);
    }
    @Override
    public String toString() {
        return "ApplePetRegistry{" +
                " \n    type='" + type + '\'' +
                ",\n    display='" + display + '\'' +
                ",\n    description='" + description + '\'' +
                ",\n    uuid='" + uuid + '\'' +
                ",\n    item='" + item + '\'' +
                ",\n    variant=" + variant +
                ",\n    baby=" + baby + '\n' +
            '}';
    }
    public static ApplePetRegistry EmptyPetRegistry = new ApplePetRegistry();
    {
        this._stack = new ItemStack(Material.MUSIC_DISC_CHIRP);
        this._stack.editMeta(itemMeta -> {
            itemMeta.displayName(Component.text("Nothing here but crickets..", TextColor.color(0x8f1c4f)));
            itemMeta.lore(new ArrayList<>(){{add(Component.text("chirp.. chirp.."));add(Component.text("get it? I'll see myself out."));}});
        });
    }
    public ApplePetRegistry() {

    }

    public static ApplePetRegistry loadPetFile(File f) {
        EnsureEmptyExists();
        Yaml Y = new Yaml(new CustomClassLoaderConstructor(ApplePetRegistry.class.getClassLoader()));
        try {
            ApplePetRegistry r = Y.loadAs(new FileInputStream(f), ApplePetRegistry.class);
            String s = r.getItem();
            if (alib.EGG_COLORS.containsKey(s)) {
                r._stack = alib.EGG_COLORS.get(s);
                return r;
            }
            Material m = Material.getMaterial(s);
            if (m == null) {
                Applepets.getInstance().getLogger().log(Level.WARNING, String.format("Malformed pet YML file: Expected a valid Item identifier or existing color, got %s", s));
                return EmptyPetRegistry;
            }
            if (EntityType.fromName(r.getType()) == null) {
                Applepets.getInstance().getLogger().log(Level.WARNING, String.format("Malformed pet YML file: Expected a valid EntityType identifier, got %s", s));
                return EmptyPetRegistry;
            };
            r._stack = new ItemStack(m);

            return r;
        } catch (FileNotFoundException | YAMLException exception) {
            Applepets.getInstance().getLogger().log(Level.WARNING, String.format("Malformed or missing pet YML file: %s", exception.getMessage()));
        }
        return EmptyPetRegistry;
    }

    public static boolean saveApplePetRegistry(ApplePetRegistry r, String filename) {


        return true;
    }
    public Entity summon(ApplePetRegistry registry, String entityName, CraftWorld world, Location playerLocation, Player owner) {
        Random r = new Random();
        playerLocation.add(r.nextFloat(-1, 1), 0, r.nextFloat(-1, 1));

        NamespacedKey key = NamespacedKey.fromString(this.getType());
        assert key != null;
        String _type = this.getType();
        net.minecraft.world.entity.EntityType<?> ty =
                Registry.ENTITY_TYPE.get(
                        ResourceLocation.read(this.getType()).getOrThrow(false, error->{
                            Applepets.getInstance().getLogger().log(Level.WARNING, "Entity type " + _type + " was not found.");
                        }));

        //Entity e = new NonHostileEntity(ty, world.getHandle(), playerLocation, owner, registry);
        Entity _e = ty.spawn(((CraftWorld)world).getHandle(), null, owner, new BlockPos(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()), MobSpawnType.COMMAND, false, false);

        assert _e != null;
        _e.setCustomName(net.minecraft.network.chat.Component.literal(entityName).withStyle(ChatFormatting.GRAY));
        CompoundTag c = this.mGetData();
        if (_e instanceof LivingEntity a) {
            a.readAdditionalSaveData(c);

        }
        if (_e instanceof Mob e) {
            if (e instanceof AgeableMob a) {
                a.setBaby(registry.baby >= 1);
                a.ageLocked = true;
            }
            e.goalSelector.removeAllGoals();
            e.goalSelector.getAvailableGoals().forEach(goal -> {
                e.goalSelector.removeGoal(goal);
            });
            FollowPlayerGoal goal = new FollowPlayerGoal(e, 1.25f, 2.25f, 10.2f);
            goal.SetOwner(owner);
            e.goalSelector.addGoal(0, goal);
            e.goalSelector.addGoal(1,new LookAtPlayerGoal(e, Player.class, 30));
            e.aware = false;
            if (e instanceof Fox f) {
                f.setFoxType(registry.variant >= 1 ? Fox.Type.SNOW : Fox.Type.RED);
            }
            if (e instanceof Cat f) {
                switch (registry.variant) {
                    case 0 -> f.setCatVariant(CatVariant.WHITE);
                    case 1 -> f.setCatVariant(CatVariant.BLACK);
                    case 2 -> f.setCatVariant(CatVariant.RED);
                    case 3 -> f.setCatVariant(CatVariant.SIAMESE);
                    case 4 -> f.setCatVariant(CatVariant.BRITISH_SHORTHAIR);
                    case 5 -> f.setCatVariant(CatVariant.CALICO);
                    case 6 -> f.setCatVariant(CatVariant.PERSIAN);
                    case 7 -> f.setCatVariant(CatVariant.RAGDOLL);
                    case 8 -> f.setCatVariant(CatVariant.TABBY);
                    case 9 -> f.setCatVariant(CatVariant.ALL_BLACK);
                    case 10 -> f.setCatVariant(CatVariant.JELLIE);
                }
            }
            if (e instanceof Rabbit f) {
                f.setRabbitType(registry.variant);
            }
            if (e instanceof Panda p) {
                p.setMainGene(Panda.Gene.byId(registry.variant));
            }
            if (e instanceof Frog f) {
                switch (registry.variant) {
                    case 0 -> f.setVariant(FrogVariant.COLD);
                    case 1 -> f.setVariant(FrogVariant.WARM);
                    case 2 -> f.setVariant(FrogVariant.TEMPERATE);
                }
            }
        }
        Applepets.SPAWNED_PETS.put(owner.getUUID(), _e.getUUID());
        return _e;
    }
}
