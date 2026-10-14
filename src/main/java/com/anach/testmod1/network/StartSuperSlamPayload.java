package com.anach.testmod1.network;

import com.anach.testmod1.AnachTestMod1;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StartSuperSlamPayload() implements CustomPacketPayload {

    public static final Type<StartSuperSlamPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            AnachTestMod1.MOD_ID,
            "start_super_slam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StartSuperSlamPayload> CODEC = StreamCodec
            .unit(new StartSuperSlamPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}