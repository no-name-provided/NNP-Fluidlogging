package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/**
 * Important mixin - tells the game which fluid to render... at least until the block is updated.
 */
@Mixin(SectionCompiler.class)
abstract class FFluidlogging_SectionCompiler {
    @Unique
    private static FluidState NNPFluidlogging$getFluidState(BlockState state, BlockPos pos, RenderChunkRegion region) {
        if (!state.isAir() && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            
            // We mixin to this so it works as expected
            return region.getFluidState(pos);
        } else {
            
            return state.getFluidState();
        }
    }
    
    /**
     * This makes blocks render with the correct fluid.
     * @return The fluid contained in the block, as reported by our modded data structure.
     */
    @ModifyVariable(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At("STORE"),
            name = "fluidstate"
    )
    private FluidState nnp_f_fluidlogging_compile(FluidState vanillaValue, SectionPos pos, RenderChunkRegion region, VertexSorting vertexSorting, SectionBufferBuilderPack pack, List<AddSectionGeometryEvent.AdditionalSectionRenderer> additionalRenderers, @Local(ordinal = 2) BlockPos bPos) {
        BlockState state = region.getBlockState(bPos);
        if (!state.isAir() && state.hasProperty(BlockStateProperties.WATERLOGGED)) {

            return NNPFluidlogging$getFluidState(state, bPos, region);
        } else {
            
            // Required for vanilla LiquidBlock to render correctly
            return state.getFluidState();
        }
    }
}
