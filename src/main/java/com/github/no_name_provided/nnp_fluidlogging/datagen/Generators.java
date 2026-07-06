package com.github.no_name_provided.nnp_fluidlogging.datagen;

import com.github.no_name_provided.nnp_fluidlogging.datagen.providers.FDataMapProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class Generators {
    
    @SubscribeEvent
    static void onGatherData(GatherDataEvent event) {
        event.createProvider(FDataMapProvider::new);
    }
}
