package com.github.no_name_provided.nnp_fluidlogging.common.network.handlers;

import com.github.no_name_provided.nnp_fluidlogging.common.network.payloads.AuxLightManagerUpdatePayload;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AuxLightManagerHandler {
    private static final Logger logger = LogUtils.getLogger();
    
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
        } else if (logger.isDebugEnabled()) {
            BlockPos pos = BlockPos.of(payload.blockPos());
            logger.debug("Failed to update client light level:\nBlockPos: {}\nBlock: {}\nFluidState: {}\nNew Light Level: {}\nbecause the AuxiliaryLightManager is not currently available for that chunk. This may happen when packets are sent too early in the chunk loading process. \n\n*If* this is causing rendering errors, consider setting \"Force Chunk Updates\" to \"True\" as a workaround.", pos, context.player().level().getBlockState(pos), context.player().level().getFluidState(pos), payload.lightLevel());
        }
    }
}
