package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

@Mixin(IBlockExtension.class)
public interface FFluidlogging_IBlockExtension {
    
    /**
     * Needed (in theory) to ensure logged blocks update their light level when their fluid changes.
     */
    @ModifyReturnValue(method = "hasDynamicLightEmission(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("RETURN"))
    private boolean nnp_f_fluidlogging_hasDynamicLightEmission(boolean original, BlockState state) {
        
        return state.hasProperty(WATERLOGGED) || original;
    }
    
    /**
     * Allows fluidlogged blocks to propagate/emit the higher of the two light levels (fluid vs block). Not to be
     * confused with the sort of emissive rendering that <i>doesn't</i> affect light levels, spawning, or the rendering
     * of nearby blocks.
     */
    @ModifyReturnValue(method = "getLightEmission(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"))
    private int nnp_f_fluidlogging_getLightEmission(int original, BlockState state, BlockGetter getter, BlockPos pos) {
        if (ServerConfig.considerFluidLightLevel) {
            AuxiliaryLightManager lManager = getter.getAuxLightManager(pos);
            //noinspection deprecation - widely used in vanilla
            if (lManager != null && !state.liquid()) {
                
                return lManager.getLightAt(pos);
            }
        }
        
        return original;
    }
}
