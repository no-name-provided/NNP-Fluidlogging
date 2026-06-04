package com.github.no_name_provided.nnp_fluidlogging.datagen;

import com.github.no_name_provided.nnp_fluidlogging.datagen.providers.FDataMapProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

/**
 * The class which queues up our data generators.
 */
@EventBusSubscriber(modid = MODID)
public class Generators {
    /**
     * This is the entry point for our datagen.
     * <p>
     * Note: Per the general (and for once sensible) neo recommendation, we're ignoring the event name and just doing
     * all our datagen on the "client-but-not-really" side.
     * </p>
     */
    @SubscribeEvent
    static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(FDataMapProvider::new);
    }
}
