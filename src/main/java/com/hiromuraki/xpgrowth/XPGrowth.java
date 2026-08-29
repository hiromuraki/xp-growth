package com.hiromuraki.xpgrowth;

import java.util.Objects;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPGrowth implements DedicatedServerModInitializer {

	public static final String MOD_ID = "xp-growth";
	public static final Logger LOGGER = Objects.requireNonNull(LoggerFactory.getLogger(MOD_ID));

	@Override
	public void onInitializeServer() {
		XPGrowthConfig.load();
		LOGGER.info("XP Growth loaded: {} attribute rules, level cap {}",
				XPGrowthConfig.get().getRules().size(), XPGrowthConfig.get().getLevelCap());

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (var player : server.getPlayerList().getPlayers()) {
				if (player == null) {
					continue;
				}
				PlayerProgression.tick(player);
			}
		});

		ServerPlayConnectionEvents.JOIN
				.register((handler, sender, server) -> PlayerProgression.onJoin(handler.getPlayer()));

		ServerPlayConnectionEvents.DISCONNECT
				.register((handler, server) -> PlayerProgression.onLeave(handler.getPlayer()));
	}
}