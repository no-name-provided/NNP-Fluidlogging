package com.github.no_name_provided.nnp_fluidlogging;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(nnp_fluidlogging.MODID)
public class nnp_fluidlogging {
    // Main mod ID
    public static final String MODID = "nnp_fluidlogging";
    
    public nnp_fluidlogging(IEventBus modEventBus, ModContainer ignoredModContainer) {
        // Add all our deferred registries to the mod registration queue
        FAttachments.register(modEventBus);
    }
}
