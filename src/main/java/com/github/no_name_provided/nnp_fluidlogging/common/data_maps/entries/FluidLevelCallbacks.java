package com.github.no_name_provided.nnp_fluidlogging.common.data_maps.entries;

import com.github.no_name_provided.nnp_fluidlogging.common.registries.FRegistries;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

public class FluidLevelCallbacks {
    public static DeferredRegister<BiFunction<BlockState, FluidType, Integer>> FLUID_LEVEL_CALLBACKS = DeferredRegister.create(
            FRegistries.FLUID_LEVEL_CALLBACKS_REGISTRY,
            MODID
    );
    public static List<Supplier<BiFunction<BlockState, FluidType, Integer>>> CONSTANTS = new ArrayList<>();
    
    public static Supplier<BiFunction<BlockState, FluidType, Integer>> SLAB_MIN = FLUID_LEVEL_CALLBACKS.register(
            "slab_min",
            () -> (blockState, unusedType) -> {
                boolean isBottom = blockState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
                
                return isBottom ? 5 : 0;
            }
    );
    public static Supplier<BiFunction<BlockState, FluidType, Integer>> SLAB_MAX = FLUID_LEVEL_CALLBACKS.register(
            "slab_max",
            () -> (blockState, unusedType) -> {
                boolean isBottom = blockState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
                
                return isBottom ? 8 : 4;
            }
    );
    public static Supplier<BiFunction<BlockState, FluidType, Integer>> STAIR_MIN = FLUID_LEVEL_CALLBACKS.register(
            "stair_min",
            () -> (blockState, unusedType) -> {
                boolean isBottom = blockState.getValue(StairBlock.HALF) == Half.BOTTOM;
                
                return isBottom ? 5 : 0;
            }
    );
    public static Supplier<BiFunction<BlockState, FluidType, Integer>> STAIR_MAX = FLUID_LEVEL_CALLBACKS.register(
            "stair_max",
            () -> (blockState, unusedType) -> {
                boolean isBottom = blockState.getValue(StairBlock.HALF) == Half.BOTTOM;
                
                return isBottom ? 8 : 4;
            }
    );
    
    public static BiFunction<BlockState, FluidType, Integer> generateConstant(Integer constant) {
        
        return (unusedState, unusedType) -> constant;
    }
    
    public static void populateConstants() {
        for (int i = 0; i < 9; i++) {
            int finalI = i;
            CONSTANTS.add(
                    FLUID_LEVEL_CALLBACKS.register(
                            "constant_" + i,
                            () -> generateConstant(finalI)
                    )
            );
        }
    }
    
    public static void register(IEventBus modBus) {
        populateConstants();
        
        FLUID_LEVEL_CALLBACKS.register(modBus);
    }
}
