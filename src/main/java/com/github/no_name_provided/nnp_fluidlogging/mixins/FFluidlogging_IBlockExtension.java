package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@Mixin(IBlockExtension.class)
public interface FFluidlogging_IBlockExtension {
    
    /**
     * Forces certain recalcitrant blocks (LeavesBlock) to update their light levels when waterlogged. Actually, it
     * fails to do that. Shrug.
     */
    @Inject(method = "hasDynamicLightEmission(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("TAIL"), cancellable = true)
    private void nnp_f_fluidlogging_hasDynamicLightEmission(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        
        cir.setReturnValue(state.hasProperty(WATERLOGGED));
    }
    
    /**
     * Allows fluidlogged blocks to propagate/emit the higher of the two light levels (fluid vs block). Not to be
     * confused with the sort of emissive rendering that <i>doesn't</i> affect light levels, spawning, or the rendering
     * of nearby blocks.
     */
    @Inject(method = "getLightEmission(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_getLightEmission(BlockState state, BlockGetter getter, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (ServerConfig.considerFluidLightLevel) {
            // Anything that uses the ServerChunkCache during worldgen (like Level#getFluidState) will
            // create a server hang at
            // `return CompletableFuture.<ChunkAccess>supplyAsync(() -> this.getChunk(p_8360_, p_8361_, p_330876_, p_8363_), this.mainThreadProcessor).join();`
            // unless it's run on the same thread as the (main) server. This isn't an issue with proto chunks, etc.
            // Since chunks aren't thread safe, we may want to use a separate data structure for this down the line (like AuxLightManager)
            if (!(getter instanceof ServerLevel level) || level.getServer().isSameThread()) {
                //noinspection deprecation - used in the method we're injecting into
                cir.setReturnValue(Math.max(
                        state.getLightEmission(),
                        // Due to an unresolvable update execution order issue, we need to manually differentiate between logged and empty states
                        // We need to update the fluid state before the block to make most things work, but we need to not use the new fluid state when checking to see if light levels changed
                        // Could alternatively be solved with a mixin to the light engine method that checks to see if there were any changes before calling the update queuer.
                        state.hasProperty(WATERLOGGED) && state.getValue(WATERLOGGED) ?
                                getter.getFluidState(pos).getFluidType().getLightLevel() : 0
                ));
            }
        }
    }
}
