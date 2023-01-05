package com.avetharun.applepets;

import com.avetharun.applepets.commands.CommandAddpet;
import com.avetharun.applepets.commands.CommandGivepet;
import com.avetharun.applepets.commands.CommandPetGUI;
import com.avetharun.applepets.mixin.MixinEntityTypes;
import com.destroystokyo.paper.event.entity.CreeperIgniteEvent;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class Applepets extends JavaPlugin implements Listener {
    private static Applepets instance;
    public static HashMap<UUID, UUID> SPAWNED_PETS = new HashMap<>();
    public static File APPLEPETS_PLAYERDATA_FOLDER;
    public static File APPLEPETS_PETDATA_FOLDER;
    public static class Permissions {
        public static final Permission ADMIN_GIVEPET = new Permission("applepets.admin.givepet");
        public static final Permission USER_GIFTPET = new Permission("applepets.giftpet");
        public static final Permission USER_PETGUI = new Permission("applepets.petgui");
        public static final Permission USER_WITHDRAWPET = new Permission("applepets.withdrawpet");
        public static final Permission USER_EDITPET = new Permission("applepets.editpet");
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
        CraftServer s = (CraftServer) getServer();
        // Plugin startup logic
        APPLEPETS_PETDATA_FOLDER = new File((this.getDataFolder() + File.separator + "pets"));
        APPLEPETS_PLAYERDATA_FOLDER = new File((this.getDataFolder() + File.separator + "players"));
        if (!APPLEPETS_PLAYERDATA_FOLDER.exists()) {APPLEPETS_PLAYERDATA_FOLDER.mkdirs();}
        if (!APPLEPETS_PETDATA_FOLDER.exists()) {APPLEPETS_PETDATA_FOLDER.mkdirs();}
        Bukkit.getPluginManager().registerEvents(this, this);
        AddPluginCommand("givepet", new CommandGivepet());
        AddPluginCommand("pet", new CommandPetGUI());
        AddPluginCommand("addpet", new CommandAddpet());
        ReloadAssets();

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            SPAWNED_PETS.forEach(((uuid, uuid2) -> {
                this.getServer().getWorlds().forEach(world -> {
                    CraftEntity _e = ((CraftEntity)world.getEntity(uuid2));
                    if (_e == null) {
                        return;
                    }
                    if (_e.getHandle() instanceof Mob m) {
                        if (!m.aware) {
                            m.getNavigation().tick();
                            m.getMoveControl().tick();
                            m.getLookControl().tick();
                            m.goalSelector.tick();
                        }
                    }
                });
            }));
        }, 1, 1);

    }

    @Override
    public void onLoad() {
        initMixins();
        super.onLoad();
    }

    void initMixins() {}

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
    public static void ShutdownAndSaveAssets() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerPetFile.PopPlayerPetfile(player.getUniqueId());
            getInstance().getLogger().log(Level.INFO, String.format("%s's pets were unloaded, and saved.", player.getName()));
        });
    }
    @Override
    public void onDisable() {
        ShutdownAndSaveAssets();
        // Plugin shutdown logic
    }
    public static Applepets getInstance() {
        return instance;
    }
    @EventHandler
    public void onSheepDyeEvent(SheepDyeWoolEvent event) {
        if (SPAWNED_PETS.containsValue(event.getEntity().getUniqueId()) || SPAWNED_PETS.containsKey(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onSheepShearEvent(PlayerShearEntityEvent event) {
        if (SPAWNED_PETS.containsValue(event.getEntity().getUniqueId()) || SPAWNED_PETS.containsKey(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onCreeperIgniteEvent(CreeperIgniteEvent event) {
        if (SPAWNED_PETS.containsValue(event.getEntity().getUniqueId())) {
            event.setIgnited(false);
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        if (SPAWNED_PETS.containsValue(event.getRightClicked().getUniqueId()) || SPAWNED_PETS.containsKey(event.getRightClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerDamageItemEvent(PlayerItemDamageEvent event) {
        var r = event.getPlayer().getWorld().rayTraceEntities(event.getPlayer().getEyeLocation(), event.getPlayer().getEyeLocation().getDirection(), 3, entity -> entity != event.getPlayer());
        if (r == null) {return;}
        Location p = r.getHitPosition().toLocation(event.getPlayer().getWorld());
        org.bukkit.entity.Entity e = r.getHitEntity();
        if (e != null) {
            if (SPAWNED_PETS.containsValue(e.getUniqueId()) || SPAWNED_PETS.containsKey(e.getUniqueId())) {
                event.setCancelled(true);
            }
        }
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
                case "PET_EDIT_NAME" -> {}
                case "PET_WITHDRAW" -> {}
                case "PET_SPAWNER" -> {
                    ItemStack s = event.getCurrentItem();
                    ItemMeta m = s.getItemMeta();
                    String name = m.getPersistentDataContainer().get(new NamespacedKey(this, "PET_NAME"), PersistentDataType.STRING);
                    String uuid = m.getPersistentDataContainer().get(new NamespacedKey(this, "PET_SPAWN_UUID"), PersistentDataType.STRING);
                    if (event.isLeftClick()) {
                        if (uuid == null || uuid.equalsIgnoreCase("NOSPAWN")) {
                            event.setCancelled(true);
                            return;
                        }
                        if (SPAWNED_PETS.containsKey(event.getWhoClicked().getUniqueId())) {
                            org.bukkit.entity.Entity e = Bukkit.getEntity(SPAWNED_PETS.get(event.getWhoClicked().getUniqueId()));
                            if (e != null) {
                                e.remove();
                            }
                        }
                        ApplePetRegistry r = ApplePetRegistry.registry.get(uuid);
                        Entity e = r.summon(r, name, (CraftWorld) event.getWhoClicked().getWorld(), event.getWhoClicked().getLocation(),
                                ((CraftPlayer) event.getWhoClicked()).getHandle());
                    }
                    if (event.isRightClick()) {

                    }
                }

                default -> {}
            }
            event.setCancelled(true);
        }
        if (event.getInventory().contains(CommandPetGUI.DEFAULT_FORWARDS_ITEM) || event.getInventory().contains(CommandPetGUI.DEFAULT_BACKWARDS_ITEM)) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onLogin(PlayerLoginEvent event){
        PlayerPetFile.PushPlayerPetfile(event.getPlayer().getUniqueId());
    };
    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        PlayerPetFile.PopPlayerPetfile(event.getPlayer().getUniqueId());
        CraftEntity e = ((CraftEntity)Bukkit.getEntity(SPAWNED_PETS.get(event.getPlayer().getUniqueId())));
        if (e != null) {
            e.remove();
            SPAWNED_PETS.remove(event.getPlayer().getUniqueId());
        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (SPAWNED_PETS.containsValue(event.getEntity().getUniqueId())) {
            if (SPAWNED_PETS.containsKey(event.getDamager().getUniqueId())) {
                event.getEntity().remove();
            }
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onEntityDamageSelf(EntityDamageEvent event) {
        if (SPAWNED_PETS.containsValue(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
