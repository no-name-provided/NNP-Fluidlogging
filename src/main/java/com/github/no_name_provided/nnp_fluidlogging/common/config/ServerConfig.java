package com.github.no_name_provided.nnp_fluidlogging.common.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
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
                            new ArrayList<>(List.of(ServerConfig.supplyFluid())),
                            ServerConfig::supplyFluid,
                            ServerConfig::validateFluid
                    );
    
    public static final ModConfigSpec SPEC = BUILDER.build();
    
    public static List<? extends String> blacklistedFluids;
    
    protected static String supplyFluid() {
        
        //noinspection OptionalGetWithoutIsPresent - We're literally grabbing the water field. It'll be fine (TM).
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
        }
    }
}
