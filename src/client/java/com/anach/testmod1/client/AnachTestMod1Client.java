package com.anach.testmod1.client;

import com.anach.testmod1.AnachTestMod1;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import com.anach.testmod1.network.StartSuperSlamPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import com.anach.testmod1.network.SuperSlamCooldownPayload;

public class AnachTestMod1Client implements ClientModInitializer {

	private static KeyMapping superJumpKey;

	private static final double SUPER_JUMP_STRENGTH = 1.5;
	private static final int COOLDOWN_DURATION = 60;

	// State
	static int cooldownTicks = 0;

	// HUD button press animation
	private static final int PRESS_ANIMATION_DURATION = 8;
	private static int pressAnimationTicks = 0;

	public static int getPressAnimationTicks() {
		return pressAnimationTicks;
	}

	public static int getPressAnimationDuration() {
		return PRESS_ANIMATION_DURATION;
	}

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
				java.util.Objects.requireNonNull(AnachTestMod1.id("super_jump_hud")),
				SuperJumpHud::render);

		// Create a category for our custom key binding
		KeyMapping.Category category = KeyMapping.Category.register(
				java.util.Objects.requireNonNull(AnachTestMod1.id("super_jump_hud")));

		// Register our Super Jump key, assigned to G
		superJumpKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						"key.anach-test-mod-1.super_jump",
						InputConstants.Type.KEYSYM,
						InputConstants.KEY_G,
						category));

		// Receive cooldown updates from the server.
		ClientPlayNetworking.registerGlobalReceiver(
				SuperSlamCooldownPayload.TYPE,
				(payload, context) -> {
					context.client().execute(() -> {
						cooldownTicks = payload.ticks();
					});
				});

		// Check for key presses during the game
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (cooldownTicks > 0) {
				cooldownTicks--;
			}
			if (pressAnimationTicks > 0) {
				pressAnimationTicks--;
			}

			while (superJumpKey.consumeClick()) {
				if (client.player == null)
					continue;

				if (!ClientPlayNetworking.canSend(StartSuperSlamPayload.TYPE))
					continue;

				// Trigger the HUD button press animation.
				pressAnimationTicks = PRESS_ANIMATION_DURATION;

				// Send the ability request to the server.
				ClientPlayNetworking.send(new StartSuperSlamPayload());
			}
		});

	}
}