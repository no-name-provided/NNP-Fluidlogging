package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments;
import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig;
import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.contents.BlockStateFluidLevelLimits;
import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.AuxLightManagerUpdatePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.no_name_provided.nnp_fluidlogging.common.data_maps.FDataMaps.BLOCKSTATE_FLUID_LEVEL_LIMITS;

/**
 * Important mixins - clear fluid from data structure when logged blocks are broken.
 */
@Mixin(BlockBehaviour.class)
abstract class FFluidlogging_BlockBehavior {
    
    /**
     * Since many (most?) SimpleWaterloggedBlocks call this in their override, it makes sense to add the fluid tick
     * here. This is less efficient than dedicated overrides for each class. Some classes (eg, coral fans) will still
     * need dedicated overrides since they don't call this method.
     */
    @Inject(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("TAIL"))
    private void nnp_f_fluidlogging_updateShape(BlockState newState, Direction direction, BlockState oldState, LevelAccessor level, BlockPos pos, BlockPos triggerPos, CallbackInfoReturnable<BlockState> cir) {
        FluidState fluidState = level.getFluidState(pos);
        if (newState.getBlock() instanceof SimpleWaterloggedBlock && !fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }
    
    /**
     * Add in-world fluid to logged state when block is placed. This replicates vanilla behavior that's ordinarily
     * handled with block-specific overrides.
     * <p>
     * Only executes on server side.
     * </p>
     */
    @Inject(method = "onPlace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V",
            at = @At("HEAD"))
    private void nnp_f_fluidlogging_onPlace(BlockState newState, Level level, BlockPos pos, BlockState oldState, boolean flag, CallbackInfo ci) {
        if (newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            
            // Respect fluid level limits - only works for flowing fluids
            //noinspection deprecation - #liquid is widely used in vanilla
            if (oldState.liquid() && newState.getBlock() instanceof SimpleWaterloggedBlock simpleWaterloggedBlock && simpleWaterloggedBlock.canPlaceLiquid(null, level, pos, newState, oldState.getFluidState().getType())) {
                if (oldState.getFluidState().getType() instanceof FlowingFluid flowingFluid) {
                    @SuppressWarnings("deprecation") //probably more efficient than a registry lookup
                    BlockStateFluidLevelLimits levelLimits = oldState.getBlock().builtInRegistryHolder().getData(BLOCKSTATE_FLUID_LEVEL_LIMITS);
                    if (levelLimits != null && oldState.getFluidState().getAmount() < levelLimits.getMinLevel(newState, oldState.getFluidState().getFluidType())) {
                        oldState = Fluids.EMPTY.defaultFluidState().createLegacyBlock();
                    } else if (levelLimits != null && oldState.getFluidState().getAmount() > levelLimits.getMaxLevel(newState, oldState.getFluidState().getFluidType())) {
                        oldState = flowingFluid.getFlowing(levelLimits.getMaxLevel(oldState, oldState.getFluidState().getFluidType()), false).createLegacyBlock();
                    }
                }
                
                // Filter out flowing liquids... unless we're going to try to handle them
                if (oldState.getFluidState().isSource() || ServerConfig.flowingFluidsCanLog) {
                    ChunkAccess chunk = level.getChunk(pos);
                    // We don't need to avoid #getFluidState for liquid blocks.
                    // Since they can't be logged, it's always correct
                    chunk.getData(FAttachments.FLUID_STATES).put(pos, oldState.getFluidState());
                    if (!level.isClientSide()) {
                        chunk.syncData(FAttachments.FLUID_STATES);
                    }
                    AuxiliaryLightManager lManager = level.getAuxLightManager(pos);
                    if (lManager != null) {
                        lManager.setLightAt(pos, oldState.getFluidState().getFluidType().getLightLevel(oldState.getFluidState(), level, pos));
                    }
                    if (!level.isClientSide()) {
                        // This must come last, otherwise the incorrect FluidState will be used for the block updates
                        level.setBlock(pos, newState.setValue(BlockStateProperties.WATERLOGGED, true), Block.UPDATE_ALL);
                    }
                    chunk.setUnsaved(true);
                    if (ServerConfig.forceChunkUpdates) {
                        if (level instanceof ServerLevel sLevel && chunk instanceof LevelChunk lChunk) {
                            sLevel.getPlayers(player ->
                                    player.shouldRenderAtSqrDistance(player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()))
                            ).forEach(player -> {
                                        player.connection.chunkSender.markChunkPendingToSend(lChunk);
                                        player.connection.chunkSender.sendNextChunks(player);
                                    }
                            );
                        }
                    } else if (ServerConfig.considerFluidLightLevel && level instanceof ServerLevel sLevel) {
                        // Send our custom light level update packet
                        BlockState finalOldState = oldState;
                        sLevel.getPlayers(player -> player.level().equals(level)).forEach(player ->
                                player.connection.send(new AuxLightManagerUpdatePayload(
                                        finalOldState.getFluidState().getFluidType().getLightLevel(finalOldState.getFluidState(), level, pos),
                                        pos.asLong()))
                        );
                    }
                }
            }
        } else {
            // Clean up any lingering entry in our data structure. Shouldn't be necessary,
            // and can probably be removed if there's a performance issue
            ChunkAccess chunk = level.getChunk(pos);
            if (!level.isClientSide()) {
                FluidStates states = chunk.getData(FAttachments.FLUID_STATES);
                states.remove(pos);
                chunk.syncData(FAttachments.FLUID_STATES);
            }
            AuxiliaryLightManager lManager = level.getAuxLightManager(pos);
            if (lManager != null) {
                lManager.removeLightAt(pos);
            }
            chunk.setUnsaved(true);
            // Fixes desync issue
            if (ServerConfig.forceChunkUpdates) {
                // Crude approach
                if (level instanceof ServerLevel sLevel && chunk instanceof LevelChunk lChunk) {
                    sLevel.getPlayers(player ->
                            player.shouldRenderAtSqrDistance(player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()))
                    ).forEach(player -> {
                                player.connection.chunkSender.markChunkPendingToSend(lChunk);
                                player.connection.chunkSender.sendNextChunks(player);
                            }
                    );
                }
            } else if (ServerConfig.considerFluidLightLevel && level instanceof ServerLevel sLevel) {
                // Send our custom light level update packet (targeted approach)
                sLevel.getPlayers(player -> player.level().equals(level)).forEach(player ->
                        player.connection.send(new AuxLightManagerUpdatePayload(
                                0,
                                pos.asLong()))
                );
            }
        }
        
    }
    
    /**
     * Clear logged state when block is removed. Placing LiquidBlock equivalent in world is handled by vanilla
     * Level#removeBlock. That runs at the tail of the method this mixes into, so we need to do a tail inject (not head)
     * and early returns added by other mods could be a problem.
     * <p>
     * This isn't called on the client side.
     * </p>
     */
    @Inject(method = "onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V",
            at = @At("TAIL"))
    private void nnp_f_fluidlogging_onRemove(BlockState newState, Level level, BlockPos pos, BlockState oldState, boolean flag, CallbackInfo ci) {
        if (!newState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            ChunkAccess chunk = level.getChunk(pos);
            // This is fine, since we shouldn't have null values in this map
            if (!level.isClientSide() && chunk.getData(FAttachments.FLUID_STATES).remove(pos) != null) {
                chunk.syncData(FAttachments.FLUID_STATES);
                chunk.setUnsaved(true);
            }
            
            AuxiliaryLightManager lManager = level.getAuxLightManager(pos);
            if (lManager != null) {
                lManager.removeLightAt(pos);
            }
            if (ServerConfig.forceChunkUpdates) {
                if (level instanceof ServerLevel sLevel && level.getChunk(pos) instanceof LevelChunk lChunk) {
                    sLevel.getPlayers(player ->
                            player.shouldRenderAtSqrDistance(player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()))
                    ).forEach(player -> {
                                player.connection.chunkSender.markChunkPendingToSend(lChunk);
                                player.connection.chunkSender.sendNextChunks(player);
                            }
                    );
                }
            } else if (ServerConfig.considerFluidLightLevel && level instanceof ServerLevel sLevel) {
                // Send our custom light level update packet
                sLevel.getPlayers(player -> player.level().equals(level)).forEach(player ->
                        player.connection.send(new AuxLightManagerUpdatePayload(
                                // Setting the light level to 0 is the same as calling #removeLightAt
                                0,
                                pos.asLong()))
                );
            }
        }
    }
}
