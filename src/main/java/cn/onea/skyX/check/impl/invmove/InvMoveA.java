package cn.onea.skyX.check.impl.invmove;

import cn.onea.skyX.SkyX;
import cn.onea.skyX.check.Check;
import cn.onea.skyX.message.SkyXmessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InvMoveA extends Check implements Listener {
    private int nowvl = 0;
    private final Set<UUID> openInventories = new HashSet<>();
    private final String kickMessage;

    public InvMoveA() {
        super("InvMoveA", 8,SkyX.getInstance().getConfigManager());
        this.kickMessage = configManager.getString("invmove.kick-message");
    }

    // inv open
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        openInventories.add(player.getUniqueId());
    }

    // inv close
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        openInventories.remove(event.getPlayer().getUniqueId());
    }

    // Player move
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // 检查是否移动（忽略头部转动）
        if (event.getFrom().getX() == event.getTo().getX() &&
                event.getFrom().getY() == event.getTo().getY() &&
                event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        // 检查是否打开容器
        if (openInventories.contains(player.getUniqueId())) {
            // InvMove 检测触发
            handleViolation(player);
        }
    }

    private void handleViolation(Player player) {
        nowvl++;
        if (nowvl > getSetMaxVL()) {
            KickPlayer(player);
            nowvl = 0;
        } else {
            SkyXmessage skyXmessage = new SkyXmessage();
            skyXmessage.sendMessage("ReachA", player.getName(),
                    String.valueOf(nowvl), "Move in the inventory");
        }
    }

    private void KickPlayer(Player player) {
        Bukkit.getScheduler().runTask(SkyX.getInstance(), () -> {
            String formattedMessage = kickMessage.replace("%reason%", "Move in the inventory");
            player.kickPlayer(formattedMessage);
        });
    }
}
