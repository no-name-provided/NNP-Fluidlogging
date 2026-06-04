package com.github.no_name_provided.nnp_fluidlogging.common.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.BiFunction;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

/**
 * Define our custom registries.
 */
public class FRegistries {
    public static final ResourceKey<Registry<BiFunction<BlockState, FluidType, Integer>>> FLUID_LEVEL_CALLBACKS =
            ResourceKey.createRegistryKey(
                    Identifier.fromNamespaceAndPath(MODID,
                            "fluid_level_callbacks")
            );
    
    /**
     * A registry of callbacks that calculate permissible fluid levels.
     */
    public static final Registry<BiFunction<BlockState, FluidType, Integer>> FLUID_LEVEL_CALLBACKS_REGISTRY =
            new RegistryBuilder<>(FLUID_LEVEL_CALLBACKS)
                    .sync(true)
                    .create();
}
