package com.github.no_name_provided.nnp_fluidlogging.common.network;

import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.AuxLightManagerUpdatePayload;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Collects, documents, and unifies common network tasks.
 */
@ParametersAreNonnullByDefault @MethodsReturnNonnullByDefault
public class FNetworkHelper {
    
    /**
     * Clears the cached AuxiliaryLightManager value on both sides, if called from server. Otherwise, only updates
     * client.
     *
     * @param pos   The BlockPos having its light level altered.
     * @param level The level containing that BlockPos.
     */
    public static void clearLightAtPos(BlockPos pos, LevelAccessor level) {
        if (level.getAuxLightManager(pos) instanceof AuxiliaryLightManager lManager) {
            lManager.removeLightAt(pos);
            if (!level.isClientSide()) {
                clearClientLightAtPos(pos, level);
            }
        }
    }
    
    /**
     * Updates the cached AuxiliaryLightManager value on both sides, if called from server. Otherwise, only updates
     * client.
     *
     * @param pos   The BlockPos having its light level altered.
     * @param value The light at that position.
     * @param level The level containing that BlockPos.
     */
    public static void setLightAtPos(BlockPos pos, int value, Level level) {
        if (level.getAuxLightManager(pos) instanceof AuxiliaryLightManager lManager) {
            lManager.setLightAt(pos, value);
            if (!level.isClientSide()) {
                updateClientLightAtPos(pos, value, level);
            }
        }
    }
    
    /**
     * Synchronizes the light level at a given position. Server only.
     *
     * @param pos    The BlockPos having its light level synced.
     * @param sLevel The level containing that BlockPos.
     */
    public static void syncLightAtPos(BlockPos pos, ServerLevel sLevel, AuxiliaryLightManager lManager) {
        int value = lManager.getLightAt(pos);
        sLevel.players().forEach(sPlayer ->
                sPlayer.connection.send(new AuxLightManagerUpdatePayload(value, pos.asLong()))
        );
    }
    
    /**
     * Updates the cached AuxiliaryLightManager value on the client. Can be called from a server context if a
     * ServerLevel is provided.
     *
     * @param pos   The BlockPos having its light level altered.
     * @param value The light at that position.
     * @param level The level containing that BlockPos.
     */
    public static void updateClientLightAtPos(BlockPos pos, int value, LevelAccessor level) {
        if (level instanceof ServerLevel sLevel) {
            sLevel.players().forEach(sPlayer ->
                    sPlayer.connection.send(new AuxLightManagerUpdatePayload(value, pos.immutable().asLong()))
            );
        } else if (level.isClientSide() && level.getAuxLightManager(pos.immutable()) instanceof AuxiliaryLightManager lManager) {
            lManager.setLightAt(pos, value);
        }
    }
    
    /**
     * Clears the cached value stored in the Auxiliary light manager on the client.
     *
     * @param pos   The BlockPos having its light level altered.
     * @param level The level containing that BlockPos.
     */
    public static void clearClientLightAtPos(BlockPos pos, LevelAccessor level) {
        // Setting the light level to 0 is the same as calling AuxiliaryLightManager#removeLightAt
        updateClientLightAtPos(pos, 0, level);
    }
}
