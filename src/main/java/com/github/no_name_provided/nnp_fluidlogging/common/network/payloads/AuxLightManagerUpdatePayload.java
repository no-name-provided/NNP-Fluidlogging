package com.github.no_name_provided.nnp_fluidlogging.common.network.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

public record AuxLightManagerUpdatePayload(int lightLevel, long blockPos) implements CustomPacketPayload {
    public static CustomPacketPayload.Type<AuxLightManagerUpdatePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MODID, "aux_light_manager_update")
    );
    
    public static final StreamCodec<ByteBuf, AuxLightManagerUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            AuxLightManagerUpdatePayload::lightLevel,
            ByteBufCodecs.VAR_LONG,
            AuxLightManagerUpdatePayload::blockPos,
            AuxLightManagerUpdatePayload::new
    );
    
    @Override
    public @NotNull Type<AuxLightManagerUpdatePayload> type() {
        
        return TYPE;
    }
}
