package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig;
import com.github.no_name_provided.nnp_fluidlogging.common.data_maps.contents.BlockStateFluidLevelLimits;
import com.github.no_name_provided.nnp_fluidlogging.common.wrappers.ClientClassWrappers;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;
import static com.github.no_name_provided.nnp_fluidlogging.common.data_maps.FDataMaps.BLOCKSTATE_FLUID_LEVEL_LIMITS;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

/**
 * Important mixins - allow SimpleWaterloggedBlock to interact with our attachment.
 */
@Mixin(SimpleWaterloggedBlock.class)
public interface FFluidlogging_SimpleWaterloggedBlock {
    /**
     * Allows us to filter which fluids SimpleWaterloggedBlock is compatible with. Needed to jailbreak vanilla
     * limitations, and makes a good hook for configurable white/blacklists.
     */
    @WrapMethod(method = "canPlaceLiquid(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z")
    private boolean nnp_f_fluidlogging_canPlaceLiquid(@Nullable LivingEntity livingEntity, BlockGetter getter, BlockPos pos, BlockState state, Fluid fluid, Operation<Boolean> original) {
        // Call this so any injected side effects can run, but ignore the return value because we don't care
        original.call(livingEntity, getter, pos, state, fluid);
        
        boolean isFluidBlacklisted = false;
        Optional<ResourceKey<Fluid>> key = BuiltInRegistries.FLUID.getResourceKey(fluid);
        if (key.isPresent()) {
            isFluidBlacklisted = ServerConfig.blacklistedFluids.contains(key.get().identifier().toString());
        }
        boolean isBlockBlacklisted = ServerConfig.blacklistedBlocks.contains(state.typeHolder().getRegisteredName());
        
        // Filter out fluids without buckets, to avoid an entire category of potential errors.
        // Default bucket is Items.AIR. Might also need to null check
        return !(isFluidBlacklisted || isBlockBlacklisted) && fluid.getBucket() != Items.AIR;
    }
    
