package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

@Mixin(SimpleWaterloggedBlock.class)
public interface FFluidlogging_SimpleWaterloggedBlock {
    
    @WrapMethod(method = "canPlaceLiquid(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z")
    private boolean nnp_f_fluidlogging_canPlaceLiquid(Player player, BlockGetter getter, BlockPos pos, BlockState state, Fluid fluid, Operation<Boolean> original) {
        // Call this so any injected side effects can run, but ignore the return value because we don't care
        original.call(player, getter, pos, state, fluid);
        
        // TODO: White/Blacklist
        return true;
    }
    
    @WrapMethod(method = "placeLiquid(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Z")
    private boolean nnp_f_fluidlogging_placeLiquid(
            LevelAccessor level,
            /*Lies! This is secretly mutable, and violates the substitution principle*/
            BlockPos pos,
            BlockState state,
            FluidState fluidState,
            Operation<Boolean> original
    ) {
        if (fluidState.getType() == Fluids.WATER) {
            
            return original.call(level, pos, state, fluidState);
        } else {
            if (!state.getValue(BlockStateProperties.WATERLOGGED)) {
                // Syncing to client is handled by the attachment
                if (!level.isClientSide()) {
                    // Conditional has side effect
                    if (level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE), 3)) {
                        ChunkAccess chunk = level.getChunk(pos);
                        chunk.getData(FLUID_STATES).map().put(pos.immutable(), fluidState);
                        // Since we mutate the data, rather than replacing it, we need to manually trigger a sync
                        chunk.syncData(FLUID_STATES);
                        chunk.setUnsaved(true);
                        level.scheduleTick(pos.immutable(), fluidState.getType(), fluidState.getType().getTickDelay(level));
                    }
                }
                
                return true;
            } else {
                
                return false;
            }
        }
    }
    
    @Inject(method = "pickupBlock(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), cancellable = true)
    private void nnp_f_fluidlogging_pickupBlock(Player player, LevelAccessor level, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        // Unsetting the waterlogged flag is handled by the vanilla method we're injecting after
        
        ChunkAccess chunk = level.getChunk(pos);
        FluidStates states = chunk.getData(FLUID_STATES);
        // For vanilla support, we default to the blockstate based waterlogging check
        FluidState fluidState = states.map().getOrDefault(pos, chunk.getFluidState(pos));
        states.map().remove(pos);
        chunk.setUnsaved(true);
        level.scheduleTick(pos.immutable(), fluidState.getType(), fluidState.getType().getTickDelay(level));
        
        // Vanilla default for waterlogged blocks
        if (fluidState.is(Fluids.WATER)) {
            
            cir.cancel();
        } else if (fluidState.is(Fluids.LAVA)) {
            
            // Lava has a weird, limited type implementation, so we don't trust it
            cir.setReturnValue(Items.LAVA_BUCKET.getDefaultInstance());
        } else if (!fluidState.isEmpty() && fluidState.isSource()) {
            
            // TODO: test with partial fluid implementations (no flowing, bucket, etc.)
            cir.setReturnValue(fluidState.getType().getBucket().getDefaultInstance());
        }
    }
}
