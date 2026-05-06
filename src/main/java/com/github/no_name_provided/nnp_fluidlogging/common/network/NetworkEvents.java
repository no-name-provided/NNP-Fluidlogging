package com.github.no_name_provided.nnp_fluidlogging.common.network;

import com.github.no_name_provided.nnp_fluidlogging.common.network.handlers.AuxLightManagerHandler;
import com.github.no_name_provided.nnp_fluidlogging.common.network.handlers.FluidStateSyncHandler;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.AuxLightManagerUpdatePayload;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.FluidStateSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

@EventBusSubscriber(modid = MODID)
public class NetworkEvents {
    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = new PayloadRegistrar("1");
        // This should be the default, but I still managed to get a concurrent modification exception
        registrar.executesOn(HandlerThread.MAIN);
        registrar.playToClient(
                AuxLightManagerUpdatePayload.TYPE,
                AuxLightManagerUpdatePayload.STREAM_CODEC,
                AuxLightManagerHandler::handle
        );
        registrar.playToClient(
                FluidStateSyncPayload.TYPE,
                FluidStateSyncPayload.STREAM_CODEC,
                FluidStateSyncHandler::handle
        );
    }
}
