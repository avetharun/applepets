package com.avetharun.applepets.events;

import com.avetharun.applepets.commands.CommandPetGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class SearchPetEvents {
    public static void OnPlayerRightSlotClicked(Player p) {} // No item.
    public static void OnPlayerLeftSlotClicked(Player p) {
    } // Back button
    public static AnvilGUI.Response OnFinish(Player player, String s) {
        return AnvilGUI.Response.close();
    }
}
