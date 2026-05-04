package com.github.no_name_provided.nnp_fluidlogging.common.wrappers;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * These methods will crash if called from a server context. They will not crash if a class which <i>could</i>
 * call them loads. These methods can be referenced in classes loaded on a dedicated server, so long aas they
 * aren't reachable. That isn't true for direct references to the client only classes they wrap. Use these
 * wrappers (rather than the methods they wrap) in common code.
 */
public class ClientClassWrappers {
    /**
     * Allows blocks to be marked for (client) updates in common code. Will crash if called on a server.
     */
    public static void setDirtyFromSharedCode(LevelAccessor level, BlockPos pos, BlockState newState, BlockState oldState) {
        if (level instanceof ClientLevel clientLevel) {
            clientLevel.setBlocksDirty(pos, newState, oldState);
        }
    }
}
