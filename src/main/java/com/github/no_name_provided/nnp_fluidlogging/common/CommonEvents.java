package com.github.no_name_provided.nnp_fluidlogging.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import static com.github.no_name_provided.nnp_fluidlogging.common.data_maps.FDataMaps.BLOCKSTATE_FLUID_LEVEL_LIMITS;
import static com.github.no_name_provided.nnp_fluidlogging.common.registries.FRegistries.FLUID_LEVEL_CALLBACKS_REGISTRY;

@EventBusSubscriber
public class CommonEvents {
    
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
