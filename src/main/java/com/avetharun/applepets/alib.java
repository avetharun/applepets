package com.avetharun.applepets;

import net.minecraft.nbt.CompoundTag;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
public class alib {

    public static CompoundTag getNBTFromString(String input) {
        // Check if the input is a compound tag or a list
        if (!input.startsWith("{") || !input.endsWith("}")) {
            if (!input.startsWith("[") || !input.endsWith("]")) {
                throw new IllegalArgumentException("Input is not a compound tag or a list");
            }
        }

        // Remove the curly braces or square brackets from the input
        input = input.substring(1, input.length() - 1);

        // Split the input into key-value pairs or list elements
        String[] pairsOrElements = input.split(",");

        CompoundTag compound = new CompoundTag();
        for (int i = 0; i < pairsOrElements.length; i++) {
            String pairOrElement = pairsOrElements[i];
            // If the input is a compound tag, split the pair into a key and a value
            if (pairOrElement.contains(":")) {
                String[] keyValue = pairOrElement.split(":");
                String key = keyValue[0];
                String value = keyValue[1];

                // Check if the value is a compound tag or a list
                int curlyBraceCount = value.length() - value.replace("{", "").length();
                int squareBracketCount = value.length() - value.replace("[", "").length();
                if (curlyBraceCount % 2 == 0 && squareBracketCount % 2 == 0) {
                    // Determine the type of the value
                    if (value.startsWith("[B;")) {
                        // The value is a byte array
                        String[] values = value.substring(3, value.length() - 1).split(",");
                        byte[] bytes = new byte[values.length];
                        for (int j = 0; j < values.length; j++) {
                            bytes[j] = Byte.parseByte(values[j]);
                        }
                        compound.putByteArray(key, bytes);
                    } else if (value.startsWith("[I;")) {
                        // The value is an int array
                        String[] values = value.substring(3, value.length() - 1).split(",");
                        int[] ints = new int[values.length];
                        for (int j = 0; j < values.length; j++) {
                            ints[j] = Integer.parseInt(values[j]);
                        }
                        compound.putIntArray(key, ints);
                    } else if (value.endsWith("b")) {
                        // The value is a byte
                        compound.putByte(key, Byte.parseByte(value.substring(0, value.length() - 1)));
                    } else if (value.endsWith("s")) {
                        // The value is a short
                        compound.putShort(key, Short.parseShort(value.substring(0, value.length() - 1)));
                    } else if (value.endsWith("i")) {
                        // The value is an int
                        compound.putInt(key, Integer.parseInt(value.substring(0, value.length() - 1)));
                    } else if (value.endsWith("l")) {
                        // The value is a long
                        compound.putLong(key, Long.parseLong(value.substring(0, value.length() - 1)));
                    } else if (value.endsWith("f")) {
                        // The value is a float
                        compound.putFloat(key, Float.parseFloat(value.substring(0, value.length() - 1)));
                    } else if (value.endsWith("d")) {
                        // The value is a double
                        compound.putDouble(key, Double.parseDouble(value.substring(0, value.length() - 1)));
                    } else if (value.startsWith("{") && value.endsWith("}")) {
                        // The value is a compound tag
                        compound.put(key, getNBTFromString(value));
                    } else if (value.startsWith("[") && value.endsWith("]")) {
                        // The value is a list
                        compound.put(key, getNBTFromString(value));
                    } else {
                        // The value is a string
                        compound.putString(key, value);
                    }
                } else {
                    // The value is a string
                    compound.putString(key, value);
                }
            } else {
                // If the input is a list, determine the type of the element
                // Check if the element is a compound tag or a list
                int curlyBraceCount = pairOrElement.length() - pairOrElement.replace("{", "").length();
                int squareBracketCount = pairOrElement.length() - pairOrElement.replace("[", "").length();
                if (curlyBraceCount % 2 == 0 && squareBracketCount % 2 == 0) {
                    // Determine the type of the element
                    if (pairOrElement.startsWith("[B;")) {
                        // The element is a byte array
                        String[] values = pairOrElement.substring(3, pairOrElement.length() - 1).split(",");
                        byte[] bytes = new byte[values.length];
                        for (int j = 0; j < values.length; j++) {
                            bytes[j] = Byte.parseByte(values[j]);
                        }
                        compound.putByteArray(Integer.toString(i), bytes);
                    } else if (pairOrElement.startsWith("[I;")) {
                        // The element is an int array
                        String[] values = pairOrElement.substring(3, pairOrElement.length() - 1).split(",");
                        int[] ints = new int[values.length];
                        for (int j = 0; j < values.length; j++) {
                            ints[j] = Integer.parseInt(values[j]);
                        }
                        compound.putIntArray(Integer.toString(i), ints);
                    } else if (pairOrElement.endsWith("b")) {
                        // The element is a byte
                        compound.putByte(Integer.toString(i), Byte.parseByte(pairOrElement.substring(0, pairOrElement.length() - 1)));
                    } else if (pairOrElement.endsWith("s")) {
                        // The element is a short
                        compound.putShort(Integer.toString(i), Short.parseShort(pairOrElement.substring(0, pairOrElement.length() - 1)));
                    } else if (pairOrElement.endsWith("i")) {
                        // The element is an int
                        compound.putInt(Integer.toString(i), Integer.parseInt(pairOrElement.substring(0, pairOrElement.length() - 1)));
                    }
                    else if (pairOrElement.endsWith("l")) {
                        // The element is a long
                        compound.putLong(Integer.toString(i), Long.parseLong(pairOrElement.substring(0, pairOrElement.length() - 1)));
                    } else if (pairOrElement.endsWith("f")) {
                        // The element is a float
                        compound.putFloat(Integer.toString(i), Float.parseFloat(pairOrElement.substring(0, pairOrElement.length() - 1)));
                    } else if (pairOrElement.endsWith("d")) {
                        // The element is a double
                        compound.putDouble(Integer.toString(i), Double.parseDouble(pairOrElement.substring(0, pairOrElement.length() - 1)));
                    } else if (pairOrElement.startsWith("{") && pairOrElement.endsWith("}")) {
                        // The element is a compound tag
                        compound.put(Integer.toString(i), getNBTFromString(pairOrElement));
                    } else if (pairOrElement.startsWith("[") && pairOrElement.endsWith("]")) {
                        // The element is a list
                        compound.put(Integer.toString(i), getNBTFromString(pairOrElement));
                    } else {
                        // The element is a string
                        compound.putString(Integer.toString(i), pairOrElement);
                    }
                } else {
                    // The element is a string
                    compound.putString(Integer.toString(i), pairOrElement);
                }
            }
        }

        return compound;
    }


