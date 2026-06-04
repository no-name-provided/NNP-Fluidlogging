package com.github.no_name_provided.nnp_fluidlogging.common.data_maps;

import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.contents.BlockStateFluidLevelLimits;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

public class FDataMaps {
    public static final DataMapType<Block, BlockStateFluidLevelLimits> BLOCKSTATE_FLUID_LEVEL_LIMITS = DataMapType.builder(
                    // Data map files will be located at [MODID]:MODID/data_maps/block/fluid_level_limits.json.
                    Identifier.fromNamespaceAndPath(MODID, "fluid_level_limits"),
                    Registries.BLOCK,
                    BlockStateFluidLevelLimits.CODEC
                    // There may be some minor bugs in clients missing this map, but I lean permissive
            ).synced(BlockStateFluidLevelLimits.CODEC, false)
            .build();
}
