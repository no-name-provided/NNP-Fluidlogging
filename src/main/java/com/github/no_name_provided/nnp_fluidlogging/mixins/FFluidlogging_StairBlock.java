package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments.FLUID_STATES;

/**
 * Important mixin - this is what stops water from flowing when a slab is logged with a different fluid. It does not
 * currently cause the correct fluid to flow, or stop adjacent fluids from being cooled.
 * <p>
 * We'll need one of these mixins for each SimpleWaterloggedBlock block, since vanilla hardcodes the water reference.
 * </p>
 */
@Mixin(StairBlock.class)
public class FFluidlogging_StairBlock extends Block implements SimpleWaterloggedBlock {
    public FFluidlogging_StairBlock(Properties props) {
        super(props);
    }
    
    /**
     * Force updateShape to trigger a tick with a fluid from our attachment (when present).
     *
     * @param level     The level the block is in.
     * @param pos       The position the block is in.
     * @param fluid     The fluid that should be ticked (usually the fluid contents of the block).
     * @param tickDelay The tick delay (usually fully specified by the fluid).
     */
    @Redirect(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/Fluid;I)V"))
    private void nnp_f_fluidlogging_updateShape(LevelAccessor level, BlockPos pos, Fluid fluid, int tickDelay) {
        Fluid trueFluid = level.getChunk(pos).getData(FLUID_STATES).map().getOrDefault(pos, Fluids.EMPTY.defaultFluidState()).getType();
        level.scheduleTick(pos, trueFluid, trueFluid.getTickDelay(level));
    }
}
