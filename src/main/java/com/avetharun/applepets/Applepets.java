package com.avetharun.applepets;

import com.avetharun.applepets.commands.CommandGivepet;
import com.avetharun.applepets.commands.CommandPetGUI;
import com.avetharun.applepets.events.SearchPetEvents;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.LivingEntity;
import net.wesjd.anvilgui.AnvilGUI;
import net.wesjd.anvilgui.version.VersionMatcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.defaults.ReloadCommand;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class Applepets extends JavaPlugin implements Listener {
    private static Applepets instance;
    public static File APPLEPETS_PLAYERDATA_FOLDER;
    public static File APPLEPETS_PETDATA_FOLDER;
    public static class Permissions {
        public static final Permission ADMIN_GIVEPET = new Permission("applepets.admin.givepet");
        public static final Permission USER_GIFTPET = new Permission("applepets.giftpet");
        public static final Permission USER_PETGUI = new Permission("applepets.petgui");
    };
    public static PluginCommand AddPluginCommand(String name, CommandExecutor executor, @Nullable TabCompleter completer, @Nullable String description) {
        PluginCommand cmd = getInstance().getCommand(name);
        assert cmd != null;
        cmd.setName(name);
        cmd.setExecutor(executor);
        if (description != null) {
            cmd.setDescription(description);
        }
        if (completer != null) {
            cmd.setTabCompleter(completer);
        }
        return cmd;
    }
    public static PluginCommand AddPluginCommand(String name, CommandExecutor o) {
        return AddPluginCommand(name, o, null, null);
    }
    public static ArrayList<String> pathsToPets;
    @Override
    public void onEnable() {
        instance = this;

        // Plugin startup logic
        APPLEPETS_PETDATA_FOLDER = new File((this.getDataFolder() + File.separator + "pets"));
        APPLEPETS_PLAYERDATA_FOLDER = new File((this.getDataFolder() + File.separator + "players"));
        if (!APPLEPETS_PLAYERDATA_FOLDER.exists()) {APPLEPETS_PLAYERDATA_FOLDER.mkdirs();}
        if (!APPLEPETS_PETDATA_FOLDER.exists()) {APPLEPETS_PETDATA_FOLDER.mkdirs();}
        Bukkit.getPluginManager().registerEvents(this, this);
        AddPluginCommand("givepet", new CommandGivepet());
        AddPluginCommand("pet", new CommandPetGUI());
        ReloadAssets();
    }
    public static void ReloadAssets() {
        PlayerPetFile.PlayerPetFiles.clear();
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerPetFile.PushPlayerPetfile(player.getUniqueId());
            getInstance().getLogger().log(Level.INFO, String.format("%s's pets were loaded, as they are online during plugin startup", player.getName()));
        });
        File[] petfiles = APPLEPETS_PETDATA_FOLDER.listFiles();
        if (petfiles != null) {
            getInstance().getLogger().log(Level.INFO, "parsing pet files: count %d", petfiles.length);
            Arrays.stream(petfiles).forEach(ApplePetRegistry::loadPetFile);
        }

    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static Applepets getInstance() {
        return instance;
    }

    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {return;}
        if (event.getCurrentItem().getItemMeta() == null) {return;}
        String _item_type = event.getCurrentItem().getItemMeta().getPersistentDataContainer().getOrDefault(
                new NamespacedKey(Applepets.getInstance(), "GUI_ITEM"),
                PersistentDataType.STRING, "pass");
        if (!_item_type.equalsIgnoreCase("pass"))
        {
            switch (_item_type) {
                case "FORWARD" -> {
                    // TODO: add advanced ui controls
                }
                case "BACKWARD" -> {
                    // TODO: see above
                }
                case "GIFT_OWNED" -> {
                    // TODO: gift pets
                }
                case "PET_SPAWNER" -> {
                    ItemStack s = event.getCurrentItem();
                    ItemMeta m = s.getItemMeta();
                    String name = m.getPersistentDataContainer().get(new NamespacedKey(this, "PET_NAME"), PersistentDataType.STRING);

                    String uuid = m.getPersistentDataContainer().get(new NamespacedKey(this, "PET_SPAWN_UUID"), PersistentDataType.STRING);
                    ApplePetRegistry r = ApplePetRegistry.registry.get(uuid);
                    r.summon(name, event.getWhoClicked().getWorld(), event.getWhoClicked().getLocation(), ((CraftPlayer)event.getWhoClicked()).getHandle());
                }

                default -> {}
            }
        }
        if (event.getInventory().contains(CommandPetGUI.DEFAULT_GIFT_ITEM)) {
            event.setCancelled(true);
        }
    }
    public static HashMap<UUID, LivingEntity> SPAWNED_PETS = new HashMap<>();
    @EventHandler
    public void onLogin(PlayerLoginEvent event){
        PlayerPetFile.PushPlayerPetfile(event.getPlayer().getUniqueId());
    };
    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        PlayerPetFile.PopPlayerPetfile(event.getPlayer().getUniqueId());
        LivingEntity e = SPAWNED_PETS.get(event.getPlayer().getUniqueId());
        if (e != null) {
            e.getBukkitLivingEntity().remove();
            SPAWNED_PETS.remove(event.getPlayer().getUniqueId());
        }
    }
}