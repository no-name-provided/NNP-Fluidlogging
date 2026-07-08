package com.github.no_name_provided.nnp_fluidlogging.mixins.compatibility.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer")
public class FFluidlogging_DefaultFluidRenderer {
    
    @ModifyExpressionValue(method = "fluidHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)F",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;"))
    private FluidState nnp_f_fluidlogging_fixFluidState(FluidState fluidState, BlockAndTintGetter world, Fluid fluid, BlockPos blockPos) {
        
        // We mix into this to make it check our attachment
        return world.getFluidState(blockPos);
    }
}