    /**
     * Allows SimpleWaterloggedBlock to use our attachment when player's attempt to manually fill them.
     * <p>
     * While we're nice enough to return false if the operation should fail, this is ignored by the vanilla bucket
     * class, which always consumes the fluid contents. Shrug.
     * </p>
     * <p>
     * AuxiliaryLightManager doesn't consistently synchronize (outside a BlockEntity context), so we need to update it
     * on both sides.
     * </p>
     */
    @WrapMethod(method = "placeLiquid(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Z")
    private boolean nnp_f_fluidlogging_placeLiquid(
            LevelAccessor level,
            // Lies! This is secretly mutable, and violates the substitution principle
            BlockPos pos,
            BlockState state,
            FluidState nominalFluidState,
            Operation<Boolean> original
    ) {
        FluidState fluidState = nominalFluidState;
        // Nope out and return early if the fluid level is too low for the block
        @SuppressWarnings("deprecation") // probably more efficient than a registry lookup
        BlockStateFluidLevelLimits levelLimits = state.getBlock().builtInRegistryHolder().getData(BLOCKSTATE_FLUID_LEVEL_LIMITS);
        if (levelLimits != null && nominalFluidState.getAmount() < levelLimits.getMinLevel(state, nominalFluidState.getFluidType())) {
            
            return false;
        } else if (levelLimits != null && nominalFluidState.getAmount() > levelLimits.getMaxLevel(state, nominalFluidState.getFluidType())) {
            // Makes sure the level isn't too high
            fluidState = nominalFluidState.trySetValue(BlockStateProperties.LEVEL_FLOWING, levelLimits.getMaxLevel(state, nominalFluidState.getFluidType()));
        }
        
        // Get current states
        // Ensure we don't accidentally pass a MutableBlockPos to a buffer, since they violate the Liskov Substitution Principle
        BlockPos iPos = pos.immutable();
        ChunkAccess chunk = level.getChunk(iPos);
        FluidStates fluidStates = chunk.getData(FLUID_STATES);
        FluidState oldFluidState = fluidStates.getOrDefault(iPos, state.getFluidState());
        boolean wasLogged = state.getValue(WATERLOGGED);
        // Handle lighting
        AuxiliaryLightManager lManager = level.getAuxLightManager(iPos);
        boolean lManagerExists = lManager != null;
        
        if (
            // Don't let players waste buckets
                (fluidState.isSource() && !oldFluidState.isSource()) ||
                        // but maybe allow fluids to flow in
                        (ServerConfig.flowingFluidsCanLog && !oldFluidState.isSource())
        ) {
            // Special case fully waterlogged blocks
            if (fluidState.is(Fluids.WATER) && fluidState.isSource()) {
                // Only sync if we actually update our data structure
                if (fluidStates.remove(iPos) != null && !level.isClientSide()) {
//                    if (level instanceof ServerLevel sLevel) {
//                        sLevel.getPlayers(player -> player.shouldRender(pos.getX(), pos.getY(), pos.getZ()))
//                                .forEach(player ->
//                                        player.connection.send(new FluidStateSyncPayload(pos, chunk.getData(FAttachments.FLUID_STATES)))
//                                );
//                    }
                    chunk.syncData(FLUID_STATES);
                }
                if (lManagerExists) {
                    lManager.removeLightAt(iPos);
                }
                if (level.isClientSide()) {
                    // Strangely, this is the one place where setting the blocks dirty actually had an effect on rendering
                    ClientClassWrappers.setDirtyFromSharedCode(level, pos, state.setValue(WATERLOGGED, true), state.setValue(WATERLOGGED, false));
                }
                chunk.markUnsaved();
                // Handle the rest
            } else {
                fluidStates.put(iPos, fluidState);
                if (!level.isClientSide()) {
//                    if (level instanceof ServerLevel sLevel) {
//                        sLevel.getPlayers(player -> player.shouldRender(pos.getX(), pos.getY(), pos.getZ()))
//                                .forEach(player ->
//                                        player.connection.send(new FluidStateSyncPayload(pos, chunk.getData(FAttachments.FLUID_STATES)))
//                                );
//                    }
                    chunk.syncData(FLUID_STATES);
                }
                if (lManagerExists) {
                    lManager.setLightAt(iPos, fluidState.getFluidType().getLightLevel(fluidState, level, iPos));
                }
                if (level.isClientSide()) {
                    // Does nothing here
                    ClientClassWrappers.setDirtyFromSharedCode(level, iPos, state.setValue(WATERLOGGED, true), state.setValue(WATERLOGGED, false));
                }
                chunk.markUnsaved();
            }
            // If these run on the client, they'll trigger before the attachment syncs
            level.setBlock(iPos, state.setValue(WATERLOGGED, true), Block.UPDATE_ALL);
            level.scheduleTick(iPos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            // Force a chunk update, if there otherwise wouldn't be one
            if (ServerConfig.forceChunkUpdates) {
                if (wasLogged && level instanceof ServerLevel sLevel && chunk instanceof LevelChunk lChunk) {
                    sLevel.getPlayers(player ->
                            player.shouldRenderAtSqrDistance(player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()))
                    ).forEach(player -> {
                                player.connection.chunkSender.markChunkPendingToSend(lChunk);
                                player.connection.chunkSender.sendNextChunks(player);
                            }
                    );
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Allows SimpleWaterloggedBlock to use our attachment when player's attempt to manually drain them.
     * <p>
     * This runs on both sides, so the sync issues don't affect it.
     * </p>
     */
    @Inject(method = "pickupBlock(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), cancellable = true)
    private void nnp_f_fluidlogging_pickupBlock(@Nullable LivingEntity livingEntity, LevelAccessor level, BlockPos pos, BlockState
            state, CallbackInfoReturnable<ItemStack> cir) {
        // Unsetting the waterlogged flag is handled by the vanilla method we're injecting after
        BlockPos iPos = pos.immutable();
        ChunkAccess chunk = level.getChunk(iPos);
        FluidStates states = chunk.getData(FLUID_STATES);
        // Handle lighting
        AuxiliaryLightManager lManager = level.getAuxLightManager(iPos);
        boolean lManagerExists = lManager != null;
        // For vanilla support, we default to the blockstate based waterlogging check
        FluidState fluidState = states.map().getOrDefault(iPos, chunk.getFluidState(iPos));
        
        // Vanilla default for waterlogged blocks. For consistency with worldgenned waterlogged blocks, water is _not_
        // stored in our data structure. Instead, it's represented by a waterlogged=true block without a corresponding
        // entry in our attachment. Note that the original waterlogged value will be cleared by the time this condition
        // is hit, so we need to look at side effects (the vanilla return value)
        if (cir.getReturnValue().is(Items.WATER_BUCKET) && fluidState.isEmpty()) {
            
            cir.cancel();
            // Only matches source blocks (flowing blocks are a different class)
        } else if (fluidState.is(Fluids.LAVA)) {
            states.map().remove(iPos);
//            chunk.syncData(FLUID_STATES);
            if (lManagerExists) {
                lManager.removeLightAt(iPos);
            }
            chunk.markUnsaved();
            
            // Lava has a weird, limited type implementation, so we don't trust it
            cir.setReturnValue(Items.LAVA_BUCKET.getDefaultInstance());
        } else if (!fluidState.isEmpty() && fluidState.isSource()) {
            states.map().remove(iPos);
//            chunk.syncData(FLUID_STATES);
            if (lManagerExists) {
                lManager.setLightAt(iPos, fluidState.getFluidType().getLightLevel(fluidState, level, iPos));
            }
            chunk.markUnsaved();
            
            // TODO: test with partial fluid implementations (no flowing, bucket, etc.)
            cir.setReturnValue(fluidState.getType().getBucket().getDefaultInstance());
        } else {
            states.map().remove(iPos);
//            chunk.syncData(FLUID_STATES);
            if (lManagerExists) {
                lManager.removeLightAt(iPos);
            }
            chunk.markUnsaved();
            
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
