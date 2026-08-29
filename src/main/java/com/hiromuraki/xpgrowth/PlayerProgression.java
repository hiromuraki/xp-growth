package com.hiromuraki.xpgrowth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

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
        var config = XPGrowthConfig.get();
        var level = Math.min(player.experienceLevel, config.getLevelCap());

        for (var rule : config.getRules().values()) {
            if (!rule.enabled()) {
                continue;
            }

            var attr = player.getAttribute(rule.attribute());
            if (attr == null) {
                continue;
            }

            var amount = (rule.maxBonus() / config.getLevelCap()) * level;
            if (amount == 0.0) {
                attr.removeModifier(rule.getModifierId());
            } else {
                var modifier = new AttributeModifier(rule.getModifierId(), amount,
                        AttributeModifier.Operation.ADD_VALUE);
                attr.addOrUpdateTransientModifier(modifier);
            }
        }

        // 掉级导致最大生命下降时，把当前生命钳到新上限
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static final Map<UUID, Integer> lastLevels = new HashMap<>();

    private static void onLevelUp(ServerPlayer player, int level) {
        var config = XPGrowthConfig.get();
        if (level > config.getLevelCap()) {
            return;
        }

        if (level % config.getMilestoneStep() == 0) {
            if (config.getMilestoneBonus()) {
                var tier = level / config.getMilestoneStep();
                var duration = Math.min(5 * tier, 30) * XPGrowth.TICK_PER_SECOND;
                // Absorption hearts = (amplifier + 1) * 4
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, Math.min(tier - 1, 4)));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, duration, 1));
            }

            if (config.getMilestoneFeedback()) {
                player.sendOverlayMessage(
                        Component.translatableWithFallback("xp_growth.level_up", "You feel stronger..."));
                player.level().playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS,
                        1.0f, 1.0f);
                player.level().sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        10,
                        0.5, 0.5, 0.5,
                        0);
            }
        }

    }
}