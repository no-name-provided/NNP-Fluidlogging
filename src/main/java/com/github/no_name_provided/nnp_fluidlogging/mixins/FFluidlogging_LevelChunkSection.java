package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunkSection.class)
public class FFluidlogging_LevelChunkSection {
    
//    @Inject(method = "getFluidState(III)Lnet/minecraft/world/level/material/FluidState;", at = @At("RETURN"))
//    private void nnp_f_fluidlogging_pickupBlock(int x, int y, int z, CallbackInfoReturnable<FluidState> cir) {
//        if (cir.getReturnValue().is(Fluids.EMPTY)) {
//
//            // pass
//        }
//    }
}
