package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IBlockExtension.class)
public interface FFluidlogging_IBlockExtension {
    
    /**
     * Forces certain recalcitrant blocks (LeavesBlock) to update their light levels when waterlogged. Actually, it
     * fails to do that. Shrug.
     */
    @Inject(method = "hasDynamicLightEmission(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("TAIL"), cancellable = true)
    private void nnp_f_fluidlogging_hasDynamicLightEmission(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        
        cir.setReturnValue(state.hasProperty(BlockStateProperties.WATERLOGGED));
    }
    
    /**
     * Allows fluidlogged blocks to propagate the higher of the two light levels (fluid vs block).
     */
    @Inject(method = "getLightEmission(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_getLightEmission(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        
        //noinspection deprecation - used in the method we're injecting into
        cir.setReturnValue(Math.max(
                state.getLightEmission(),
                // Due to an unresolvable update execution order issue, we need to manually differentiate between logged and empty states
                // We need to update the fluid state before the block to make most things work, but we need to not use the new fluid state when checking to see if light levels changed
                // Could alternatively be solved with a mixin to the light engine method that checks to see if there were any changes before calling the update queuer
                state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? level.getFluidState(pos).getFluidType().getLightLevel() : 0
        ));
    }
}
