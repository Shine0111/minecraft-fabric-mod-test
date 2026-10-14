package com.anach.testmod1.sound;

import com.anach.testmod1.AnachTestMod1;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {

    public static final SoundEvent SUPER_SLAM_LAUNCH = register("super_slam_launch");

    public static final SoundEvent SUPER_SLAM_FALL = register("super_slam_fall");

    public static final SoundEvent SUPER_SLAM_ENERGY = register("super_slam_energy");

    private ModSounds() {
    }

    private static SoundEvent register(String name) {
        var id = AnachTestMod1.id(name);

        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createVariableRangeEvent(id));
    }

    public static void initialize() {
        AnachTestMod1.LOGGER.info("Super Slam sounds registered!");
    }
}