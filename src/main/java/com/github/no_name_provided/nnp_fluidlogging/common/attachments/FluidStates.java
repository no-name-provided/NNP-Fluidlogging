package com.github.no_name_provided.nnp_fluidlogging.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.FluidState;

import java.util.HashMap;

public record FluidStates(HashMap<BlockPos, FluidState> map) {
    
    public static Codec<FluidStates> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.unboundedMap(
                                    // Unbound map keys must begin with strings (or things built on them)
                                    Codec.STRING
                                            .xmap(Long::parseLong, String::valueOf)
                                            .xmap(BlockPos::of, BlockPos::asLong),
                                    FluidState.CODEC
                                    // Workaround for overzealous type validation; should probably find a more efficient solution
                            ).xmap(HashMap::new, HashMap::new)
                            .fieldOf("map").forGetter(FluidStates::map)
            ).apply(inst, FluidStates::new)
    );
    
    public static StreamCodec<RegistryFriendlyByteBuf, FluidStates> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
