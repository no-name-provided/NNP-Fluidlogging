package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;
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
    @WrapMethod(method = "canPlaceLiquid(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z")
    private boolean nnp_f_fluidlogging_canPlaceLiquid(Player player, BlockGetter getter, BlockPos pos, BlockState state, Fluid fluid, Operation<Boolean> original) {
        // Call this so any injected side effects can run, but ignore the return value because we don't care
        original.call(player, getter, pos, state, fluid);
        
        boolean isBlacklisted = false;
        Optional<ResourceKey<Fluid>> key = BuiltInRegistries.FLUID.getResourceKey(fluid);
        if (key.isPresent()) {
            isBlacklisted = ServerConfig.blacklistedFluids.contains(key.get().location().toString());
        }
        
        // Filter out fluids without buckets, to avoid an entire category of potential errors.
        // Default bucket is Items.AIR. Might also need to null check
        return !isBlacklisted;
    }
    
    /**
     * Allows SimpleWaterloggedBlock to use our attachment when player's attempt to manually fill them.
     */
    @WrapMethod(method = "placeLiquid(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Z")
    private boolean nnp_f_fluidlogging_placeLiquid(
            LevelAccessor level,
            // Lies! This is secretly mutable, and violates the substitution principle
            BlockPos pos,
            BlockState state,
            FluidState fluidState,
            Operation<Boolean> original
    ) {
        // Get current states
        // Ensure we don't accidentally pass a MutableBlockPos to a buffer, since they violate the Liskov Substitution Principle
        BlockPos iPos = pos.immutable();
        ChunkAccess chunk = level.getChunk(iPos);
        Map<BlockPos, FluidState> fluidStates = chunk.getData(FLUID_STATES).map();
        FluidState oldFluidState = fluidStates.getOrDefault(iPos, state.getFluidState());
        boolean wasLogged = state.getValue(WATERLOGGED);
        
        if (
            // Don't let players waste buckets
                (fluidState.isSource() && !oldFluidState.isSource()) ||
                        // but maybe allow fluids to flow in
                        (ServerConfig.flowingFluidsCanLog && !fluidState.isSource())
        ) {
            if (!level.isClientSide()) {
                // Special case fully waterlogged blocks
                if (fluidState.is(Fluids.WATER) && fluidState.isSource()) {
                    // Only sync if we actually update our structure
                    if (fluidStates.remove(iPos) != null) {
                        chunk.syncData(FLUID_STATES);
                        chunk.setUnsaved(true);
                    }
                    // Handle the rest
                } else {
                    fluidStates.put(iPos, fluidState);
                    chunk.syncData(FLUID_STATES);
                    chunk.setUnsaved(true);
                }
                // Unlike block entities, block states don't actually send updates unless they change
                // Even changing them and immediately changing them back doesn't seem to cause an update
                if (wasLogged && level instanceof Level realLevel) {
                    // Since Level#markAndNotify, et at. don't seem to work (on either side), we are at an impasse
                }
                // If these run on the client, they'll trigger before the attachment syncs and render the wrong fluid
                // until a neighbor updates the BlockState
                level.setBlock(iPos, state.setValue(WATERLOGGED, true), Block.UPDATE_ALL);
                level.scheduleTick(iPos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Allows SimpleWaterloggedBlock to use our attachment when player's attempt to manually drain them.
     */
    @Inject(method = "pickupBlock(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), cancellable = true)
    private void nnp_f_fluidlogging_pickupBlock(Player player, LevelAccessor level, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        // Unsetting the waterlogged flag is handled by the vanilla method we're injecting after
        ChunkAccess chunk = level.getChunk(pos);
        FluidStates states = chunk.getData(FLUID_STATES);
        // For vanilla support, we default to the blockstate based waterlogging check
        FluidState fluidState = states.map().getOrDefault(pos, chunk.getFluidState(pos));
        
        // Vanilla default for waterlogged blocks. For consistency with worldgenned waterlogged blocks, water is _not_
        // stored in our data structure. Instead, it's represented by a waterlogged=true block without a corresponding
        // entry in our attachment. Note that the original waterlogged value will be cleared by the time this condition
        // is hit, so we need to look at side effects (the vanilla return value)
        if (cir.getReturnValue().is(Items.WATER_BUCKET) && fluidState.isEmpty()) {
            
            cir.cancel();
            // Only matches source blocks (flowing blocks are a different class)
        } else if (fluidState.is(Fluids.LAVA)) {
            states.map().remove(pos);
            chunk.syncData(FLUID_STATES);
            chunk.setUnsaved(true);
            level.scheduleTick(pos.immutable(), fluidState.getType(), fluidState.getType().getTickDelay(level));
            
            // Lava has a weird, limited type implementation, so we don't trust it
            cir.setReturnValue(Items.LAVA_BUCKET.getDefaultInstance());
        } else if (!fluidState.isEmpty() && fluidState.isSource()) {
            states.map().remove(pos);
            chunk.syncData(FLUID_STATES);
            chunk.setUnsaved(true);
            level.scheduleTick(pos.immutable(), fluidState.getType(), fluidState.getType().getTickDelay(level));
            
            // TODO: test with partial fluid implementations (no flowing, bucket, etc.)
            cir.setReturnValue(fluidState.getType().getBucket().getDefaultInstance());
        } else {
            states.map().remove(pos);
            chunk.syncData(FLUID_STATES);
            chunk.setUnsaved(true);
            
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
