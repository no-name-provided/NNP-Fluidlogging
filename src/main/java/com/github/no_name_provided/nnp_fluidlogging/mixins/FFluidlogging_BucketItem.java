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
    
    @ModifyVariable(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
    at = @At("STORE"),
    name = "placePos")
    private BlockPos nnp_f_fluidlogging_use_removeHardcodedCheckForWater(BlockPos original, Level level, Player player, @Local(name = "pos") BlockPos pos, @Local(name = "clicked") BlockState clicked, @Local(name = "directionOffsetPos") BlockPos directionOffsetPos) {
        
        return canBlockContainFluid(player, level, pos, clicked) ? pos : directionOffsetPos;
    }
}
