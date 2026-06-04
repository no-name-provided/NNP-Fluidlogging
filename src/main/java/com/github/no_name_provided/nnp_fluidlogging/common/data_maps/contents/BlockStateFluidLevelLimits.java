package com.github.no_name_provided.nnp_fluidlogging.common.data_maps.contents;

import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.entries.FluidLevelCallbacks;
import com.github.no_name_provided.nnp_fluidlogging.common.registries.FRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.BiFunction;

/**
 * Attached to blocks through the FluidLevelLimit data map. Allows you to define a maximum (and minimum) levels for
 * fluids in BlockStates associated with that block. We use callbacks so you can return a state-specific value, even
 * though BlockStates aren't registry objects.
 * <p>
 * Normal fluids range from 1 to 8, with (still) fluid height being calculated as LEVEL/9. This means fluid height
 * normally ranges from 1/9=0.111... to 8/9=0.888... of a full block (which is itself 16 pixels high on most vanilla
 * textures). However, the hard caps are 0 (no fluid) and 9 (a full block).
 * </p>
 * <p>
 * Implementation inspired by net.minecraft.world.level.timers.FunctionCallback, which appears to have a similar
 * implementation.
 * </p>
 *
 * @param minLevelCallback If null, will default to vanilla (0).
 * @param maxLevelCallback If null, will default to vanilla (9).
 */
public record BlockStateFluidLevelLimits(
        BiFunction<BlockState, FluidType, Integer> minLevelCallback,
        BiFunction<BlockState, FluidType, Integer> maxLevelCallback
) {
    @SuppressWarnings("DataFlowIssue") // We handle nullity in the constructor, so null values aren't a problem here
    public static final Codec<BlockStateFluidLevelLimits> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("min_callback").xmap(
                            FRegistries.FLUID_LEVEL_CALLBACKS_REGISTRY::getValue,
                            FRegistries.FLUID_LEVEL_CALLBACKS_REGISTRY::getKey
                    ).forGetter(BlockStateFluidLevelLimits::minLevelCallback),
                    Identifier.CODEC.fieldOf("max_callback").xmap(
                            FRegistries.FLUID_LEVEL_CALLBACKS_REGISTRY::getValue,
                            FRegistries.FLUID_LEVEL_CALLBACKS_REGISTRY::getKey
                    ).forGetter(BlockStateFluidLevelLimits::maxLevelCallback)
            ).apply(instance, BlockStateFluidLevelLimits::new)
    );
    
    /**
     * Overwrite the default constructor so we can gracefully handle default (null) parameters. Vanilla fluid levels
     * range from 0 to 8.
     */
    public BlockStateFluidLevelLimits {
        if (minLevelCallback == null) {
            minLevelCallback = FluidLevelCallbacks.CONSTANTS.getFirst().get();
        }
        if (maxLevelCallback == null) {
            maxLevelCallback = FluidLevelCallbacks.CONSTANTS.get(9).get();
        }
    }
    
    /**
     * Calculate the minimum fluid level. Consider caching (will need to be cleared if registries reloaded).
     *
     * @param blockState The BlockState being fluidlogged.
     * @return The minimum level for fluid in that BlockState.
     */
    public int getMinLevel(BlockState blockState, FluidType fluidType) {
        
        return minLevelCallback.apply(blockState, fluidType);
    }
    
    /**
     * Calculate the maximum fluid level. Consider caching (will need to be cleared if registries reloaded).
     * <p>
     * It's expected this will usually be left as default (8).
     * </p>
     *
     * @param blockState The BlockState being fluidlogged.
     * @return The maximum level for fluid in that BlockState.
     */
    public int getMaxLevel(BlockState blockState, FluidType fluidType) {
        
        return maxLevelCallback.apply(blockState, fluidType);
    }
}
