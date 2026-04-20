package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class FFluidlogging_BlockBehavior {
    
//    @Inject(method = "getFluidState()Lnet/minecraft/world/level/material/FluidState;",
//    at = @At("RETURN"), cancellable = true)
//    private void nnp_f_fluidlogging_getFluidState(CallbackInfoReturnable<FluidState> cir) {
//        if (cir.getReturnValue().isEmpty()) {
//
//            cir.setReturnValue();
//        }
//    }
}
