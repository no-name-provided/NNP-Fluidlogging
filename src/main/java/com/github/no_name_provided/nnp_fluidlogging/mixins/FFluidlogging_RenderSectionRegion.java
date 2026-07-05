package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCopy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

/**
 * Important mixin - Does most of the heavy lifting of making visual fluidstate checks prefer our data structure. Some
 * other mixins ultimately boil down to replacing hardcoded BlockState#getFluidState with calls to this.
 */
@Mixin(RenderSectionRegion.class)
abstract class FFluidlogging_RenderSectionRegion implements BlockAndTintGetter {
    @Mutable @Shadow @Final
    public final int minSectionY;
    
    FFluidlogging_RenderSectionRegion(int minSectionY) {
        this.minSectionY = minSectionY;
    }
    
    @Shadow @Final
    abstract public SectionCopy getSection(int sectionX, int sectionY, int sectionZ);
    
    @Unique
    LevelChunk NNPFluidlogging$getContainingChunk(ChunkPos pos) {
        
        return getSection(pos.x(), this.minSectionY, pos.z()).wrapped;
    }
    
    
    /**
     * Does most of the heavy lifting of making visual fluidstate checks prefer our attachment over the cached,
     * hardcoded blockstate default.
     * <p>
     * Heavily borrows from vanilla.
     * </p>
     *
     * @param pos Location of the block space in the level.
     * @param cir Wrapper for return value. Triggers early return when used.
     */
    @Inject(method = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_getFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        LevelChunk chunk = this.NNPFluidlogging$getContainingChunk(ChunkPos.containing(pos));
        cir.setReturnValue(chunk.getData(FLUID_STATES).getOrDefault(pos, chunk.getBlockState(pos).getFluidState()));
    }
}
