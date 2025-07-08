package cn.onea.skyX.command;

import cn.onea.skyX.command.impl.CommandVersion;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class SkyCommandExecutor implements CommandExecutor {
    private final List<SubCommand> commands = new ArrayList<>();

    public SkyCommandExecutor() {
        commands.add(new CommandVersion());
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("medusa.commands")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.AQUA + "Skyx AntiCheat Commands:\n");
            for (SubCommand cmd : commands) {
                sender.sendMessage(ChatColor.GREEN + "Use /Skyx " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
            }
            sender.sendMessage("");
            return true;
        }

        String sub = args[0];
        for (SubCommand cmd : commands) {
            String name = cmd.getInfo().name();
            if (!name.equals(sub)) {
                continue;
            }
            if (!sender.hasPermission("medusa." + name)) {
                return false;
            }
            boolean success = cmd.execute(sender, command, label, args);
            if (!success) {
                sender.sendMessage(ChatColor.GREEN + "Use /Skyx " + cmd.getInfo().name() + " " + cmd.getInfo().syntax());
            }
            return true;
        }

        return false;
    }
}
