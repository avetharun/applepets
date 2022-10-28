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
        sender.sendMessage(String.format("%s", label));
        Arrays.stream(args).forEach(arg->{
            sender.sendMessage(String.format("Argument: %s", arg));
        });
        if (sender.hasPermission(Applepets.Permissions.ADMIN_GIVEPET) && sender instanceof Player player) {
            PlayerPetFile.GetPlayerPets(player.getUniqueId());
        }
        // Silently return.
        return true;
    }
}
