package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

/**
 * Important mixin - Does most of the heavy lifting of making fluidstate checks prefer our data structure. Some other
 * mixins ultimately boil down to replacing hardcoded BlockState#getFluidState with calls to this.
 */
@Mixin(LevelChunk.class)
abstract class FFluidlogging_LevelChunk extends ChunkAccess {
    @Final @Shadow
    Level level;
    
    private FFluidlogging_LevelChunk(ChunkPos pos, UpgradeData data, LevelHeightAccessor heightGetter, Registry<Biome> biomeRegistry, long rand, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData) {
        super(pos, data, heightGetter, biomeRegistry, rand, levelChunkSections, blendingData);
    }
    
    /**
     * Does most of the heavy lifting of making functional fluidstate checks prefer our attachment over the cached,
     * hardcoded blockstate default.
     * <p>
     * Heavily borrows from vanilla.
     * </p>
     *
     * @param cir Wrapper for return value. Triggers early return when used.
     */
    @Inject(method = "getFluidState(III)Lnet/minecraft/world/level/material/FluidState;",
            at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_getFluidState(int x, int y, int z, CallbackInfoReturnable<FluidState> cir) {
        // This may be a significant source of lag, since we're bypassing the vanilla
        // section-by-section approach, which skips empty sections entirely. However,
        // the majority of the lag was fixed by using correct (section) coordinates
        
        // We have to use section coordinates here, or we'll quietly grab the wrong attachment
        FluidStates states = level.getChunk(
                SectionPos.blockToSectionCoord(x),
                SectionPos.blockToSectionCoord(z)
        ).getData(FLUID_STATES);
        
        // Might have a recursion issue somewhere with this default value
        cir.setReturnValue(states.map().getOrDefault(new BlockPos(x, y, z), level.getBlockState(new BlockPos(x, y, z)).getFluidState()));
    }
}
