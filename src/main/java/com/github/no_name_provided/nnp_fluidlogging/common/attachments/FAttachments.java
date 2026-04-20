package com.github.no_name_provided.nnp_fluidlogging.common.attachments;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;

import static com.github.no_name_provided.nnp_fluidlogging.nnp_fluidlogging.MODID;

public class FAttachments {
    public static DeferredRegister<AttachmentType<?>> FATTACHMENTS = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES,
            MODID
    );
    
    public static DeferredHolder<AttachmentType<?>, AttachmentType<FluidStates>> FLUID_STATES = FATTACHMENTS.register(
            "fluid_states", () -> AttachmentType.builder(() -> new FluidStates(new HashMap<>()))
                    .sync(FluidStates.STREAM_CODEC)
                    .serialize(FluidStates.CODEC).build()
    );
    
    public static void register(IEventBus modBus) {
        FATTACHMENTS.register(modBus);
    }
}
