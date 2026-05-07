package com.github.no_name_provided.nnp_fluidlogging.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Jailbreaks the vanilla bucket class, making it usable for generalized fluidlogging.
 */
@Mixin(BucketItem.class)
abstract class FFluidlogging_BucketItem extends Item implements DispensibleContainerItem {
    public FFluidlogging_BucketItem(Properties properties, Fluid content) {
        super(properties);
        this.content = content;
    }
    
    @Mutable @Final @Shadow
    public final Fluid content;
    
    @Shadow @Final
    protected abstract boolean canBlockContainFluid(@Nullable Player player, Level worldIn, BlockPos posIn, BlockState blockstate);
    
    /**
     * Removes a random hardcoded water check added in 26.1, allowing vanilla buckets to continue placing nonwater
     * fluids.
     *
     * @param original           The original return value.
     * @param level              The level being interacted with.
     * @param player             The player doing the interacting.
     * @param pos                THe targeted position.
     * @param clicked            The targeted BlockState.
     * @param directionOffsetPos The position that will be interacted with if the targeted position can't hold fluid.
     * @return The block position where the fluid should be placed. This should be the targeted BlockPos (default) or
     * the targeted-face-adjacent empty BlockPos (fallback).
     */
    @ModifyVariable(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At("STORE"),
            name = "placePos")
    private BlockPos nnp_f_fluidlogging_use_removeHardcodedCheckForWater(BlockPos original, Level level, Player player, @Local(name = "pos") BlockPos pos, @Local(name = "clicked") BlockState clicked, @Local(name = "directionOffsetPos") BlockPos directionOffsetPos) {
        
        return canBlockContainFluid(player, level, pos, clicked) ? pos : directionOffsetPos;
    }
}
