package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * These mixins cause adjacent fluid logged blocks are treated as sources for (already) flowing fluids.
 */
@Mixin(LiquidBlockRenderer.class)
public class FFluidlogging_LiquidBlockRenderer {
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.DOWN), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate1"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate1(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.UP), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate2"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate2(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.NORTH), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate3"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate3(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.SOUTH), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate4"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate4(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.WEST), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidstate5"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidstate5(FluidState value, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.EAST), level);
    }
    
    /**
     * Convenience method for checking fluid states during rendering with some degree of efficiency.
     *
     * @param pos   Location of block space.
     * @param level BlockAndTintGetter. This isn't necessarily going to be a level.
     * @return The fluid state to be used for rendering.
     */
    @Unique
    private static FluidState NNPFluidlogging$checkFluidState(BlockPos pos, BlockAndTintGetter level) {
        BlockState state = level.getBlockState(pos);
        // This first check is just an efficiency thing, borrowed from vanilla
        if (!state.isAir()) {
            if (state.getBlock() instanceof LiquidBlock) {
                
                return state.getFluidState();
            } else if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                
                return level.getFluidState(pos);
            }
        }
        
        return Fluids.EMPTY.defaultFluidState();
    }
}
