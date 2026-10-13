package com.anach.testmod1.client;

import com.anach.testmod1.AnachTestMod1;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;

public class AnachTestMod1Client implements ClientModInitializer {

	private static KeyMapping superJumpKey;

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as
		// rendering.

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
			while (superJumpKey.consumeClick()) {
				var Player = client.player;
				if (Player == null) {
					return;
				}

				Player.sendSystemMessage(
						Component.literal("Super Jump key detected!"));

				var velocity = Player.getDeltaMovement();

				Player.setDeltaMovement(velocity.x, 1.5, velocity.z);
			}
		});
	}
}