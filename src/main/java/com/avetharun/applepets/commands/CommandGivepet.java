package com.avetharun.applepets.commands;

import com.avetharun.applepets.Applepets;
import com.avetharun.applepets.PlayerPetFile;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CommandGivepet implements CommandExecutor {
    public static final TabCompleter completer = new TabCompleter() {
        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (sender instanceof Player player) {
                Applepets instance = Applepets.getInstance();
            }
            return null;
        }
    };
    public CommandGivepet() {
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(String.format("Error trying to add pet to %s: Need 2 arguments, found %d", sender.getName(), args.length));
            return true;
        }
        if (sender.hasPermission(Applepets.Permissions.ADMIN_GIVEPET) && sender instanceof Player player) {
            String out = PlayerPetFile.AddPetToPlayerOnline(Applepets.getInstance().getServer().getPlayerUniqueId(args[0]), args[1]);
            if (!out.equals("EXISTS")) {
                sender.sendMessage(String.format("Added pet with internal UUID %s to player %s using ID %s", args[1], args[0], out));
            }
        }
        // Silently return.
        return true;
    }
}
