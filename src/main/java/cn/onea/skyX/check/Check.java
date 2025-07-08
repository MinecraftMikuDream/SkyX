package cn.onea.skyX.check;

import cn.onea.skyX.SkyX;
import cn.onea.skyX.config.ConfigManager;
import com.github.retrooper.packetevents.event.PacketListener;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
@Setter
public abstract class Check implements PacketListener {
    private int setMaxVL;
    private final SkyX plugin = SkyX.getInstance();

    private String checkName;
    private String displayName;
    public final ConfigManager configManager;


    public Check(String checkName, int setMaxVL, ConfigManager configManager) {
        this.checkName = checkName;
        this.setMaxVL = setMaxVL;
        this.configManager = configManager;
    }

    public void KickPlayer(Player player, String checkName){
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.kickPlayer("§c§lSkyX §7§l> §c§l检测到 " + player.getName() + " 触发了 " + checkName + " 检查");
        });
    }

}
