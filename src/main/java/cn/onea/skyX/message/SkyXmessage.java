package cn.onea.skyX.message;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class SkyXmessage {
    public void sendMessage(String checkName, String PlayerName, String PlayerVl, String reasen) {
        Bukkit.getServer().broadcastMessage("[" + ChatColor.RED + "Skyx" + ChatColor.WHITE + "] " + PlayerName + " failed " + ChatColor.GRAY + checkName + ChatColor.RED + " vl:" + PlayerVl + ChatColor.WHITE + " " + reasen);
    }
}
