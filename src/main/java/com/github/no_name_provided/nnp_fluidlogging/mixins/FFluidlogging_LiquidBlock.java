package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

@Mixin(LiquidBlock.class)
abstract class FFluidlogging_LiquidBlock extends Block implements BucketPickup {
    public FFluidlogging_LiquidBlock(Properties props) {
        super(props);
    }
    
    /**
     * May not be necessary.
     */
    @WrapMethod(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    private BlockState nnp_f_fluidlogging_updateShape(BlockState bState, Direction direction, BlockState triggerPos, LevelAccessor level, BlockPos pos, BlockPos otherPos, Operation<BlockState> original) {
        FluidState state = level.getChunk(pos).getData(FLUID_STATES).map().get(pos);
        if (state != null) {
            level.scheduleTick(pos, state.getType(), state.getType().getTickDelay(level));
            
            return bState;
        } else {
            
            return original.call(bState, direction, triggerPos, level, pos, otherPos);
        }
    }
    
    /**
     * Force shape updates to prefer our data structure. May not be necessary.
     */
    @Redirect(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
    private void nnp_f_fluidlogging_updateShape_fixScheduleTick(LevelAccessor level, BlockPos pos, Fluid fluid, int tickDelay) {
        FluidState state = level.getChunk(pos).getData(FLUID_STATES).map().get(pos);
        level.scheduleTick(pos, state == null ? Fluids.WATER : state.getType(), (state == null ? Fluids.WATER : state.getType()).getTickDelay(level));
    }
    
//    /**
//     * Breaks vanilla flow. Force neighbor updates to prefer our data structure. May not be necessary.
//     */
//    @Redirect(method = "neighborChanged(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
//    private void nnp_f_fluidlogging_neighborChanged_fixScheduleTick(Level level, BlockPos pos, Fluid fluid, int tickDelay) {
//        FluidState state = level.getChunk(pos).getData(FLUID_STATES).map().get(pos);
//        level.scheduleTick(pos, state == null ? Fluids.WATER : state.getType(), (state == null ? Fluids.WATER : state.getType()).getTickDelay(level));
//    }
}
