package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * These mixins cause adjacent fluid logged blocks are treated as sources for (already) flowing fluids. They also stop
 * you from seeing the "face" of a fluid that should be "hidden" behind an adjacent block of the same fluid (this is
 * only a problem for mods that add transparent fluids, but it's a big graphical eyesore for those).
 */
@SuppressWarnings("LocalMayBeArgsOnly")
// The compiler always produces this warning, even when I'm not using method arguments and setting this flag will result in a crash
@Mixin(FluidRenderer.class)
abstract class FFluidlogging_FluidRenderer {
    @Shadow @Final
    protected abstract float getHeight(BlockAndTintGetter level, Fluid fluidType, BlockPos pos, BlockState state, FluidState fluidState);
    
    @ModifyArg(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/FluidRenderer;shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0),
            index = 3
    )
    private BlockState nnp_f_fluidlogging_tesselate_fixDownOcclusion(BlockState fluidState, @Local(name = "pos") BlockPos pos, @Local(name = "level") BlockAndTintGetter level) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.DOWN), level).createLegacyBlock();
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidStateDown"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidStateDown(FluidState value, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.DOWN), level);
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidStateUp"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidStateUp(FluidState value, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.UP), level);
    }
    
    @ModifyArg(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/FluidRenderer;shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 1),
            index = 3
    )
    private BlockState nnp_f_fluidlogging_tesselate_fixNorthOcclusion(BlockState fluidState, @Local(name = "pos") BlockPos pos, @Local(name = "level") BlockAndTintGetter level) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.NORTH), level).createLegacyBlock();
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidStateNorth"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidStateNorth(FluidState value, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.NORTH), level);
    }
    
    @ModifyArg(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/FluidRenderer;shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 2),
            index = 3
    )
    private BlockState nnp_f_fluidlogging_tesselate_fixSouthOcclusion(BlockState fluidState, @Local(name = "pos") BlockPos pos, @Local(name = "level") BlockAndTintGetter level) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.SOUTH), level).createLegacyBlock();
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidStateSouth"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidStateSouth(FluidState value, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.SOUTH), level);
    }
    
    @ModifyArg(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/FluidRenderer;shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 3),
            index = 3
    )
    private BlockState nnp_f_fluidlogging_tesselate_fixWestOcclusion(BlockState blockState, @Local(name = "pos") BlockPos pos, @Local(name = "level") BlockAndTintGetter level) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.WEST), level).createLegacyBlock();
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidStateWest"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidStateWest(FluidState value, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.WEST), level);
    }
    
    @ModifyArg(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/FluidRenderer;shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 4),
            index = 3
    )
    private BlockState nnp_f_fluidlogging_tesselate_fixEastOcclusion(BlockState fluidState, @Local(name = "pos") BlockPos pos, @Local(name = "level") BlockAndTintGetter level) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.EAST), level).createLegacyBlock();
    }
    
    @ModifyVariable(method = "tesselate(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/client/renderer/block/FluidRenderer$Output;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V",
            at = @At("STORE"),
            name = "fluidStateEast"
    )
    private FluidState nnp_f_fluidlogging_tesselate_fluidStateEast(FluidState value, BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
        
        return NNPFluidlogging$checkFluidState(pos.relative(Direction.EAST), level);
    }
    
    /**
     * This is actually a mixin for a wrapper for the "real" getHeight function. We make it pass the correct FluidState
     * as the last parameter to the wrapped call. Fixes glitch where fluid blocks diagonal to fluidlogged blocks treat
     * those blocks as having the incorrect fluidstate, and render with one corner too low (creating a visual gap in the
     * LiquidBlocks rendered).
     */
    @Redirect(method = "getHeight(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/FluidRenderer;getHeight(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F"))
    private float nnp_f_fluidlogging_getHeight_fixReturn(FluidRenderer instance, BlockAndTintGetter level, Fluid fluid, BlockPos pos, BlockState blockState, FluidState fluidState) {
        
        return getHeight(level, fluid, pos, blockState, level.getFluidState(pos));
    }
    
    /**
     * Forces fluids to prefer our data structure when falling down off of a logged block. Prevents gap between source
     * and first flowing block.
     */
    @Redirect(method = "getHeight(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/Fluid;isSame(Lnet/minecraft/world/level/material/Fluid;)Z", ordinal = 1)
    )
    private boolean nnp_f_fluidlogging_getHeight_isSame(Fluid blockStateBasedFluidState, Fluid fluid, BlockAndTintGetter level, Fluid thisFluid, BlockPos pos) {
        
        return thisFluid.isSame(NNPFluidlogging$checkFluidState(pos.above(), level).getType());
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
        
        // We can't assume this is empty, as it might be like kelp (a fluid container that isn't waterloggable)
        return state.getFluidState();
    }
}
