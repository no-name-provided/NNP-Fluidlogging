package com.github.no_name_provided.nnp_fluidlogging.common.network.payloads;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

public record FluidStateSyncPayload(BlockPos blockPos, FluidStates states) implements CustomPacketPayload {
    public static Type<FluidStateSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MODID, "fluidstate_sync")
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStateSyncPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            FluidStateSyncPayload::blockPos,
            FluidStates.STREAM_CODEC,
            FluidStateSyncPayload::states,
            FluidStateSyncPayload::new
    );
    
    @Override
    public @NotNull Type<FluidStateSyncPayload> type() {
        
        return TYPE;
    }
}
