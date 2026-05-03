package com.github.no_name_provided.nnp_fluidlogging.common.network.handlers;

import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.AuxLightManagerUpdatePayload;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AuxLightManagerHandler {
    
    /**
     * Updates light levels on client. Assumes packet is for current level. Cleaner solution than manually
     * forcing chunk updates.
     * <p>
     * The thread the supplied handler executes in depends on the {@link HandlerThread} set in
     * {@link PayloadRegistrar#executesOn}.
     *
     * @param payload The data to handle.
     * @param context Any context.
     */
    public static void handle(AuxLightManagerUpdatePayload payload, IPayloadContext context) {
        AuxiliaryLightManager lManager = context.player().level().getAuxLightManager(BlockPos.of(payload.blockPos()));
        if (lManager != null) {
            lManager.setLightAt(BlockPos.of(payload.blockPos()), payload.lightLevel());
        } else {
            LogUtils.getLogger().debug("Failed to update client light level at {}.", BlockPos.of(payload.blockPos()));
            LogUtils.getLogger().debug("Consider setting \"Force Chunk Updates\" to \"True\" as a workaround.");
        }
    }
}
