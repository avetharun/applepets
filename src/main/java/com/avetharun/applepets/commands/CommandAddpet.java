package com.avetharun.applepets.commands;

import com.avetharun.applepets.ApplePetRegistry;
import com.avetharun.applepets.PlayerPetFile;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

public class CommandAddpet implements CommandExecutor {
    static void onCloseInputWindow(Player p) {
        p.sendMessage(Component.text("You closed the input window, cancelling."));
    }
    static AnvilGUI.Builder inputMobTypeB = new AnvilGUI.Builder().onClose(CommandAddpet::onCloseInputWindow);
    static AnvilGUI.Builder inputMobNameB = new AnvilGUI.Builder().onClose(CommandAddpet::onCloseInputWindow);
    static AnvilGUI.Builder inputMobAge   = new AnvilGUI.Builder().onClose(CommandAddpet::onCloseInputWindow);
    static AnvilGUI.Builder inputMobFilename    = new AnvilGUI.Builder().onClose(CommandAddpet::onCloseInputWindow);

    static {
        inputMobTypeB.itemRight(CommandPetGUI.DEFAULT_BACK_ITEM);
        inputMobNameB.itemRight(CommandPetGUI.DEFAULT_BACK_ITEM);
        inputMobAge.itemRight(CommandPetGUI.DEFAULT_BACK_ITEM);
        inputMobFilename.itemRight(CommandPetGUI.DEFAULT_BACK_ITEM);
    };
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ApplePetRegistry r = new ApplePetRegistry();
        

        return true;
    }
}
