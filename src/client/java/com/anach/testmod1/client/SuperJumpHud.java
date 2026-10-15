package com.anach.testmod1.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SuperJumpHud {

        private static final int RADIUS = 12;
        private static final int THICKNESS = 3;
        private static final int SEGMENTS = 60;

        private static final int BACKGROUND_COLOR = 0xAA444444;
        private static final int READY_COLOR = 0xFF35D07F;
        private static final int COOLDOWN_COLOR = 0xFF16B8FF;
        private static final int ARROW_COLOR = 0xFFFFFFFF;

        public static void render(
                        GuiGraphicsExtractor graphics,
                        DeltaTracker deltaTracker) {
                Minecraft minecraft = Minecraft.getInstance();

                // Don't display the indicator when the player isn't in the world.
                if (minecraft.player == null) {
                        return;
                }

                // Get the screen dimensions.
                int screenWidth = minecraft.getWindow().getGuiScaledWidth();
                int screenHeight = minecraft.getWindow().getGuiScaledHeight();

                // Position the indicator bottom left
                int centerX = 35;
                int centerY = screenHeight - 45;
                // Calculate the button press animation scale.
                int pressTicks = AnachTestMod1Client.getPressAnimationTicks();
                int pressDuration = AnachTestMod1Client.getPressAnimationDuration();

                double pressProgress = pressDuration > 0
                                ? (double) pressTicks / pressDuration
                                : 0.0;

                // Shrink the ring by up to 18% when pressed.
                double scale = 1.0 - 0.18 * pressProgress;

                int cooldownTicks = AnachTestMod1Client.getCooldownTicks();

                int cooldownDuration = AnachTestMod1Client.getCooldownDuration();

                // Calculate the remaining cooldown percentage.
                double progress = cooldownDuration > 0
                                ? (double) cooldownTicks / cooldownDuration
                                : 0.0;

                progress = Math.max(0.0, Math.min(1.0, progress));

                // Choose the color based on the ability's state.
                int ringColor = cooldownTicks > 0
                                ? COOLDOWN_COLOR
                                : READY_COLOR;

                // Draw the background ring.
                drawRing(
                                graphics,
                                centerX,
                                centerY,
                                1.0,
                                BACKGROUND_COLOR,
                                scale);

                // Draw the cooldown ring.
                if (cooldownTicks > 0) {
                        drawRing(
                                        graphics,
                                        centerX,
                                        centerY,
                                        progress,
                                        ringColor,
                                        scale);
                } else {
                        // Show a complete green ring when Super Jump is ready.
                        drawRing(
                                        graphics,
                                        centerX,
                                        centerY,
                                        1.0,
                                        READY_COLOR,
                                        scale);
                }

                // Draw the upward arrow in the center.
                // graphics.text(
                // minecraft.font,
                // "↑",
                // centerX - 4,
                // centerY - 5,
                // ARROW_COLOR,
                // false);
        }

        private static void drawRing(
                        GuiGraphicsExtractor graphics,
                        int centerX,
                        int centerY,
                        double progress,
                        int color,
                        double scale) {

                int visibleSegments = (int) Math.ceil(SEGMENTS * progress);

                // Scale the radius to animate the ring.
                double animatedRadius = RADIUS * scale;

                for (int i = 0; i < visibleSegments; i++) {

                        double angle = 2.0 * Math.PI * i / SEGMENTS
                                        - Math.PI / 2.0;

                        int x = centerX
                                        + (int) Math.round(
                                                        Math.cos(angle) * animatedRadius);

                        int y = centerY
                                        + (int) Math.round(
                                                        Math.sin(angle) * animatedRadius);

                        graphics.fill(
                                        x - THICKNESS / 2,
                                        y - THICKNESS / 2,
                                        x + (THICKNESS + 1) / 2,
                                        y + (THICKNESS + 1) / 2,
                                        color);
                }
        }
}