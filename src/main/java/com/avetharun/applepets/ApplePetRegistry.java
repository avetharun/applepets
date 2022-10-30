package com.avetharun.applepets;

import com.avetharun.applepets.mixin.NonHostileEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftAgeable;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    String ai;
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
    public String getAi() {return ai;}
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
    public void setAi(String a) {ai = a;}
    public void setVariant(int v) {variant = v;}
    public void setItem(String item) {this.item = item;}

    public void setBaby(int baby) {
        this.baby = baby;
    }

    @Override
    public String toString() {
        return "ApplePetRegistry{" +
                " \n    type='" + type + '\'' +
                ",\n    display='" + display + '\'' +
                ",\n    description='" + description + '\'' +
                ",\n    uuid='" + uuid + '\'' +
                ",\n    item='" + item + '\'' +
                ",\n    ai='" + ai + '\'' +
                ",\n    variant=" + variant +
                ",\n    baby=" + baby + '\n' +
            '}';
    }
    public static ApplePetRegistry EmptyPetRegistry = new ApplePetRegistry();
    ApplePetRegistry() {
        this._stack = new ItemStack(Material.MUSIC_DISC_CHIRP);
        this._stack.editMeta(itemMeta -> {
            itemMeta.displayName(Component.text("Nothing here but crickets..", TextColor.color(0x8f1c4f)));
            itemMeta.lore(new ArrayList<>(){{add(Component.text("chirp.. chirp.."));add(Component.text("get it? I'll see myself out."));}});
        });

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
    public NonHostileEntity summon(ApplePetRegistry registry, String entityName, World world, Location playerLocation, Player owner) {
        Random r = new Random();
        playerLocation.add(r.nextFloat(-1, 1), 0, r.nextFloat(-1, 1));

        NamespacedKey key = NamespacedKey.fromString(this.getType());
        assert key != null;
        net.minecraft.world.entity.EntityType ty =
                Registry.ENTITY_TYPE.get(
                        ResourceLocation.read(this.getType()).getOrThrow(false, error->{}));
            NonHostileEntity e = new NonHostileEntity(ty, ((CraftWorld) world).getHandle(), playerLocation, owner, registry);
        e.setCustomName(net.minecraft.network.chat.Component.literal(entityName).withStyle(ChatFormatting.RESET, ChatFormatting.GRAY));
        return e;
    }
}
