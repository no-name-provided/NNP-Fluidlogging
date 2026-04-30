package com.github.no_name_provided.nnp_fluidlogging;

import com.github.no_name_provided.nnp_fluidlogging.common.attachments.FAttachments;
import com.github.no_name_provided.nnp_fluidlogging.common.config.ServerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NNP_Fluidlogging.MODID)
public class NNP_Fluidlogging {
    // Main mod ID
    public static final String MODID = "nnp_fluidlogging";
    
    /**
     * Mod entry point. Some initialization can be done here. Registration <i>is</i> done here.
     * @param modEventBus The event bus associated with this mod.
     * @param modContainer The FML container associated with this mod.
     */
    public NNP_Fluidlogging(IEventBus modEventBus, ModContainer modContainer) {
        // Add all our deferred registries to the mod registration queue
        FAttachments.register(modEventBus);
        // Register our configs.
        // The file itself is registered on both sides
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        // But the screen is only available on clients.
        // Neo is stupid about this, so we need to manually wrap the call in a conditionally loaded class.
        // Alternatively, consider using separate entry points
        if (FMLEnvironment.dist.isClient()) {
            ServerConfig.registerExtensionPoint(modContainer);
        }
    }
}
