package com.anach.testmod1.network;

import com.anach.testmod1.AnachTestMod1;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;

public record SuperSlamCooldownPayload(int ticks)
        implements CustomPacketPayload {

    public static final Type<SuperSlamCooldownPayload> TYPE = new Type<>(AnachTestMod1.id("super_slam_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SuperSlamCooldownPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SuperSlamCooldownPayload::ticks,
            SuperSlamCooldownPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}