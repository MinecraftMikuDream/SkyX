package cn.onea.skyX.check.impl.reach;

import cn.onea.skyX.SkyX;
import cn.onea.skyX.check.Check;
import cn.onea.skyX.config.ConfigManager;
import cn.onea.skyX.message.SkyXmessage;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReachA extends Check {
    private int nowvl;
    private final Map<UUID, Integer> attackRecords = new ConcurrentHashMap<>();
    private final Map<Integer, Long> lastDamageTimes = new ConcurrentHashMap<>();

    // 配置值
    private double maxReach;
    private int checkDelayTicks;
    private int vlThreshold;
    private double pingFactor;
    private double verticalSpeedFactor;
    private double maxAdditionalReach;
    private String kickMessage;

    public ReachA() {
        super("ReachA", 10,SkyX.getInstance().getConfigManager());
        System.out.println("ReachA 已加载");

        // 加载配置值
        loadConfigValues();
    }

    private void loadConfigValues() {
        maxReach = configManager.getDouble("reach.max-reach");
        checkDelayTicks = configManager.getInt("reach.check-delay-ticks");
        vlThreshold = configManager.getInt("reach.vl-threshold");
        pingFactor = configManager.getDouble("reach.ping-factor");
        verticalSpeedFactor = configManager.getDouble("reach.vertical-speed-factor");
        maxAdditionalReach = configManager.getDouble("reach.max-additional-reach");
        kickMessage = configManager.getString("reach.kick-message");

        // 设置检测阈值
        setThreshold(vlThreshold);
    }

    private void setThreshold(int vlThreshold) {
        this.vlThreshold = vlThreshold;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            Player player = event.getPlayer();

            if (player.isInsideVehicle() ||
                    player.getGameMode() == GameMode.CREATIVE ||
                    packet.getHand() != InteractionHand.MAIN_HAND) {
                return;
            }

            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                int entityId = packet.getEntityId();

                // 记录攻击事件
                attackRecords.put(player.getUniqueId(), entityId);

                // 延迟检测
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        processDelayedCheck(player, entityId);
                    }
                }.runTaskLater(SkyX.getInstance(), checkDelayTicks);
            }
        }
    }

    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            lastDamageTimes.put(event.getEntity().getEntityId(), System.currentTimeMillis());
        }
    }

    private boolean wasRecentlyDamaged(LivingEntity entity, int delayTicks) {
        Long lastDamageTime = lastDamageTimes.get(entity.getEntityId());
        if (lastDamageTime == null) return false;

        // 计算伤害是否发生在延迟时间内
        long damageDelayMillis = delayTicks * 50L; // ticks 转毫秒
        return (System.currentTimeMillis() - lastDamageTime) <= damageDelayMillis;
    }

    private void processDelayedCheck(Player player, int entityId) {
        // 检查攻击记录是否还存在（防止多次检测）
        if (!attackRecords.containsKey(player.getUniqueId()) ||
                attackRecords.get(player.getUniqueId()) != entityId) {
            return;
        }

        // 清除记录
        attackRecords.remove(player.getUniqueId());

        Entity target = findEntityById(player, entityId);
        if (target == null) return;

        // 只检测活体实体
        if (!(target instanceof LivingEntity)) return;
        LivingEntity livingTarget = (LivingEntity) target;

        // 检查实体是否在延迟时间内受到伤害
        if (!wasRecentlyDamaged(livingTarget, checkDelayTicks)) {
            return;
        }

        // 计算距离
        double distance = player.getLocation().distance(livingTarget.getLocation());

        // 动态阈值调整
        double maxAllowedReach = getDynamicMaxReach(player, livingTarget, distance);

        if (distance > maxAllowedReach) {
            handleViolation(player, distance);
        }
    }

    private double getDynamicMaxReach(Player player, LivingEntity target, double currentDistance) {
        double baseReach = maxReach;

        // 实体在空中时增加容差
        if (!target.isOnGround()) {
            Vector velocity = target.getVelocity();
            double verticalSpeed = Math.abs(velocity.getY());
            baseReach += verticalSpeed * verticalSpeedFactor;
            baseReach = Math.min(baseReach, maxReach + maxAdditionalReach);
        }

        // 考虑玩家延迟
        baseReach += player.spigot().getPing() / 100.0 * pingFactor;

        return baseReach;
    }

    private void handleViolation(Player player, double distance) {
        nowvl++;
        if (nowvl > vlThreshold) {
            KickPlayer(player, distance);
            nowvl = 0;
        } else {
            SkyXmessage skyXmessage = new SkyXmessage();
            skyXmessage.sendMessage("ReachA", player.getName(),
                    String.valueOf(nowvl), "distance: " + String.format("%.2f", distance));
        }
    }

    private void KickPlayer(Player player, double distance) {
        Bukkit.getScheduler().runTask(SkyX.getInstance(), () -> {
            String formattedMessage = kickMessage.replace("%distance%", String.format("%.2f", distance));
            player.kickPlayer(formattedMessage);
        });
    }

    private Entity findEntityById(Player player, int entityId) {
        if (player.getEntityId() == entityId) {
            return player;
        }

        if (player.isInsideVehicle() && player.getVehicle().getEntityId() == entityId) {
            return player.getVehicle();
        }

        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity.getEntityId() == entityId) {
                return entity;
            }
        }

        return null;
    }

    public double calculateDynamicMaxReach(Player player, LivingEntity target, double currentDistance) {
        return getDynamicMaxReach(player, target, currentDistance);
    }
}