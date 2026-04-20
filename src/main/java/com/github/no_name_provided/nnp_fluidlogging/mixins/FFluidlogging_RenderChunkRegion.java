package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

@Mixin(RenderChunkRegion.class)
abstract class FFluidlogging_RenderChunkRegion implements BlockAndTintGetter {
    
    @Shadow
    protected abstract RenderChunk getChunk(int p_350795_, int p_350558_);
    
    @Inject(method = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;",
    at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_getFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        RenderChunk chunk = this.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        cir.setReturnValue(chunk.wrapped.getData(FLUID_STATES).map().getOrDefault(pos, chunk.getBlockState(pos).getFluidState()));
    }
}
