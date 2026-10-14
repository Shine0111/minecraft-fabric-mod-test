package com.anach.testmod1.combat;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SuperSlamManager {

    private static final double LAUNCH_STRENGTH = 1.5;
    private static final double IMPACT_RADIUS = 5.0;
    private static final float MAX_DAMAGE = 30.0F;
    private static final double MAX_KNOCKBACK = 3.0;
    private static final double MAX_UPWARD_KNOCKBACK = 0.9;
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
        boolean slamFallProtection = false;
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

        player.hurtMarked = true;
        player.fallDistance = 0.0F;

        data.state = State.AIRBORNE;
        data.hasBeenAirborne = false;
        data.slamFallProtection = true;
        player.fallDistance = 0.0F;

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

                        // Prevent fall damage from accumulating during Super Slam.
                        player.fallDistance = 0.0F;
                    }

                    if (data.hasBeenAirborne && player.onGround()) {
                        impact(player, data);
                        player.fallDistance = 0.0F;
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
                        data.slamFallProtection = false;

                        System.out.println("[Super Slam] Ready!");
                    }
                }
            }
        }
    }

    private static void impact(ServerPlayer player, AttackData data) {
        System.out.println("[Super Slam] IMPACT at " + player.blockPosition());
        // Apply a small cost for using Super Slam: half a heart.
        player.hurtServer(
                (ServerLevel) player.level(),
                player.damageSources().generic(),
                1.0F);

        var level = (ServerLevel) player.level();

        // Find hostile mobs in a box surrounding the player.
        var nearbyMobs = level.getEntitiesOfClass(
                Monster.class,
                player.getBoundingBox().inflate(IMPACT_RADIUS),
                Monster::isAlive);

        int affectedMobs = 0;

        for (Monster mob : nearbyMobs) {
            // Calculate the distance between the player and the mob.
            double distance = Math.sqrt(mob.distanceToSqr(player));

            // The bounding box includes corners outside the circular radius.
            if (distance > IMPACT_RADIUS) {
                continue;
            }

            // Damage decreases with distance.
            double damageMultiplier = 1.0 - (distance / IMPACT_RADIUS);
            float damage = (float) (MAX_DAMAGE * damageMultiplier);

            if (damage > 0.0F) {
                boolean damaged = mob.hurtServer(
                        level,
                        player.damageSources().playerAttack(player),
                        damage);

                if (damaged) {
                    System.out.println(
                            "[Super Slam] Damaged "
                                    + mob.getName().getString()
                                    + " for "
                                    + damage
                                    + " points!");
                }
            }

            // Calculate the horizontal direction away from the player.
            double dx = mob.getX() - player.getX();
            double dz = mob.getZ() - player.getZ();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            if (horizontalDistance > 0.001) {
                dx /= horizontalDistance;
                dz /= horizontalDistance;
            } else {
                // Avoid division by zero if the mob overlaps the player.
                dx = 0.0;
                dz = 1.0;
            }

            // Knockback also weakens with distance.
            double knockbackMultiplier = 1.0 - (distance / IMPACT_RADIUS);

            mob.push(
                    dx * MAX_KNOCKBACK * knockbackMultiplier,
                    MAX_UPWARD_KNOCKBACK * knockbackMultiplier,
                    dz * MAX_KNOCKBACK * knockbackMultiplier);

            // Tell the server to synchronize the mob's movement.
            mob.hurtMarked = true;

            affectedMobs++;
        }

        System.out.println(
                "[Super Slam] Hit " + affectedMobs + " hostile mobs!");

        data.state = State.IMPACT;
    }
}