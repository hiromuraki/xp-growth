package com.hiromuraki.xprogue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

public final class PlayerProgression {
    private PlayerProgression() {
    }

    public static void onJoin(ServerPlayer player) {
        lastLevels.put(player.getUUID(), player.experienceLevel);
        apply(player);
    }

    public static void onLeave(ServerPlayer player) {
        lastLevels.remove(player.getUUID());
    }

    public static void tick(ServerPlayer player) {
        var currentLevel = player.experienceLevel;
        var previousLevel = lastLevels.get(player.getUUID());
        if (previousLevel == null || previousLevel != currentLevel) {
            lastLevels.put(player.getUUID(), currentLevel);
            apply(player);
            if (previousLevel != null && currentLevel > previousLevel) {
                onLevelUp(player, currentLevel);
            }
        }
    }

    private static void apply(ServerPlayer player) {
        var config = XpRogueConfig.get();
        var level = Math.min(player.experienceLevel, config.getLevelCap());

        for (var rule : config.getRules().values()) {
            if (!rule.enabled()) {
                continue;
            }

            var attr = player.getAttribute(rule.attribute());
            if (attr == null) {
                continue;
            }

            var amount = rule.perLevel() * level;
            if (amount == 0.0) {
                attr.removeModifier(rule.modifierId());
            } else {
                attr.addOrUpdateTransientModifier(rule.modifier(level));
            }
        }

        // 掉级导致最大生命下降时，把当前生命钳到新上限
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static final Map<UUID, Integer> lastLevels = new HashMap<>();

    private static void onLevelUp(ServerPlayer player, int level) {
        var config = XpRogueConfig.get();
        if (!config.getFeedback()) {
            return;
        }

        if (level > config.getLevelCap()) {
            return;
        }

        if (level % config.getMilestoneInterval() == 0) {
            player.sendOverlayMessage(Component.translatableWithFallback("xp_rogue.level_up", "You feel stronger..."));
            player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
            player.level().sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    10,
                    0.5, 0.5, 0.5,
                    0);
        }
    }
}