package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static net.minecraft.world.level.material.FlowingFluid.getCacheKey;

@Mixin(FlowingFluid.class)
abstract class FFluidlogging_FlowingFluid extends Fluid {
    
    /**
     * Lazy implementation. May not be necessary.
     */
    @Inject(method = "getSpread(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Map;",
    at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_getSpread(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Map<Direction, FluidState>> cir) {
        FlowingFluid flowingFluid = (FlowingFluid)(Object)this;
        int i = 1000;
        Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<Pair<BlockState, FluidState>> short2objectmap = new Short2ObjectOpenHashMap<>();
        Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();
        
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.relative(direction);
            short short1 = getCacheKey(pos, blockpos);
            Pair<BlockState, FluidState> pair = short2objectmap.computeIfAbsent(short1, p_284929_ -> {
                BlockState blockstate1 = level.getBlockState(blockpos);
                return Pair.of(blockstate1, level.getFluidState(blockpos));
            });
            BlockState blockstate = pair.getFirst();
            FluidState fluidstate = pair.getSecond();
            FluidState fluidstate1 = flowingFluid.getNewLiquid(level, blockpos, blockstate);
            if (flowingFluid.canPassThrough(level, fluidstate1.getType(), pos, state, direction, blockpos, blockstate, fluidstate)) {
                BlockPos blockpos1 = blockpos.below();
                boolean flag = short2booleanmap.computeIfAbsent(short1, p_255612_ -> {
                    BlockState blockstate1 = level.getBlockState(blockpos1);
                    return flowingFluid.isWaterHole(level, flowingFluid.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
                });
                int j;
                if (flag) {
                    j = 0;
                } else {
                    j = flowingFluid.getSlopeDistance(level, blockpos, 1, direction.getOpposite(), blockstate, pos, short2objectmap, short2booleanmap);
                }
                
                if (j < i) {
                    map.clear();
                }
                
                if (j <= i) {
                    map.put(direction, fluidstate1);
                    i = j;
                }
            }
        }
        
        cir.setReturnValue(map);
    }
    
    
    /**
     * Lazy hack of a mixin. May be unnecessary.
     */
    @Inject(method = "getNewLiquid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
    at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_getNewLiquid(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<FluidState> cir) {
        FlowingFluid flowingFluid = ((FlowingFluid)(Object)this);
        int i = 0;
        int j = 0;
        
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
//            FluidState fluidstate = blockstate.getFluidState();
            FluidState fluidstate = level.getFluidState(blockpos);
            if (fluidstate.getType().isSame(flowingFluid) && flowingFluid.canPassThroughWall(direction, level, pos, state, blockpos, blockstate)) {
                if (fluidstate.isSource() && net.neoforged.neoforge.event.EventHooks.canCreateFluidSource(level, blockpos, blockstate)) {
                    j++;
                }
                
                i = Math.max(i, fluidstate.getAmount());
            }
        }
        
        if (j >= 2) {
            BlockState blockstate1 = level.getBlockState(pos.below());
            //FluidState fluidstate1 = blockstate1.getFluidState();
            FluidState fluidstate1 = level.getFluidState(pos.below());
            //noinspection deprecation - copied from vanilla
            if (blockstate1.isSolid() || flowingFluid.isSourceBlockOfThisType(fluidstate1)) {
                
                cir.setReturnValue(flowingFluid.getSource(false));
            }
        }
        
        BlockPos blockpos1 = pos.above();
        BlockState blockstate2 = level.getBlockState(blockpos1);
//        FluidState fluidstate2 = blockstate2.getFluidState();
        FluidState fluidstate2 = level.getFluidState(blockpos1);
        if (!fluidstate2.isEmpty()
                && fluidstate2.getType().isSame(flowingFluid)
                && flowingFluid.canPassThroughWall(Direction.UP, level, pos, state, blockpos1, blockstate2)) {
            
            cir.setReturnValue(flowingFluid.getFlowing(8, true));
        } else {
            int k = i - flowingFluid.getDropOff(level);
            
            cir.setReturnValue(k <= 0 ? Fluids.EMPTY.defaultFluidState() : flowingFluid.getFlowing(k, false));
        }
        
        cir.cancel();
    }
}
