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

    public int getMilestoneInterval() {
        return milestoneInterval;
    }

    public boolean getFeedback() {
        return feedback;
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
                var config = gson.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonObject.class);
                if (config.has("levelCap")) {
                    currentConfig.levelCap = config.get("levelCap").getAsInt();
                }
                if (config.has("milestoneInterval")) {
                    currentConfig.milestoneInterval = config.get("milestoneInterval").getAsInt();
                }
                if (config.has("feedback")) {
                    currentConfig.feedback = config.get("feedback").getAsBoolean();
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
                        var perLevel = o.has("perLevel") ? o.get("perLevel").getAsDouble() : rule.perLevel();
                        entry.setValue(new AttributeRule(rule.key(), rule.attribute(), perLevel, enabled));
                    }
                }
            } catch (Exception e) {
                XPGrowth.LOGGER.warn("Failed to load config file, using defaults", e);
            }
        }

        instance = currentConfig;
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("levelCap", levelCap);
        root.addProperty("milestoneInterval", milestoneInterval);
        root.addProperty("feedback", feedback);
        JsonObject attrs = new JsonObject();
        for (AttributeRule rule : rules.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", rule.enabled());
            o.addProperty("perLevel", rule.perLevel());
            attrs.add(rule.key(), o);
        }
        root.add("attributes", attrs);
        return root;
    }

    private static XPGrowthConfig instance = new XPGrowthConfig();
    private int levelCap = 25;
    private int milestoneInterval = 5;
    private boolean feedback = true;
    private final Map<String, AttributeRule> rules = new LinkedHashMap<>();

    private void registerDefaults() {
        rules.clear();
        rules.put("max_health", new AttributeRule("max_health", Attributes.MAX_HEALTH, 0.8, true));
        rules.put("movement_speed", new AttributeRule("movement_speed", Attributes.MOVEMENT_SPEED, 0.0008, true));
        rules.put("knockback_resistance",
                new AttributeRule("knockback_resistance", Attributes.KNOCKBACK_RESISTANCE, 0.032, true));
        rules.put("block_break_speed",
                new AttributeRule("block_break_speed", Attributes.BLOCK_BREAK_SPEED, 0.02, true));
        rules.put("safe_fall_distance",
                new AttributeRule("safe_fall_distance", Attributes.SAFE_FALL_DISTANCE, 0.08, true));
        rules.put("attack_damage", new AttributeRule("attack_damage", Attributes.ATTACK_DAMAGE, 0.08, true));
        rules.put("luck", new AttributeRule("luck", Attributes.LUCK, 0.04, true));
        rules.put("attack_speed", new AttributeRule("attack_speed", Attributes.ATTACK_SPEED, 0.04, true));
        rules.put("armor", new AttributeRule("armor", Attributes.ARMOR, 0.16, true));
        rules.put("armor_toughness", new AttributeRule("armor_toughness", Attributes.ARMOR_TOUGHNESS, 0.08, true));
        rules.put("burning_time", new AttributeRule("burning_time", Attributes.BURNING_TIME, -0.04, true));
    }
}