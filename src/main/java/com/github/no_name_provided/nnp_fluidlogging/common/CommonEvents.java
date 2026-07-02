package com.github.no_name_provided.nnp_fluidlogging.common;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.FluidStateSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;
import static com.github.no_name_provided.nnp_fluidlogging.common.data_maps.FDataMaps.BLOCKSTATE_FLUID_LEVEL_LIMITS;
import static com.github.no_name_provided.nnp_fluidlogging.common.registries.FRegistries.FLUID_LEVEL_CALLBACKS_REGISTRY;

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
//    @SubscribeEvent
    static void onChunkStartWatch(ChunkWatchEvent.Sent event) {
        event.getPlayer().connection.send(new FluidStateSyncPayload(
                event.getPos().getWorldPosition(),
                event.getChunk().getData(FAttachments.FLUID_STATES)
        ));
    }
    
    /**
     * Register our registry so people can register callbacks... because registration.
     *
     * @param event The event being handled.
     */
    @SubscribeEvent // on the mod event bus
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(FLUID_LEVEL_CALLBACKS_REGISTRY);
    }
    
    /**
     * Register our data maps.
     *
     * @param event The event being handled.
     */
    @SubscribeEvent
    static void onRegisterDataMaps(RegisterDataMapTypesEvent event) {
        event.register(BLOCKSTATE_FLUID_LEVEL_LIMITS);
    }
}
