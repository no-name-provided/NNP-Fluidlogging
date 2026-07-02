package com.github.no_name_provided.nnp_fluidlogging.common.attachments.sync;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FluidStates;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.ConcurrentHashMap;

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
        if (initialSync) {
            FluidStates.STREAM_CODEC.encode(buf, states);
        } else {
            // Quick hack to encode only the updates
            FluidStates.STREAM_CODEC.encode(buf, new FluidStates(states.unsyncedUpdates(), new ConcurrentHashMap<>()));
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
        if (oldStates == null) {
            return FluidStates.STREAM_CODEC.decode(buf);
        } else {
            // We only send modified entries, so we need to merge the new values with the old
            ConcurrentHashMap<BlockPos, FluidState> updatedEntries = FluidStates.STREAM_CODEC.decode(buf).map();
            // #putAll overrides existing entries, so it's important we merge the new into the
            // old (rather than vice versa)
            oldStates.map().putAll(updatedEntries);
            // Handle fluid removals, which are encoded as Fluids.EMPTY#defaultFluidState
            oldStates.map().values().removeIf(value -> value.getFluidType() == Fluids.EMPTY.getFluidType());
            // (Theoretically) prevent small memory leak - this isn't used on the client,
            // but may be populated by common code
            oldStates.unsyncedUpdates().clear();
            return oldStates;
        }
    }
}