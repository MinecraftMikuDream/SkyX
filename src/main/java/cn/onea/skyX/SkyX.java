package cn.onea.skyX;

import cn.onea.skyX.check.Check;
import cn.onea.skyX.check.impl.invmove.InvMoveA;
import cn.onea.skyX.check.impl.reach.ReachA;
import cn.onea.skyX.command.SkyCommandExecutor;
import cn.onea.skyX.config.ConfigManager;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class SkyX extends JavaPlugin {
    private static SkyCommandExecutor command;
    private ConfigManager configManager;
    private final List<Check> activeChecks = new ArrayList<>();

    public static SkyX getInstance() {
        return getPlugin(SkyX.class);
    }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        PacketEvents.getAPI().getEventManager().registerListener(
                new ReachA(), PacketListenerPriority.NORMAL);
    }

    @Override
    public void onEnable() {
        // 创建插件数据文件夹
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        configManager = new ConfigManager(this);

        if (!Bukkit.getVersion().contains("1.8.8")) {
            Logger logger = Bukkit.getLogger();
            logger.severe("Skyx is only compatible with 1.8.8 servers.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        PacketEvents.getAPI().init();
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);

        SkyCommandExecutor command = new SkyCommandExecutor();
        this.getCommand("skyx").setExecutor(command);

        if (configManager.getBoolean("reach.enabled") &&
            configManager.getBoolean("invmove.enabled")
        ) {
            ReachA reachA = new ReachA();
            InvMoveA invMoveA = new InvMoveA();
            PacketEvents.getAPI().getEventManager().registerListener(reachA, PacketListenerPriority.NORMAL);
            PacketEvents.getAPI().getEventManager().registerListener(invMoveA, PacketListenerPriority.NORMAL);
            activeChecks.add(reachA);
            activeChecks.add(invMoveA);
        }
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    private class EntityDamageListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onEntityDamage(EntityDamageEvent event) {
            // 通知所有检测器实体受伤事件
            for (Check check : activeChecks) {
                if (check instanceof ReachA) {
                    ((ReachA) check).onEntityDamage(event);
                }
            }
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
