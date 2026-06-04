package com.github.no_name_provided.nnp_fluidlogging.datagen;

import com.github.no_name_provided.nnp_fluidlogging.datagen.providers.FDataMapProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

@EventBusSubscriber(modid = MODID)
public class Generators {
    @SubscribeEvent
    static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(FDataMapProvider::new);
    }
}
