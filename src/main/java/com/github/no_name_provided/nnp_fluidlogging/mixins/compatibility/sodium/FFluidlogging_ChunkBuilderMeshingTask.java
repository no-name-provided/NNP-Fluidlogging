package com.github.no_name_provided.nnp_fluidlogging.mixins.compatibility.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask")
public class FFluidlogging_ChunkBuilderMeshingTask {

    @ModifyExpressionValue(method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;"))
    private FluidState nnp_f_fluidlogging(FluidState original, @Local(name = "slice") LevelSlice slice, @Local(name = "x") int x, @Local(name = "y") int y, @Local(name = "z") int z) {
        // We mix into this to make it check our attachment
        return slice.getFluidState(new BlockPos(x, y, z));
    }
}