    public static String md5(String base) {

        try{
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static String HashPlayerPet(String playerUUID, String petUUID) {
        String t = String.valueOf(Instant.now().toEpochMilli());
        return md5(t + playerUUID + petUUID).substring(0,6);
    }
    public static final HashMap<String, ItemStack> EGG_COLORS = new HashMap<>(){{
        put("WHITE", new ItemStack(Material.GHAST_SPAWN_EGG)); // ghast
        put("GRAY", new ItemStack(Material.ELDER_GUARDIAN_SPAWN_EGG)); // eld. guardian
        put("DARKGRAY", new ItemStack(Material.SILVERFISH_SPAWN_EGG)); // silverfish
        put("BLACK", new ItemStack(Material.WITHER_SKELETON_SPAWN_EGG)); // wither
        put("MAROON", new ItemStack(Material.STRIDER_SPAWN_EGG)); // strider
        put("DARKRED", new ItemStack(Material.SPIDER_SPAWN_EGG)); // spider
        put("GREEN", new ItemStack(Material.SLIME_SPAWN_EGG)); // slime
        put("DARKGREEN", new ItemStack(Material.ZOMBIE_HORSE_SPAWN_EGG)); // zombie horse
        put("AQUA", new ItemStack(Material.ALLAY_SPAWN_EGG)); // allay
        put("BLUE", new ItemStack(Material.WARDEN_SPAWN_EGG)); // warden
        put("DARKBLUE", new ItemStack(Material.SQUID_SPAWN_EGG)); // squid
        put("PINK", new ItemStack(Material.AXOLOTL_SPAWN_EGG)); // axolotl
        put("CORAL", new ItemStack(Material.PIG_SPAWN_EGG)); // pig
        put("PURPLE", new ItemStack(Material.SHULKER_SPAWN_EGG)); // shulker
        put("CYAN", new ItemStack(Material.DROWNED_SPAWN_EGG)); // drowned
        put("YELLOW", new ItemStack(Material.BLAZE_SPAWN_EGG)); // blaze
        put("PALEBROWN", new ItemStack(Material.LLAMA_SPAWN_EGG)); // llama
        put("BROWN", new ItemStack(Material.RABBIT_SPAWN_EGG)); // rabbit
        put("DARKBROWN", new ItemStack(Material.VILLAGER_SPAWN_EGG)); // villager
        put("ORANGE", new ItemStack(Material.FROG_SPAWN_EGG)); // frog
    }};
    public static int GetInventoryPos(int width, int x, int y) {
        return ((y-1) * (width) + (x-1));
        // zero indexes my beloved
    }
    public static AnvilGUI.Builder CreateNewAnvilGUIBuilder(Plugin source,
                                             String title, String ItemText,
                                             ItemStack inputLeft, ItemStack inputRight,
                                             boolean preventClosing,
                                             Consumer<Player> closeListener,
                                             Consumer<Player> inputLeftClickListener,
                                             Consumer<Player> inputRightClickListener,
                                             BiFunction<Player, String, AnvilGUI.Response> outputClickListener) {
        AnvilGUI.Builder b = new AnvilGUI.Builder().plugin(source).text(ItemText).title(title)
                .itemLeft(inputLeft).itemRight(inputRight)
                .onLeftInputClick(inputLeftClickListener)
                .onRightInputClick(inputRightClickListener)
                .onClose(closeListener)
                .onComplete(outputClickListener);
        if (preventClosing) { b = b.preventClose();}
        return b;
    }
    public static AnvilGUI CreateNewAnvilGUIAndOpen(Plugin source, Player player,
                                                            String title, String ItemText,
                                                            ItemStack inputLeft, ItemStack inputRight,
                                                            boolean preventClosing,
                                                            Consumer<Player> closeListener,
                                                            Consumer<Player> inputLeftClickListener,
                                                            Consumer<Player> inputRightClickListener,
                                                            BiFunction<Player, String, AnvilGUI.Response> outputClickListener) {
        return CreateNewAnvilGUIBuilder(source, title, ItemText, inputLeft, inputRight, preventClosing, closeListener, inputLeftClickListener, inputRightClickListener, outputClickListener).open(player);
    }

}
