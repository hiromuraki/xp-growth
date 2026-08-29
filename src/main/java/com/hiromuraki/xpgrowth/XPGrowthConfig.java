package com.hiromuraki.xpgrowth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
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

    public Map<String, AttributeRule> getRules() {
        return rules;
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
                if (config.has("levelCap")) {
                    currentConfig.levelCap = Math.max(config.get("levelCap").getAsInt(), 1);
                }
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
                        var enabled = o.has("enabled") ? o.get("enabled").getAsBoolean() : rule.enabled();
                        var maxBonus = o.has("maxBonus") ? o.get("maxBonus").getAsDouble() : rule.maxBonus();
                        entry.setValue(new AttributeRule(rule.key(), rule.attribute(), maxBonus, enabled));
                    }
                }
            } catch (Exception e) {
                XPGrowth.LOGGER.warn("Failed to load config file, using defaults", e);
            }
        }

        instance = currentConfig;
    }

    public JsonObject toJson() {
        var root = new JsonObject();
        root.addProperty("levelCap", levelCap);
        root.addProperty("milestoneStep", milestoneStep);
        root.addProperty("milestoneFeedback", milestoneFeedback);
        root.addProperty("milestoneBonus", milestoneBonus);

        var attrs = new JsonObject();
        for (AttributeRule rule : rules.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", rule.enabled());
            o.addProperty("maxBonus", rule.maxBonus());
            attrs.add(rule.key(), o);
        }
        root.add("attributes", attrs);
        return root;
    }

    private static XPGrowthConfig instance = new XPGrowthConfig();
    private int levelCap = 30;
    private int milestoneStep = 5;
    private boolean milestoneFeedback = true;
    private boolean milestoneBonus = true;
    private final Map<String, AttributeRule> rules = new LinkedHashMap<>();

    private void registerDefaults() {
        rules.clear();
        rules.put("max_health", new AttributeRule("max_health", Attributes.MAX_HEALTH, 20, true));
        rules.put("movement_speed", new AttributeRule("movement_speed", Attributes.MOVEMENT_SPEED, 0.02, true));
        rules.put("water_movement_efficiency",
                new AttributeRule("water_movement_efficiency", Attributes.WATER_MOVEMENT_EFFICIENCY, 0.2, true));
        rules.put("knockback_resistance",
                new AttributeRule("knockback_resistance", Attributes.KNOCKBACK_RESISTANCE, 0.8, true));
        rules.put("block_break_speed",
                new AttributeRule("block_break_speed", Attributes.BLOCK_BREAK_SPEED, 0.5, true));
        rules.put("safe_fall_distance",
                new AttributeRule("safe_fall_distance", Attributes.SAFE_FALL_DISTANCE, 2, true));
        rules.put("attack_damage", new AttributeRule("attack_damage", Attributes.ATTACK_DAMAGE, 2, true));
        rules.put("luck", new AttributeRule("luck", Attributes.LUCK, 1, true));
        rules.put("attack_speed", new AttributeRule("attack_speed", Attributes.ATTACK_SPEED, 1, true));
        rules.put("armor", new AttributeRule("armor", Attributes.ARMOR, 4, true));
        rules.put("armor_toughness", new AttributeRule("armor_toughness", Attributes.ARMOR_TOUGHNESS, 2, true));
        rules.put("burning_time", new AttributeRule("burning_time", Attributes.BURNING_TIME, -1, true));
    }
}