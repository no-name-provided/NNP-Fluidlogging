package com.github.no_name_provided.nnp_fluidlogging.common.registries;

import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.FDataMaps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.BiFunction;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

/**
 * Create custom registries and provide access to their keys.
 */
public class FRegistries {
    public static final ResourceKey<Registry<BiFunction<BlockState, FluidType, Integer>>> FLUID_LEVEL_CALLBACKS =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MODID,
                            "fluid_level_callbacks")
            );
    
    /**
     * A registry of fluid level callbacks ({@code BiFunction<BlockState, FluidType, Integer>}), which are used to
     * determine the maximum and minimum fluid level for each blockstate the game attempts to fluidlog. These can be
     * used in {@link FDataMaps#BLOCKSTATE_FLUID_LEVEL_LIMITS}.
     * <p>
     * Several basic callbacks are provided, and populated for vanilla blocks that should have minimum and maximum fluid
     * levels. However, modders can also register their own if they need to run arbitrary code to calculate level
     * limits.
     * </p>
     */
    public static final Registry<BiFunction<BlockState, FluidType, Integer>> FLUID_LEVEL_CALLBACKS_REGISTRY =
            new RegistryBuilder<>(FLUID_LEVEL_CALLBACKS)
                    .sync(true)
                    .create();
}
