package com.anach.testmod1.combat;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SuperSlamManager {

    private static final double LAUNCH_STRENGTH = 1.5;
    private static final int COOLDOWN_DURATION = 60;

    private static final Map<UUID, AttackData> PLAYERS = new HashMap<>();

    private enum State {
        READY,
        AIRBORNE,
        IMPACT,
        COOLDOWN
    }

    private static class AttackData {
        State state = State.READY;
        int cooldownTicks = 0;
        boolean hasBeenAirborne = false;
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(
                SuperSlamManager::tick);
    }

    public static void start(ServerPlayer player) {
        AttackData data = PLAYERS.computeIfAbsent(
                player.getUUID(),
                uuid -> new AttackData());

        // The attack can only start when ready and grounded.
        if (data.state != State.READY || !player.onGround()) {
            return;
        }

        // Launch the player while preserving horizontal velocity.
        var velocity = player.getDeltaMovement();

        player.setDeltaMovement(
                velocity.x,
                LAUNCH_STRENGTH,
                velocity.z);

        data.state = State.AIRBORNE;
        data.hasBeenAirborne = false;

        System.out.println("[Super Slam] Launch!");
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {

            AttackData data = PLAYERS.get(player.getUUID());

            if (data == null) {
                continue;
            }

            switch (data.state) {

                case READY -> {
                    // Waiting for the player to activate the attack.
                }

                case AIRBORNE -> {
                    if (!player.onGround()) {
                        data.hasBeenAirborne = true;
                    }

                    // Trigger the impact only after an actual landing.
                    if (data.hasBeenAirborne && player.onGround()) {
                        impact(player, data);
                    }
                }

                case IMPACT -> {
                    // The impact is processed once, then cooldown begins.
                    data.state = State.COOLDOWN;
                    data.cooldownTicks = COOLDOWN_DURATION;
                }

                case COOLDOWN -> {
                    if (data.cooldownTicks > 0) {
                        data.cooldownTicks--;
                    }

                    if (data.cooldownTicks <= 0) {
                        data.state = State.READY;
                        System.out.println(
                                "[Super Slam] Ready!");
                    }
                }
            }
        }
    }

    private static void impact(
            ServerPlayer player,
            AttackData data) {
        // For now, verify landing detection before adding damage.
        System.out.println(
                "[Super Slam] IMPACT at "
                        + player.blockPosition());

        data.state = State.IMPACT;
    }
}