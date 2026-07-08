package com.github.no_name_provided.nnp_fluidlogging.mixins.compatibility.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.LevelSlice")
public class FFluidlogging_LevelSlice {
    
    @Shadow @Final
    private ClientLevel level;
    
    @WrapMethod(method = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;")
    private FluidState getFluidState(BlockPos pos, Operation<FluidState> original) {
        ChunkPos chunkPos = new ChunkPos(pos);
        // We need to make sure chunks are loaded, since Sodium apparently renders some
        // without retreiving them from the server
        if (level.hasChunk(chunkPos.x, chunkPos.z)) {
            
            return level.getFluidState(pos);
        } else {
            
            return original.call(pos);
        }
    }
}
