package com.github.no_name_provided.nnp_fluidlogging.common.helpers;

import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.AuxLightManagerUpdatePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MiscHelpers {
    
    /**
     * (Failed) attempt to prevent our attachments from syncing during worldgen/too early in loading. Consider
     * removing.
     */
    public static <T> void safeSyncChunkAttachment(ChunkAccess chunk, DeferredHolder<AttachmentType<?>, AttachmentType<T>> type) {
        if (
                chunk instanceof LevelChunk levelChunk &&
                        levelChunk.getLevel().getServer() instanceof MinecraftServer server && server.isSameThread()
        ) {
            levelChunk.syncData(type);
        }
    }
    
    public static void updateClientLightLevels(BlockPos pos, int lightLevel, ServerLevel sLevel) {
        ChunkPos chunkPos = new ChunkPos(pos);
        sLevel.getPlayers(player ->
                player.level().equals(sLevel) &&
                        sLevel.getChunkSource().chunkMap.isChunkTracked(player, chunkPos.x, chunkPos.z)
        ).forEach(player ->
                player.connection.send(new AuxLightManagerUpdatePayload(
                        lightLevel,
                        pos.asLong()))
        );
    }
}
