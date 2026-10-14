package com.anach.testmod1.network;

import com.anach.testmod1.AnachTestMod1;
import com.anach.testmod1.combat.SuperSlamManager;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class SuperSlamNetworking {

    private SuperSlamNetworking() {
        // Prevent creating instances of this utility class.
    }

    public static void initialize() {
        // Register the packet that the client sends to the server.
        PayloadTypeRegistry.serverboundPlay().register(
                StartSuperSlamPayload.TYPE,
                StartSuperSlamPayload.CODEC);

        // Handle the packet when the server receives it.
        ServerPlayNetworking.registerGlobalReceiver(
                StartSuperSlamPayload.TYPE,
                (payload, context) -> {
                    var player = context.player();

                    SuperSlamManager.start(player);
                });

        AnachTestMod1.LOGGER.info("Super Slam networking initialized!");
    }
}