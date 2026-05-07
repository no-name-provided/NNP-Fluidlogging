package com.github.no_name_provided.nnp_fluidlogging.common;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.FluidStateSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

/**
 * Register common events, to be run by both sides.
 */
@EventBusSubscriber(modid = MODID)
public class CommonEvents {
    /**
     * Synchronize chunk data when chunks are sent from the server to the client. Only required if we aren't on a
     * version of NeoForge that handles attachment synchronization automatically (or we haven't set it up it for some
     * reason). Well, that, and it might help with lighting consistency across chunk reloads/server restarts.
     *
     * @param event The event being handled.
     */
    @SubscribeEvent
    static void onChunkStartWatch(ChunkWatchEvent.Sent event) {
        event.getPlayer().connection.send(new FluidStateSyncPayload(
                event.getPos().getWorldPosition(),
                event.getChunk().getData(FAttachments.FLUID_STATES)
        ));
    }
}
