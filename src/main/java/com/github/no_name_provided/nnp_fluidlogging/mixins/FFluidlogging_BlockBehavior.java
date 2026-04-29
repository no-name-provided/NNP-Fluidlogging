package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments;
import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Important mixins - clear fluid from data structure when logged blocks are broken. Attempt to apply fluid
 * light levels.
 */
@Mixin(BlockBehaviour.class)
abstract class FFluidlogging_BlockBehavior {
    /**
     * Add in-world fluid to logged state when block is placed. This replicates vanilla behavior that's
     * ordinarily handled with block-specific overrides.
     */
    @Inject(method = "onPlace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V",
            at = @At("HEAD"))
    private void nnp_f_fluidlogging_onPlace(BlockState newState, Level level, BlockPos pos, BlockState oldState, boolean flag, CallbackInfo ci) {
        if (!level.isClientSide()) {
            if (newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                //noinspection deprecation - #liquid is widely used in vanilla
                if (oldState.liquid()) {
                    // Filter out flowing liquids... unless we're going to try to handle them
                    if (oldState.getFluidState().isSource() || ServerConfig.flowingFluidsCanBeWaterlogged) {
                        ChunkAccess chunk = level.getChunk(pos);
                        // We don't need to avoid #getFluidState for liquid blocks.
                        // Since they can't be logged, it's always correct
                        chunk.getData(FAttachments.FLUID_STATES).map().put(pos, oldState.getFluidState());
                        chunk.syncData(FAttachments.FLUID_STATES);
                        chunk.setUnsaved(true);
                        // This must come last, otherwise the incorrect FluidState will be used for the block updates
                        level.setBlock(pos, newState.setValue(BlockStateProperties.WATERLOGGED, true), Block.UPDATE_ALL);
                    }
                }
            } else {
                // Clean up any lingering entry in our data structure. Shouldn't be necessary,
                // and can probably be removed if there's a performance issue
                ChunkAccess chunk = level.getChunk(pos);
                FluidStates states = chunk.getData(FAttachments.FLUID_STATES);
                states.map().remove(pos);
                chunk.syncData(FAttachments.FLUID_STATES);
                chunk.setUnsaved(true);
            }
        }
    }
    
    /**
     * Clear logged state when block is removed. Placing LiquidBlock equivalent in world is handled by vanilla
     * Level#removeBlock. That runs at the tail of the method this mixes into, so we need to do a tail inject
     * (not head) and early returns added by other mods could be a problem.
     */
    @Inject(method = "onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V",
            at = @At("TAIL"))
    private void nnp_f_fluidlogging_onRemove(BlockState newState, Level level, BlockPos pos, BlockState oldState, boolean flag, CallbackInfo ci) {
        if (!level.isClientSide() && !newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            ChunkAccess chunk = level.getChunk(pos);
            // This is fine, since we shouldn't have null values in this map
            if (chunk.getData(FAttachments.FLUID_STATES).map().remove(pos) != null) {
                chunk.syncData(FAttachments.FLUID_STATES);
                chunk.setUnsaved(true);
            }
        }
    }
}
