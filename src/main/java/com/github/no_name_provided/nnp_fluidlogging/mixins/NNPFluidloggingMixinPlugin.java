package com.github.no_name_provided.nnp_fluidlogging.mixins;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

@SuppressWarnings("SameParameterValue") // want to make extending things easy
public final class NNPFluidloggingMixinPlugin implements IMixinConfigPlugin {
    
    //region Useful additions --------------------------------------
    
    private static boolean checkModLoading(String modId) {
        return LoadingModList.get().getModFileById(modId) != null;
    }
    
    /**
     * This is where we filter out compatibility mixins, to avoid crashes when optional mods aren't present.
     *
     * @param targetClassName Fully qualified class name of the target class
     * @param mixinClassName  Fully qualified class name of the mixin
     * @return True to allow the mixin to be applied, or false to remove it from target's mixin set
     */
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (targetClassName.contains("net.caffeinemc.mods.sodium")) {
            
            return checkModLoading("sodium");
        } else {
            
            return true;
        }
    }
    
    //endregion --------------------------------------
    
    //region Mandatory overrides (that shouldn't be) --------------------------------------
    
    /**
     * Called after the plugin is instantiated, do any setup here.
     *
     * @param mixinPackage The mixin root package from the config
     */
    @Override
    public void onLoad(String mixinPackage) {
    
    }
    
    /**
     * Called only if the "referenceMap" key in the config is <b>not</b> set. This allows the refmap file name to be
     * supplied by the plugin programmatically if desired. Returning <code>null</code> will revert to the default
     * behavior of using the default refmap JSON file.
     *
     * @return Path to the refmap resource or null to revert to the default
     */
    @Override
    public String getRefMapperConfig() {
        return null;
    }
    
    /**
     * Called after all configurations are initialized, this allows this plugin to observe classes targeted by other
     * mixin configs and optionally remove targets from its own set. The set myTargets is a direct view of the targets
     * collection in this companion config and keys may be removed from this set to suppress mixins in this config which
     * target the specified class. Adding keys to the set will have no effect.
     *
     * @param myTargets    Target class set from the companion config
     * @param otherTargets Target class set incorporating targets from all other configs, read-only
     */
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    
    }
    
    /**
     * After mixins specified in the configuration have been processed, this method is called to allow the plugin to add
     * any additional mixins to load. It should return a list of mixin class names or return null if the plugin does not
     * wish to append any mixins of its own.
     *
     * @return additional mixins to apply
     */
    @Override public List<String> getMixins() {
        return List.of();
    }
    
    /**
     * Called immediately <b>before</b> a mixin is applied to a target class, allows any pre-application transformations
     * to be applied.
     *
     * @param targetClassName Transformed name of the target class
     * @param targetClass     Target class tree
     * @param mixinClassName  Name of the mixin class
     * @param mixinInfo       Information about this mixin
     */
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    
    }
    
    /**
     * Called immediately <b>after</b> a mixin is applied to a target class, allows any post-application transformations
     * to be applied.
     *
     * @param targetClassName Transformed name of the target class
     * @param targetClass     Target class tree
     * @param mixinClassName  Name of the mixin class
     * @param mixinInfo       Information about this mixin
     */
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    
    }
    
    //endregion  --------------------------------------
}