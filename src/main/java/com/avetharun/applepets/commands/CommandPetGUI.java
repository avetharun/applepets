package com.avetharun.applepets.commands;

import com.avetharun.applepets.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class CommandPetGUI implements CommandExecutor {
    public static final int GUI_WIDTH = 9;
    public static final int GUI_HEIGHT = 4;
    public static final ItemStack DEFAULT_BACKWARDS_ITEM = new ItemStack(Material.ECHO_SHARD);
    public static final ItemStack DEFAULT_BACK_ITEM = new ItemStack(Material.ECHO_SHARD);
    public static final ItemStack DEFAULT_FORWARD_ITEM = new ItemStack(Material.AMETHYST_SHARD);
    public static final ItemStack DEFAULT_GIFT_ITEM = new ItemStack(Material.FILLED_MAP);
    public CommandPetGUI() {
        DEFAULT_GIFT_ITEM.editMeta(MapMeta.class, (m -> {
            m.setColor(Color.MAROON);
            m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            m.displayName(Component.text("Give another player a pet", TextColor.color(0xdb7991)));
            m.addEnchant(Enchantment.CHANNELING, 1, true);
            m.getPersistentDataContainer().set(new NamespacedKey(Applepets.getInstance(), "GUI_LOCK"), PersistentDataType.INTEGER, 1);
            m.getPersistentDataContainer().set(new NamespacedKey(Applepets.getInstance(), "GUI_ITEM"), PersistentDataType.STRING, "GIFT_OWNED");
        }));
    }
    static void Open(Player player) {
        Inventory ui = Bukkit.createInventory(null, 9*4, Component.text(String.format("%s's Pets", player.getName())));
        player.openInventory(ui);
        int start = 0;
        if (player.hasPermission(Applepets.Permissions.USER_GIFTPET)) {
            ui.setItem(alib.GetInventoryPos(GUI_WIDTH, 1, 4), DEFAULT_GIFT_ITEM);
            start = 1;
        }
        ui.setItem(alib.GetInventoryPos(GUI_WIDTH, start+1, 4), DEFAULT_BACK_ITEM);
        ui.setItem(alib.GetInventoryPos(GUI_WIDTH, start+2, 4), DEFAULT_FORWARD_ITEM);
        HashMap<String, UserApplePet> pets = PlayerPetFile.GetPlayerPets(player.getUniqueId()).getPets();
        if (pets.isEmpty()) {
            ui.addItem(ApplePetRegistry.EmptyPetRegistry._stack);
        }
        pets.forEach((uuid, pet)->{
            Applepets.getInstance().getLogger().log(Level.INFO, String.format("Getting pet using UUID %s", uuid));
            ApplePetRegistry r = ApplePetRegistry.GetOrDefault(pet.RegistryUUID);
            if (r == null) {
                Applepets.getInstance().getLogger().log(Level.WARNING, "Unable to find above pet. Using fallback.");
                r = ApplePetRegistry.EmptyPetRegistry;
            }
            ItemStack s = r._stack;
            ApplePetRegistry finalR = r;
            s.editMeta(meta->{
                String name = pet.getDisplay() == null? finalR.getDisplay() : pet.getDisplay();
                meta.displayName(Component.text(name, TextColor.color(0xcfcfcf)));
                meta.lore(new ArrayList<>(){{add(Component.text(finalR.getDescription()));}});
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(new NamespacedKey(Applepets.getInstance(), "PET_SPAWN_UUID"), PersistentDataType.STRING, finalR.getUuid() == null? "NOSPAWN": finalR.getUuid());
                container.set(new NamespacedKey(Applepets.getInstance(), "PET_NAME"), PersistentDataType.STRING, name);
                container.set(new NamespacedKey(Applepets.getInstance(), "GUI_ITEM"), PersistentDataType.STRING, "PET_SPAWNER");
                container.set(new NamespacedKey(Applepets.getInstance(), "PLAYER_PET_UUID"), PersistentDataType.STRING, uuid);
            });
            ui.addItem(s);
        });
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 0) {
            if (sender.hasPermission(Applepets.Permissions.ADMIN_GIVEPET) && Arrays.stream(args).findFirst().get().equalsIgnoreCase("reload")) {
                Applepets.ReloadAssets();
                sender.sendMessage("reloaded.");
                return true;
            }
            if (sender.hasPermission(Applepets.Permissions.ADMIN_GIVEPET) && Arrays.asList(args).contains("save")) {
                Applepets.ShutdownAndSaveAssets();
            }
        }
        if (sender instanceof Player player) {
            Open(player);
        }
        return true;
    }
}
