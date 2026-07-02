package com.github.no_name_provided.nnp_fluidlogging.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A map from BlockPos to FluidState. Not prepopulated - a position without a fluidlogged block is a missing entry. Not
 * intended for waterlogged blocks.
 * <p>
 * This is stored in a record because it will be used for attachments, and those must be "immutable".
 * </p>
 */
public record FluidStates(ConcurrentHashMap<BlockPos, FluidState> map,
                          ConcurrentHashMap<BlockPos, FluidState> unsyncedUpdates) {
    
    public static Codec<FluidStates> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.unboundedMap(
                                    // Unbound map keys must begin with strings (or things built on them)
                                    Codec.STRING
                                            .xmap(Long::parseLong, String::valueOf)
                                            .xmap(BlockPos::of, BlockPos::asLong),
                                    FluidState.CODEC
                                    // Workaround for overzealous type validation; should probably find a more efficient solution
                            ).xmap(ConcurrentHashMap::new, ConcurrentHashMap::new)
                            .fieldOf("map").forGetter(FluidStates::map)
            ).apply(inst, instance -> new FluidStates(instance, new ConcurrentHashMap<>()))
    );
    
    public static StreamCodec<RegistryFriendlyByteBuf, FluidStates> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
    
    /**
     * Wrapper for Map#put that updates our map of unsynced updates.
     *
     * @param pos   The position with an updated FluidState.
     * @param state The old FluidState at that position (or null, if there was none).
     * @return The new FluidState
     */
    public @Nullable FluidState put(BlockPos pos, FluidState state) {
        map().put(pos, state);
        return unsyncedUpdates().put(pos, state);
    }
    
    /**
     * Wrapper for Map#put that updates our map of unsynced updates.
     *
     * @param changedEntries Map of entries that have been changed.
     */
    public void putAll(Map<BlockPos, FluidState> changedEntries) {
        map().putAll(changedEntries);
        unsyncedUpdates().putAll(changedEntries);
    }
    
    /**
     * Wrapper for internal map method of same name.
     */
    public @Nullable FluidState get(BlockPos pos, FluidState defaultState) {
        return map().get(pos);
    }
    
    /**
     * Wrapper for internal map method of same name.
     */
    public FluidState getOrDefault(BlockPos pos, FluidState defaultState) {
        return map().getOrDefault(pos, defaultState);
    }
    
    /**
     * Wrapper for internal map method of same name. Adds adds empty entry in update map.
     *
     * @return The value that was removed, or null if not present.
     */
    public @Nullable FluidState remove(BlockPos pos) {
        unsyncedUpdates().put(pos, Fluids.EMPTY.defaultFluidState());
        return map().remove(pos);
    }
}
