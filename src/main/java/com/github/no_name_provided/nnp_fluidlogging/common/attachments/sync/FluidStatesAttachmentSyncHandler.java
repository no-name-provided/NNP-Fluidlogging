package com.github.no_name_provided.nnp_fluidlogging.common.attachments.sync;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;

/**
 * Tells NeoForge's built-in attachment synchronization to only send those map entries that actually changed.
 */
@ParametersAreNonnullByDefault @MethodsReturnNonnullByDefault
public class FluidStatesAttachmentSyncHandler implements AttachmentSyncHandler<FluidStates> {
    
    /**
     * Writes attachment data to a buffer.
     *
     * <p>If {@code initialSync} is {@code true},
     * the data should be written in full because the client does not have any previous data.
     *
     * <p>If {@code initialSync} is {@code false},
     * the client already received a previous version of the data. In this case, this method is only called once for the
     * attachment, and the resulting data is broadcast to all relevant players.
     *
     * <p>If nothing is written to the buffer, nothing is sent to the client at all,
     * and {@link #read} will not be called on the client side.
     *
     * @param buf         The buffer we're populating.
     * @param states      The FluidStates on the server.
     * @param initialSync Whether this is the first time we synced our attachment (since client connect).
     */
    @Override
    public void write(RegistryFriendlyByteBuf buf, FluidStates states, boolean initialSync) {
        // Use a defensive copy so concurrent mutation can't corrupt the buffer encoding
        HashMap<BlockPos, FluidState> snapshot = initialSync ? new HashMap<>(states.map()) : new HashMap<>(states.unsyncedUpdates());
        if (initialSync) {
            // Sending empty attachments is pointless
            FluidStates.SAFE_STREAM_CODEC_FOR_UPDATES.encode(buf, snapshot);
            
            // Only send a packet if there were changes
        } else if (!snapshot.isEmpty()) {
            // Encode only the updates
            FluidStates.SAFE_STREAM_CODEC_FOR_UPDATES.encode(buf, snapshot);
            // This method is only called once, and the results cached, so we don't need to worry about mutating
            // our attachment here. The map of updates isn't saved on unload, so setting chunks dirty
            // isn't an issue, either
            //
            // TODO: make sure there aren't threading/concurrency issues
            states.unsyncedUpdates().clear();
        }
    }
    
    /**
     * Reads data from buffer (on the client side) and returns the updated attachment.
     *
     * @param holder    The attachment holder, can be cast to chunk since that's the only place we use this.
     * @param buf       The buffer we're reading from.
     * @param oldStates The previous value of the attachment, or {@code null} if there was no previous value.
     * @return The new value of the attachment, or {@code null} if the attachment should be removed.
     */
    @Override
    public @Nullable FluidStates read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable FluidStates oldStates) {
        // For some inexplicable reason, Neo is sending us malformed packets (packets without data, despite being
        // successfully encoded with, at the very least, a var_int). Might be related to not writing anything to
        // buffer when no update is needed, which is supposed to prevent this method from being called
        // (per the docstring, as no update packet is necessary), but I really don't know. They _seem_ to filter out
        // empty updates in C:/Users/chaz/Java Programs/Minecraft Mods/NNP Fluidlogging/build/moddev/artifacts/neoforge-21.1.235-merged.jar!/net/neoforged/neoforge/attachment/AttachmentSync.java:174
        if (buf.readableBytes() == 0) {
            
            // No change on updates, no attachment on initial send. If this is just Neo sending packets I
            // haven't written to, that's the correct behavior...
            return oldStates;
        }
        
        if (oldStates == null) {
            FluidStates newStates = new FluidStates(FluidStates.SAFE_STREAM_CODEC_FOR_UPDATES.decode(buf), new HashMap<>());
            
            // Avoid setting empty attachments on every chunk. I'm not sure
            // this is good for performance, but it's worth trying
            return newStates.map().isEmpty() ? null : newStates;
        } else {
            // We only send modified entries, so we need to merge the new values with the old
            HashMap<BlockPos, FluidState> updatedEntries = FluidStates.SAFE_STREAM_CODEC_FOR_UPDATES.decode(buf);
            // #putAll overrides existing entries, so it's important we merge the new into the
            // old (rather than vice versa)
            oldStates.map().putAll(updatedEntries);
            // Handle fluid removals, which are encoded as Fluids.EMPTY#defaultFluidState
            oldStates.map().values().removeIf(value -> value.getFluidType() == Fluids.EMPTY.getFluidType());
            // (Theoretically) prevent small memory leak - this isn't used on the client,
            // but may be populated by common code
            oldStates.unsyncedUpdates().clear();
            
            // Remove redundant attachments
            return oldStates.map().isEmpty() ? null : oldStates;
        }
    }
    
    /**
     * Workaround for a bug where attachments try to sync too early during worldgen May be related to
     * <a href="https://mojira.dev/MC-299444">MC-299444</a>. #BlameTheNeoForgedTeam
     */
    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
//        return false;
        
        if (holder instanceof LevelChunk chunk) {
            ChunkPos pos = chunk.getPos();
            
            return chunk.getFullStatus() != FullChunkStatus.INACCESSIBLE &&
                    chunk.getPersistedStatus() == ChunkStatus.FULL &&
                    to.level() == chunk.getLevel() &&
                    // Interestingly, this is not enough to ensure the chunk is actually being tracked by the client...
                    ((ServerLevel) chunk.getLevel()).getChunkSource().chunkMap.isChunkTracked(to, pos.x, pos.z);
//                    chunk.isInLevel() &&
//                    to.getChunkTrackingView().isInViewDistance(chunk.getPos().x, chunk.getPos().z);
        } else {
            
            return false;
        }

//        ChunkAccess chunk = (ChunkAccess) holder;
//        return chunk.getLevel() instanceof ServerLevel level &&
//                level.getChunkSource().chunkMap.isChunkTracked(to, chunk.getPos().x, chunk.getPos().z) &&
//                chunk instanceof LevelChunk levelChunk && levelChunk.isInLevel();
//        return ((ChunkAccess) holder).getPersistedStatus() == ChunkStatus.FULL;
    }
}
