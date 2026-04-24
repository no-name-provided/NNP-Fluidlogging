package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public class FFluidlogging_LeavesBlock {
    
    @Inject(method = "getFluidState(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At("RETURN"), cancellable = true)
    private void nnp_f_fluidlogging_getFluidState(BlockState state, CallbackInfoReturnable<FluidState> cir) {
        // The return value form this directly controls almost everything that depends on fluid contents
        // (including visuals and ticking). Unfortunately, it isn't called with positional context,
        // so we need to look upstream.
        
        // We're leaving this here so we can "toggle it on" and compare differences in behavior. Makes it
        // easier to see what we missed.
        
//        cir.setReturnValue(Fluids.LAVA.defaultFluidState());
    }
}
