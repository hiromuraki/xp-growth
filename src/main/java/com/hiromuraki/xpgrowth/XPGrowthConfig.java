package com.hiromuraki.xpgrowth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

record ConfigDto(
        Integer milestoneStep,
        Boolean milestoneFeedback,
        Boolean milestoneBonus,
        List<AttributeRuleDto> rules) {

    public ConfigDto {
        milestoneStep = milestoneStep == null ? 5 : Math.max(milestoneStep, 1);
    }
}

record AttributeRuleDto(
        String attributeKey,
        Integer startLevel,
        Integer maxLevel,
        Integer step,
        Double stepBonus,
        Boolean enabled) {
}

public final class XPGrowthConfig {
    public static int getLevelCap() {
        return levelCap;
    }

    public static int getMilestoneStep() {
        return milestoneStep;
    }

    public static boolean getMilestoneFeedback() {
        return milestoneFeedback;
    }

    public static boolean getMilestoneBonus() {
        return milestoneBonus;
    }

    @SuppressWarnings("null")
    public static Collection<AttributeRule> getRules() {
        return rules.values();
    }

    public static void load() {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var path = FabricLoader.getInstance().getConfigDir().resolve("xp-growth.json");
        var configDto = getDefaultConfigDto();

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, gson.toJson(configDto), StandardCharsets.UTF_8);
                XPGrowth.LOGGER.info("Generated default config at {}", path);
            } catch (IOException e) {
                XPGrowth.LOGGER.warn("Failed to write default config file", e);
            }
        } else {
            try {
                configDto = Objects
                        .requireNonNull(gson.fromJson(Files.readString(path, StandardCharsets.UTF_8), ConfigDto.class));

            } catch (Exception e) {
                XPGrowth.LOGGER.warn("Failed to load config file, using defaults", e);
            }
        }

        setFromDto(configDto);
    }

    private static int milestoneStep = 1;
    private static boolean milestoneFeedback = true;
    private static boolean milestoneBonus = true;
    private static int levelCap = 0;
    private static Map<String, AttributeRule> rules = new LinkedHashMap<>();
    @SuppressWarnings("null")
    private static Map<String, Holder<Attribute>> attributeMap = Map.ofEntries(
            Map.entry("max_health", Attributes.MAX_HEALTH),
            Map.entry("oxygen_bonus", Attributes.OXYGEN_BONUS),
            Map.entry("knockback_resistance", Attributes.KNOCKBACK_RESISTANCE),
            Map.entry("explosion_knockback_resistance", Attributes.EXPLOSION_KNOCKBACK_RESISTANCE),
            Map.entry("safe_fall_distance", Attributes.SAFE_FALL_DISTANCE),
            Map.entry("fall_damage_multiplier", Attributes.FALL_DAMAGE_MULTIPLIER),
            Map.entry("movement_speed", Attributes.MOVEMENT_SPEED),
            Map.entry("water_movement_efficiency", Attributes.WATER_MOVEMENT_EFFICIENCY),
            Map.entry("luck", Attributes.LUCK),
            Map.entry("attack_damage", Attributes.ATTACK_DAMAGE),
            Map.entry("attack_speed", Attributes.ATTACK_SPEED),
            Map.entry("block_break_speed", Attributes.BLOCK_BREAK_SPEED),
            Map.entry("submerged_mining_speed", Attributes.SUBMERGED_MINING_SPEED),
            Map.entry("armor", Attributes.ARMOR),
            Map.entry("armor_toughness", Attributes.ARMOR_TOUGHNESS),
            Map.entry("burning_time", Attributes.BURNING_TIME));

    @SuppressWarnings("null")
    private static ConfigDto getDefaultConfigDto() {
        var milestoneStep = 5;
        var milestoneFeedback = true;
        var milestoneBonus = true;
        var maxLevel = 30;
        var rules = List.of(
                // 血量：从 10 级开始生效，每 2 级增加 2 点血
                // base:20 + add:20 => final:40
                new AttributeRuleDto("max_health", 10, maxLevel, 2, 2.0, true),
                // 潜水时间：每级提升
                // base:1 + add:1 => final:2
                new AttributeRuleDto("oxygen_bonus", 0, maxLevel, 1, 1.0 / maxLevel, true),
                // 击退抗性：每级提升
                // base:0 + base:60% => final:60%
                new AttributeRuleDto("knockback_resistance", 0, maxLevel, 1, 0.6 / maxLevel, true),
                // 爆炸击退抗性：每级提升
                // base:0 + add:30% => final:30% 
                new AttributeRuleDto("explosion_knockback_resistance", 0, maxLevel, 1, 0.3 / maxLevel, true),
                // 掉落最大高度：15 级 + 0.5，30 级 + 1.0
                // base:3 + add:1 => final:4
                new AttributeRuleDto("safe_fall_distance", 0, maxLevel, 15, 0.5, true),
                // 掉落伤害：每级提升
                // base:100% + add:-20% => final:80%
                new AttributeRuleDto("fall_damage_multiplier", 0, maxLevel, 1, -0.2 / maxLevel, true),
                // 移速：每级提升
                // base:0.1 + add:0.02 => final:0.12
                new AttributeRuleDto("movement_speed", 0, maxLevel, 1, 0.02 / maxLevel, true),
                // 水下移速效率：每级提升
                // base:0% + add:20% => final:20%
                new AttributeRuleDto("water_movement_efficiency", 0, maxLevel, 1, 0.2 / maxLevel, true),
                // 幸运值：每级提升
                // base:0 + add:1 => final:1
                new AttributeRuleDto("luck", 0, maxLevel, 1, 1.0 / maxLevel, true),
                // 攻击伤害：每 5 级提升 0.5，最大 3 点
                // base:1 + add:3 => final:4
                new AttributeRuleDto("attack_damage", 0, maxLevel, 5, 0.5, true),
                // 攻击速度：每级提升，最大提高 1 点
                // base:4 + add:1 => final:5
                new AttributeRuleDto("attack_speed", 0, maxLevel, 1, 1.0 / maxLevel, true),
                // 破坏方块速度：每级提升
                // base:100% + add:50% => final:150%
                new AttributeRuleDto("block_break_speed", 0, maxLevel, 1, 0.5 / maxLevel, true),
                // 水下挖掘时间：每级提升
                // base:20% + add:25% => final:45%
                new AttributeRuleDto("submerged_mining_speed", 0, maxLevel, 1, 0.25 / maxLevel, true),
                // 护甲：从 10 级开始，每 5 级给予 1 点，最大 4 点
                // base:0 + add:4 => final:4
                new AttributeRuleDto("armor", 10, maxLevel, 5, 1.0, true),
                // 护甲韧性：从 10 级开始，每 5 级给予 0.5 点，最大 2 点
                // base:0 + add:2 => final:2
                new AttributeRuleDto("armor_toughness", 10, maxLevel, 5, 0.5, true),
                // 着火时间：每级提升
                // base:100% - add:50% => final:50%
                new AttributeRuleDto("burning_time", 0, maxLevel, 1, -0.5 / maxLevel, true));

        return new ConfigDto(milestoneStep, milestoneFeedback, milestoneBonus, rules);
    }

    private static void setFromDto(ConfigDto configDto) {
        try {
            milestoneStep = configDto.milestoneStep();
            milestoneBonus = configDto.milestoneBonus();
            milestoneFeedback = configDto.milestoneFeedback();
            levelCap = 0;

            rules.clear();
            for (var rule : configDto.rules()) {
                var attribute = attributeMap.getOrDefault(rule.attributeKey(), null);
                if (attribute == null) {
                    XPGrowth.LOGGER.warn("Unrecognized attribute key {}, skipped", rule.attributeKey());
                    continue;
                }

                rules.put(rule.attributeKey(), new AttributeRule(
                        rule.attributeKey(),
                        attribute,
                        rule.startLevel(),
                        rule.maxLevel(),
                        rule.step(),
                        rule.stepBonus(),
                        rule.enabled()));

                if (rule.maxLevel() > levelCap) {
                    levelCap = rule.maxLevel();
                }
            }
        } catch (Exception e) {
            XPGrowth.LOGGER.warn("Error when loading config, using defaults", e);
            setFromDto(getDefaultConfigDto());
        }
    }
}
