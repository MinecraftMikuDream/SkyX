package cn.onea.skyX.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class SubCommand implements Comparable<SubCommand> {

    protected abstract boolean execute(CommandSender sender, Command command, String label, String[] args);

    protected void printSeparator(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "&m----------------------------------------------");
    }

    protected void printBlankLine(CommandSender sender) {
        sender.sendMessage("");
    }

    protected void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    public CommandInfo getInfo() {
        CommandInfo info = this.getClass().getAnnotation(CommandInfo.class);
        if (info == null) {
            System.err.println("CommandInfo annotation missing on " + this.getClass().getSimpleName());
        }
        return info;
    }

    public int compareTo(@NotNull SubCommand other) {
        return 0;
    }

}
