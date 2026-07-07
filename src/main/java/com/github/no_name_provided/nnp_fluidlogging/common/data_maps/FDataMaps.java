package com.github.no_name_provided.nnp_fluidlogging.common.data_maps;

import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.contents.BlockStateFluidLevelLimits;
import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.entries.FluidLevelCallbacks;
import com.github.no_name_provided.nnp_fluidlogging.common.registries.FRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

/**
 * Define and statically expose our DataMaps.
 */
public class FDataMaps {
    /**
     * Our DataMap of fluid level limits. Rather than use simple integers, which wouldn't work for dynamic geometry
     * (like slabs and stairs, which can be placed upside down) we instead allow DataPack devs to register callbacks
     * that dynamically calculate the correct minimum and maximum fluid heights (one callback each).
     * <p>
     * For efficiency (and because BlockStates aren't registry objects), a callback must be provided for each block, and
     * handle every combination of FluidType and BlockState that block could encounter.
     * </p>
     * <p>
     * Entries must be registered in {@link FRegistries#FLUID_LEVEL_CALLBACKS_REGISTRY}, which mod developers are
     * welcome to extend. Default (valid) entries are add by {@link FluidLevelCallbacks}
     * </p>
     * <p>
     * Data map files will be located at [MODID]:MODID/data_maps/block/fluid_level_limits.json.
     * </p>
     */
    public static final DataMapType<Block, BlockStateFluidLevelLimits> BLOCKSTATE_FLUID_LEVEL_LIMITS = DataMapType.builder(
                    ResourceLocation.fromNamespaceAndPath(MODID, "fluid_level_limits"),
                    Registries.BLOCK,
                    BlockStateFluidLevelLimits.CODEC
                    // There may be some minor bugs in clients missing this map, but I lean permissive
            ).synced(BlockStateFluidLevelLimits.CODEC, false)
            .build();
}