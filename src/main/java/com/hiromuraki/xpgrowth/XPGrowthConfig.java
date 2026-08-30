package com.hiromuraki.xpgrowth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class XPGrowthConfig {
    private XPGrowthConfig() {
        registerDefaults();
    }

    public static XPGrowthConfig get() {
        return instance;
    }

    public int getLevelCap() {
        return levelCap;
    }

    public int getMilestoneStep() {
        return milestoneStep;
    }

    public boolean getMilestoneFeedback() {
        return milestoneFeedback;
    }

    public boolean getMilestoneBonus() {
        return milestoneBonus;
    }

    @SuppressWarnings("null")
    public Collection<AttributeRule> getRules() {
        return rules.values();
    }

    public static void load() {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var path = FabricLoader.getInstance().getConfigDir().resolve("xp-growth.json");
        var currentConfig = new XPGrowthConfig();

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, gson.toJson(currentConfig.toJson()), StandardCharsets.UTF_8);
                XPGrowth.LOGGER.info("Generated default config at {}", path);
            } catch (IOException e) {
                XPGrowth.LOGGER.warn("Failed to write default config file", e);
            }
        } else {
            try {
                @SuppressWarnings("null")
                var config = gson.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonObject.class);
                if (config.has("milestoneStep")) {
                    currentConfig.milestoneStep = Math.max(config.get("milestoneStep").getAsInt(), 1);
                }
                if (config.has("milestoneFeedback")) {
                    currentConfig.milestoneFeedback = config.get("milestoneFeedback").getAsBoolean();
                }
                if (config.has("milestoneBonus")) {
                    currentConfig.milestoneBonus = config.get("milestoneBonus").getAsBoolean();
                }

                if (config.has("attributes") && config.get("attributes").isJsonObject()) {
                    var attrs = config.getAsJsonObject("attributes");
                    for (var entry : currentConfig.rules.entrySet()) {
                        if (!attrs.has(entry.getKey())) {
                            continue;
                        }
                        var o = attrs.getAsJsonObject(entry.getKey());
                        var rule = entry.getValue();
                        var attr = new AttributeRule(
                                rule.key(),
                                rule.attribute(),
                                o.has("startLevel") ? o.get("startLevel").getAsInt() : rule.startLevel(),
                                o.has("maxLevel") ? o.get("maxLevel").getAsInt() : rule.maxLevel(),
                                o.has("step") ? o.get("step").getAsInt() : rule.step(),
                                o.has("stepBonus") ? o.get("stepBonus").getAsDouble() : rule.stepBonus(),
                                o.has("enabled") ? o.get("enabled").getAsBoolean() : rule.enabled());
                        entry.setValue(attr);
                    }
                }

                currentConfig.syncLevelCap();
                instance = currentConfig;

            } catch (Exception e) {
                XPGrowth.LOGGER.warn("Failed to load config file, using defaults", e);
            }
        }
    }

    public JsonObject toJson() {
        var root = new JsonObject();
        root.addProperty("milestoneStep", milestoneStep);
        root.addProperty("milestoneFeedback", milestoneFeedback);
        root.addProperty("milestoneBonus", milestoneBonus);

        var attrs = new JsonObject();
        rules.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("enabled", e.getValue().enabled());
                    o.addProperty("startLevel", e.getValue().startLevel());
                    o.addProperty("maxLevel", e.getValue().maxLevel());
                    o.addProperty("step", e.getValue().step());
                    o.addProperty("stepBonus", e.getValue().stepBonus());
                    attrs.add(e.getKey(), o);
                });
        root.add("attributes", attrs);
        return root;
    }

    private static XPGrowthConfig instance = new XPGrowthConfig();
    private int levelCap = 0;
    private int milestoneStep = 5;
    private boolean milestoneFeedback = true;
    private boolean milestoneBonus = true;
    private final Map<String, AttributeRule> rules = new LinkedHashMap<>();

    private void registerDefaults() {
        var maxLevel = 30;
        var attrSheet = List.of(
                // 血量：每 2 级增加 2 点血，30 级满
                new AttributeRule("max_health", Attributes.MAX_HEALTH,
                        10, maxLevel, 2, 2, true),
                // 潜水时间：每级提升
                new AttributeRule("oxygen_bonus", Attributes.OXYGEN_BONUS,
                        0, maxLevel, 1, 1.0 / maxLevel, true),
                // 击退抗性：每级提升
                new AttributeRule("knockback_resistance", Attributes.KNOCKBACK_RESISTANCE,
                        0, maxLevel, 1, 0.6 / maxLevel, true),
                // 爆炸击退抗性：每级提升
                new AttributeRule("explosion_knockback_resistance", Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
                        0, maxLevel, 1, 0.3 / maxLevel, true),
                // 掉落最大高度：15 级 + 0.5，30 级 + 1.0
                new AttributeRule("safe_fall_distance", Attributes.SAFE_FALL_DISTANCE,
                        0, maxLevel, 15, 0.5, true),
                // 掉落伤害：每级提升
                new AttributeRule("fall_damage_multiplier", Attributes.FALL_DAMAGE_MULTIPLIER,
                        0, maxLevel, 1, -0.2 / maxLevel, true),
                // 移速：每级提升
                new AttributeRule("movement_speed", Attributes.MOVEMENT_SPEED,
                        0, maxLevel, 1, 0.02 / maxLevel, true),
                // 水下移速：每级提升
                new AttributeRule("water_movement_efficiency", Attributes.WATER_MOVEMENT_EFFICIENCY,
                        0, maxLevel, 1, 0.2 / maxLevel, true),
                // 幸运值：每级提升
                new AttributeRule("luck", Attributes.LUCK,
                        0, maxLevel, 1, 1.0 / maxLevel, true),
                // 攻击伤害：每 5 级提升 0.5
                new AttributeRule("attack_damage", Attributes.ATTACK_DAMAGE,
                        0, maxLevel, 5, 0.5, true),
                // 攻击速度：每级提升
                new AttributeRule("attack_speed", Attributes.ATTACK_SPEED,
                        0, maxLevel, 1, 1.0 / maxLevel, true),
                // 破坏方块速度：每级提升
                new AttributeRule("block_break_speed", Attributes.BLOCK_BREAK_SPEED,
                        0, maxLevel, 1, 0.5 / maxLevel, true),
                // 水下挖掘时间：每级提升
                new AttributeRule("submerged_mining_speed", Attributes.SUBMERGED_MINING_SPEED,
                        0, maxLevel, 1, 0.5 / maxLevel, true),
                // 护甲：从 10 级开始，每 5 级给予 1 点，最大 4 点
                new AttributeRule("armor", Attributes.ARMOR,
                        10, maxLevel, 5, 1, true),
                // 护甲韧性：从 10 级开始，每 5 级给予 0.5 点，最大 2 点
                new AttributeRule("armor_toughness", Attributes.ARMOR_TOUGHNESS,
                        10, maxLevel, 5, 0.5, true),
                // 着火时间：每级提升
                new AttributeRule("burning_time", Attributes.BURNING_TIME,
                        0, maxLevel, 1, -1.0 / maxLevel, true));

        rules.clear();
        for (var attr : attrSheet) {
            rules.put(attr.key(), attr);
        }

        syncLevelCap();
    }

    private void syncLevelCap() {
        for (var rule : rules.values()) {
            if (rule.maxLevel() > levelCap) {
                levelCap = rule.maxLevel();
            }
        }
    }
}
