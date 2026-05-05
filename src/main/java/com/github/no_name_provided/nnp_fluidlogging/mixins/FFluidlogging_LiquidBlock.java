package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

@Mixin(LiquidBlock.class)
abstract class FFluidlogging_LiquidBlock extends Block implements BucketPickup {
    @Shadow @Final public FlowingFluid fluid;
    
    private FFluidlogging_LiquidBlock(Properties props) {
        super(props);
    }
    
    /**
     * Probably not be necessary, or even counterproductive, since LiquidBlock can't be fluidlogged. Attempt to fix
     * fluid interactions.
     */
    @Inject(method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
            at = @At("HEAD"), cancellable = true)
    private void nnp_f_fluidlogging_randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource, CallbackInfo ci) {
        level.getFluidState(pos).randomTick(level, pos, randomSource);
        
        ci.cancel();
    }
    
    /**
     * May not be necessary. Attempt to prevent incorrect fluid interactions.
     */
    @Redirect(method = "onPlace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
    private void nnp_f_fluidlogging_onPlace_fixScheduleTick(Level level, BlockPos pos, Fluid fluid, int tickDelay) {
        FluidState state = level.getChunk(pos).getData(FLUID_STATES).map().getOrDefault(pos, fluid.defaultFluidState());
        level.scheduleTick(pos, state.getType(), state.getType().getTickDelay(level));
    }
}
