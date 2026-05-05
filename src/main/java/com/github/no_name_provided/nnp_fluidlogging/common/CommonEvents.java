package com.github.no_name_provided.nnp_fluidlogging.common;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.FluidStateSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

@EventBusSubscriber(modid = MODID)
public class CommonEvents {
    @SubscribeEvent
    static void onChunkStartWatch(ChunkWatchEvent.Sent event) {
        event.getPlayer().connection.send(new FluidStateSyncPayload(
                event.getPos().getWorldPosition(),
                event.getChunk().getData(FAttachments.FLUID_STATES)
        ));
    }
}
