package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
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

@Mixin(LevelChunk.class)
public abstract class FFluidlogging_LevelChunk extends ChunkAccess {
    @Final @Shadow
    Level level;
    
    public FFluidlogging_LevelChunk(ChunkPos pos, UpgradeData data, LevelHeightAccessor heightGetter, Registry<Biome> biomeRegistry, long rand, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData) {
        super(pos, data, heightGetter, biomeRegistry, rand, levelChunkSections, blendingData);
    }
    
    
    @Inject(method = "getFluidState(III)Lnet/minecraft/world/level/material/FluidState;",
            at = @At("RETURN"), cancellable = true)
    private void nnp_f_fluidlogging_getFluidState(int x, int y, int z, CallbackInfoReturnable<FluidState> cir) {
        if (cir.getReturnValue().isEmpty()) {
            
            // This appears to be the source of the severe lag. Probably has to do with bypassing the vanilla
            // section-by-section approach, which skips empty sections entirely
            
            //levelchunksection.getFluidState(x & 15, y & 15, z & 15);
            FluidStates states = level.getChunk(x, y).getData(FLUID_STATES);
            
            cir.setReturnValue(states.map().getOrDefault(new BlockPos(x, y, z), level.getBlockState(new BlockPos(x, y, z)).getFluidState()));
        }
    }
}
