package com.github.no_name_provided.nnp_fluidlogging.common.network.handlers;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.FluidStateSyncPayload;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class FluidStateSyncHandler {
    /**
     * Updates FluidState attachment on the client. Only use if there's a weird desync, or we're on a version of
     * Minecraft that doesn't have synchronized Neo attachments.
     */
    public static void handle(FluidStateSyncPayload payload, IPayloadContext context) {
        ChunkAccess chunk = context.player().level().getChunk(payload.blockPos());
        chunk.setData(FAttachments.FLUID_STATES, payload.states());
        chunk.markUnsaved();
    }
}
