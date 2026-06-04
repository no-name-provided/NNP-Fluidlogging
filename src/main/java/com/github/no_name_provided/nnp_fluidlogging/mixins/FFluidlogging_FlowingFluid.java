package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.FluidStateSyncPayload;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
import static net.minecraft.world.level.material.FlowingFluid.canPassThroughWall;

@Mixin(FlowingFluid.class)
abstract class FFluidlogging_FlowingFluid extends Fluid {
    
    /**
     * Forces spreading fluids to use our data structure.
     *
     * @param fluidState The source FluidState.
     * @param level      The ServerLevel.
     * @param testPos        The position being queried (target, not source).
     * @return The correct source FluidState.
     */
    @ModifyVariable(method = "getSpread(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Map;",
            at = @At("STORE"),
            name = "testFluidState"
    )
    private FluidState nnp_f_fluidlogging_getSpread(FluidState fluidState, ServerLevel level, @Local(name = "testPos") BlockPos testPos) {
        
        return level.getFluidState(testPos);
    }
    
    
    /**
     * Forces flowing fluids to place the correct fluid in the correct as they spread.
     */
    @Inject(method = "getNewLiquid(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_getNewLiquid(ServerLevel level, BlockPos pos, BlockState state, CallbackInfoReturnable<FluidState> cir) {
        FlowingFluid flowingFluid = ((FlowingFluid) (Object) this);
        int i = 0;
        int j = 0;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = blockpos$mutableblockpos.setWithOffset(pos, direction);
            BlockState blockstate = level.getBlockState(blockpos);
            FluidState fluidstate = level.getFluidState(blockpos);
            if (fluidstate.getType().isSame(flowingFluid) && canPassThroughWall(direction, level, pos, state, blockpos, blockstate)) {
                // Trick Neo event into thinking we're looking at a liquid block, so vanillaResult calculates the correct value
                if (fluidstate.isSource() && net.neoforged.neoforge.event.EventHooks.canCreateFluidSource(level, blockpos, fluidstate.createLegacyBlock().trySetValue(BlockStateProperties.LEVEL, fluidstate.getAmount()))) {
                    j++;
                }
                
                i = Math.max(i, fluidstate.getAmount());
            }
        }
        
        if (j >= 2) {
            BlockState blockstate1 = level.getBlockState(blockpos$mutableblockpos.setWithOffset(pos, Direction.DOWN));
            FluidState fluidstate1 = level.getFluidState(pos.below());
            //noinspection deprecation - copied from vanilla
            if (blockstate1.isSolid() || flowingFluid.isSourceBlockOfThisType(fluidstate1)) {
                
                cir.setReturnValue(flowingFluid.getSource(false));
                return;
            }
        }
        
        BlockPos blockpos1 = blockpos$mutableblockpos.setWithOffset(pos, Direction.UP);
        BlockState blockstate2 = level.getBlockState(blockpos1);
        FluidState fluidstate2 = level.getFluidState(blockpos1);
        if (!fluidstate2.isEmpty()
                && fluidstate2.getType().isSame(flowingFluid)
                && canPassThroughWall(Direction.UP, level, pos, state, blockpos1, blockstate2)) {
            
            cir.setReturnValue(flowingFluid.getFlowing(8, true));
        } else {
            int k = i - flowingFluid.getDropOff(level);
            
            cir.setReturnValue(k <= 0 ? Fluids.EMPTY.defaultFluidState() : flowingFluid.getFlowing(k, false));
        }
    }
    
    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_tick(ServerLevel level, BlockPos pos, BlockState bState, FluidState fState, CallbackInfo ci) {
        FlowingFluid thisFluid = ((FlowingFluid) (Object) this);
        if (!fState.isSource()) {
            FluidState newFluidState = thisFluid.getNewLiquid(level, pos, level.getBlockState(pos));
            int i = thisFluid.getSpreadDelay(level, pos, fState, newFluidState);
            if (newFluidState.isEmpty()) {
                // Make sure we use (and update) our data structure
                fState = newFluidState;
                if (bState.hasProperty(WATERLOGGED)) {
                    if (bState.getValue(WATERLOGGED)) {
                        level.setBlock(pos, bState.setValue(WATERLOGGED, Boolean.FALSE), 3);
                    }
                    ChunkAccess chunk = level.getChunkAt(pos);
                    chunk.getData(FAttachments.FLUID_STATES).map().remove(pos);
                    if (level instanceof ServerLevel sLevel) {
                        sLevel.getPlayers(player -> player.shouldRender(pos.getX(), pos.getY(), pos.getZ()))
                                .forEach(player ->
                                    player.connection.send(new FluidStateSyncPayload(pos, chunk.getData(FAttachments.FLUID_STATES)))
                                );
                    }
//                    chunk.syncData(FAttachments.FLUID_STATES);
                    chunk.markUnsaved();
                } else {
                    bState = Blocks.AIR.defaultBlockState();
                    level.setBlock(pos, bState, Block.UPDATE_ALL);
                }
            } else if (!newFluidState.equals(fState)) {
                // Make sure we use (and update) our data structure
                fState = newFluidState;
                if (bState.hasProperty(WATERLOGGED)) {
                    if (!bState.getValue(WATERLOGGED)) {
                        level.setBlock(pos, bState.setValue(WATERLOGGED, Boolean.TRUE), Block.UPDATE_CLIENTS);
                    }
                    ChunkAccess chunk = level.getChunkAt(pos);
                    chunk.getData(FAttachments.FLUID_STATES).map().put(pos, fState);
                    if (level instanceof ServerLevel sLevel) {
                        sLevel.getPlayers(player -> player.shouldRender(pos.getX(), pos.getY(), pos.getZ()))
                                .forEach(player ->
                                    player.connection.send(new FluidStateSyncPayload(pos, chunk.getData(FAttachments.FLUID_STATES)))
                                );
                    }
//                    chunk.syncData(FAttachments.FLUID_STATES);
                    chunk.markUnsaved();
                } else {
                    bState = newFluidState.createLegacyBlock();
                    level.setBlock(pos, bState, Block.UPDATE_CLIENTS);
                }
                level.scheduleTick(pos, newFluidState.getType(), i);
                level.updateNeighborsAt(pos, newFluidState.createLegacyBlock().getBlock());
            }
        }
        thisFluid.spread(level, pos, bState, fState);
        
        ci.cancel();
    }
}
