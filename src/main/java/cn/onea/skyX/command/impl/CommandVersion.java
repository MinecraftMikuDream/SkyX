package cn.onea.skyX.command.impl;

import cn.onea.skyX.command.CommandInfo;
import cn.onea.skyX.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name="Version", purpose="Get Skyx Version")
public class CommandVersion extends SubCommand {

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(ChatColor.GREEN + "Skyx Version: 1.0.0");
            return true;
        }
        return false;
    }
}
