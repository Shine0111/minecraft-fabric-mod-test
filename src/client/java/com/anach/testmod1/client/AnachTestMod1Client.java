package com.anach.testmod1.client;

import com.anach.testmod1.AnachTestMod1;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class AnachTestMod1Client implements ClientModInitializer {

	private static KeyMapping superJumpKey;

	private static final double SUPER_JUMP_STRENGTH = 1.5;
	private static final int COOLDOWN_DURATION = 60;

	// State
	static int cooldownTicks = 0;

	// Getters
	public static int getCooldownTicks() {
		return cooldownTicks;
	}

	public static int getCooldownDuration() {
		return COOLDOWN_DURATION;
	}

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as
		// rendering.

		HudElementRegistry.attachElementAfter(
				VanillaHudElements.MISC_OVERLAYS,
				AnachTestMod1.id("super_jump_hud"),
				SuperJumpHud::render);

		// Create a category for our custom key binding
		KeyMapping.Category category = KeyMapping.Category.register(
				AnachTestMod1.id("super_jump"));

		// Register our Super Jump key, assigned to G
		superJumpKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						"key.anach-test-mod-1.super_jump",
						InputConstants.Type.KEYSYM,
						InputConstants.KEY_G,
						category));

		// Check for key presses during the game
		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			// Reduce cooldown ticks once per client tick
			if (cooldownTicks > 0) {
				cooldownTicks--;
			}

			while (superJumpKey.consumeClick()) {
				var Player = client.player;
				if (Player == null) {
					continue;
				}
				if (!Player.onGround()) {
					continue;
				}

				// Can't activate if we're still on cooldown
				if (cooldownTicks > 0) {
					continue;
				}

				var velocity = Player.getDeltaMovement();

				Player.setDeltaMovement(velocity.x, SUPER_JUMP_STRENGTH, velocity.z);

				// Start cooldown after ability is used
				cooldownTicks = COOLDOWN_DURATION;
			}
		});
	}
}