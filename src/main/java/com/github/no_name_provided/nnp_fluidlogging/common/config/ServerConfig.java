package com.github.no_name_provided.nnp_fluidlogging.common.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

import static com.github.no_name_provided.nnp_fluidlogging.NNP_Fluidlogging.MODID;

@EventBusSubscriber(modid = MODID)
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_FLUIDS =
            BUILDER.comment("Which fluids should be blacklisted? This isn't retroactive.",
                            "Empty by default. Water cannot be blacklisted.")
                    .defineListAllowEmpty(
                            "blacklist.nnp_fluidlogging",
                            new ArrayList<>(),
                            ServerConfig::supplyFluid,
                            ServerConfig::validateFluid
                    );
    private static final ModConfigSpec.BooleanValue CONSIDER_FLUID_LIGHT_LEVEL =
            BUILDER.comment("Should fluidlogged blocks emit the higher of blocklight and fluid light?",
                            "Significant performance penalty.")
                    .define("consider_fluid_light." + MODID, true);
    private static final ModConfigSpec.BooleanValue FLOWING_FLUIDS_CAN_LOG =
            BUILDER.comment("Can partial fluid blocks waterlog (WIP)")
                    .define("flowing_fluids_log." + MODID, false);
    private static final ModConfigSpec.BooleanValue FORCE_CHUNK_UPDATES =
            BUILDER.comment("Should we force chunk updates (resolves sync issues, but may cause stability problems)")
                    .define("force_chunk_updates." + MODID, false);
    
    public static final ModConfigSpec SPEC = BUILDER.build();
    
    public static List<? extends String> blacklistedFluids;
    public static boolean considerFluidLightLevel;
    public static boolean flowingFluidsCanLog;
    public static boolean forceChunkUpdates;
    
    protected static String supplyFluid() {
        
        //noinspection OptionalGetWithoutIsPresent - We're literally grabbing statically initialized fields. It'll be fine (TM).
        return BuiltInRegistries.FLUID.getResourceKey(Fluids.LAVA).get().location().toString();
    }
    
    protected static boolean validateFluid(Object element) {
        if (element instanceof String fluidString) {
            // Returns null on failure
            ResourceLocation loc = ResourceLocation.tryParse(fluidString);
            
            return loc != null && BuiltInRegistries.FLUID.containsKey(loc);
        }
        
        return false;
    }
    
    @SubscribeEvent
    static void onConfigUpdate(final ModConfigEvent event) {
        if (!(event instanceof ModConfigEvent.Unloading) && event.getConfig().getType() == ModConfig.Type.SERVER) {
            blacklistedFluids = BLACKLISTED_FLUIDS.get();
            considerFluidLightLevel = CONSIDER_FLUID_LIGHT_LEVEL.get();
            flowingFluidsCanLog = FLOWING_FLUIDS_CAN_LOG.get();
            forceChunkUpdates = FORCE_CHUNK_UPDATES.get();
        }
    }
    
    public static void registerExtensionPoint(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
