package fi.dy.masa.tweakeroo.mixin;

//@Mixin(PresetsScreen.class)
public abstract class MixinPresetsScreen
{
    //@Shadow @Final private static RegistryKey<Biome> BIOME_KEY;
    //@Shadow @Final private CustomizeFlatLevelScreen parent;

    /*
    @Inject(method = "init", at = @At("HEAD"))
    private void tweakeroo_addCustomEntries(CallbackInfo ci)
    {
        // FIXME
        if (FeatureToggle.TWEAK_CUSTOM_FLAT_PRESETS.getBooleanValue())
        {
            **
            int vanillaEntries = 9;
            int toRemove = PRESETS.size() - vanillaEntries;

            if (toRemove > 0)
            {
                PRESETS.subList(0, toRemove).clear();
            }

            List<String> presetStrings = Configs.Lists.FLAT_WORLD_PRESETS.getStrings();

            for (int i = presetStrings.size() - 1; i >= 0; --i)
            {
                String str = presetStrings.get(i);

                if (this.registerPresetFromString(str) && PRESETS.size() > vanillaEntries)
                {
                    Object o = PRESETS.remove(PRESETS.size() - 1);
                    PRESETS.add(0, o);
                }
            }
            **
        }
    }
    */
}
