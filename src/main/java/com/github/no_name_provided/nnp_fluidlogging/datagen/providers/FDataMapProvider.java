package com.github.no_name_provided.nnp_fluidlogging.datagen.providers;

import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.FDataMaps;
import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.contents.BlockStateFluidLevelLimits;
import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.entries.FluidLevelCallbacks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.common.data.DataMapProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class FDataMapProvider extends DataMapProvider {
    /**
     * Create a new provider.
     *
     * @param packOutput     the output location
     * @param lookupProvider a {@linkplain CompletableFuture} supplying the registries
     */
    public FDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }
    
    /**
     * Generate data map entries.
     */
    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<BlockStateFluidLevelLimits, Block> builder = builder(FDataMaps.BLOCKSTATE_FLUID_LEVEL_LIMITS);
        BuiltInRegistries.BLOCK.asHolderIdMap().forEach(block -> {
            if (block.value() instanceof StairBlock) {
                builder.add(
                        block,
                        new BlockStateFluidLevelLimits(
                                FluidLevelCallbacks.STAIR_MIN.get(),
                                FluidLevelCallbacks.STAIR_MAX.get()
                        ),
                        false
                );
            } else if (block.value() instanceof SlabBlock) {
                builder.add(
                        block,
                        new BlockStateFluidLevelLimits(
                                FluidLevelCallbacks.SLAB_MIN.get(),
                                FluidLevelCallbacks.SLAB_MAX.get()
                        ),
                        false
                );
            }
        });
    }
}