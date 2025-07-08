package cn.onea.skyX.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private File configFile;
    @Getter
    private FileConfiguration config;
    private final Map<String, Object> defaultValues = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupDefaultValues();
        reloadConfig();
    }

    private void setupDefaultValues() {
        // 通用设置
        defaultValues.put("prefix", "&8[&cSkyX&8]");
        defaultValues.put("language", "zh_CN");

        // Reach
        defaultValues.put("reach.enabled", true);
        defaultValues.put("reach.max-reach", 3.3);
        defaultValues.put("reach.check-delay-ticks", 2);
        defaultValues.put("reach.hurt-time-threshold", 8);
        defaultValues.put("reach.vl-threshold", 10);
        defaultValues.put("reach.ping-factor", 0.1);
        defaultValues.put("reach.vertical-speed-factor", 0.5);
        defaultValues.put("reach.max-additional-reach", 1.0);
        defaultValues.put("reach.kick-message", "&c检测到异常攻击距离 (%.2f)");

    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
            plugin.getLogger().info("配置文件已生成");
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // 确保所有默认值都存在
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
            }
        }

        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存配置文件: " + e.getMessage());
        }
    }

    // 获取值的方法
    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }
}
