package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
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
            AuxiliaryLightManager lManager = getter.getAuxLightManager(pos);
            //noinspection deprecation - widely used in vanilla
            if (lManager != null && !state.liquid()) {
                
                cir.setReturnValue(lManager.getLightAt(pos));
            } else {
                
                //noinspection deprecation - used in the method we're injecting into
                cir.setReturnValue(state.getLightEmission());
            }
        }
    }
}
