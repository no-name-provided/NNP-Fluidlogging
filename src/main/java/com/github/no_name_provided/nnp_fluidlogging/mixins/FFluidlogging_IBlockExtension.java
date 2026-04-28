package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
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
     * Allows fluidlogged blocks to propagate/emit the higher of the two light levels (fluid vs block). Not to be
     * confused with the sort of emissive rendering that <i>doesn't</i> affect light levels, spawning, or the rendering
     * of nearby blocks.
     */
    @Inject(method = "getLightEmission(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_getLightEmission(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        
        //noinspection deprecation - used in the method we're injecting into
        cir.setReturnValue(Math.max(
                state.getLightEmission(),
                // Due to an unresolvable update execution order issue, we need to manually differentiate between logged and empty states
                // We need to update the fluid state before the block to make most things work, but we need to not use the new fluid state when checking to see if light levels changed
                // Could alternatively be solved with a mixin to the light engine method that checks to see if there were any changes before calling the update queuer.
                //
                // One of the BlockGetter variants we can be passed during worldgen tends to create a server-side thread
                // lock when we call Level#getFluidState on it, so we limit ourselves to full-fledged level accessors.
                // Might cause issues with worldgen blocks until the first update. It's possible this isn't calling our
                // #getFluidState mixin, and could be better resolved by mixing in a redirect. ProtoChunk#getFluidState
                // seems like a likely culprit
                state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED) ? (this instanceof LevelAccessor ? level.getFluidState(pos).getFluidType().getLightLevel() : 0) : 0
        ));
    }
}
