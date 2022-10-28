package com.avetharun.applepets;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;

public class alib {
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
