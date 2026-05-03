package com.github.no_name_provided.nnp_fluidlogging.common.network;

import com.github.no_name_provided.nnp_fluidlogging.common.network.handlers.AuxLightManagerHandler;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.AuxLightManagerUpdatePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

@EventBusSubscriber(modid = MODID)
public class NetworkEvents {
    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = new PayloadRegistrar("1");
        registrar.playToClient(
                AuxLightManagerUpdatePayload.TYPE,
                AuxLightManagerUpdatePayload.STREAM_CODEC,
                AuxLightManagerHandler::handle
        );
    }
}
