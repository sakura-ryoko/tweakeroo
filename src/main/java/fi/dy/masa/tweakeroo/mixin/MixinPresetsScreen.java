package fi.dy.masa.tweakeroo.mixin;

//@Mixin(PresetsScreen.class)
public abstract class MixinPresetsScreen
{
    /*
    @Shadow @Final private static RegistryKey<Biome> BIOME_KEY;

    @Shadow @Final private CustomizeFlatLevelScreen parent;

    @Inject(method = "init", at = @At("HEAD"))
    private void addCustomEntries(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_CUSTOM_FLAT_PRESETS.getBooleanValue())
        {
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
        }
    }

    private boolean registerPresetFromString(String str)
    {
        Matcher matcher = MiscUtils.PATTERN_WORLD_PRESET.matcher(str);

        if (matcher.matches())
        {
            // TODO --> I added some code here, and added the IMixinCustomizeFlatLevelScreen
            GeneratorOptionsHolder generatorOptionsHolder = ((IMixinCustomizeFlatLevelScreen) this.parent).tweakeroo_getCreateWorldParent().getWorldCreator().getGeneratorOptionsHolder();
            DynamicRegistryManager.Immutable dynamicRegistryManager = generatorOptionsHolder.getCombinedRegistryManager();
            //FeatureSet featureSet = generatorOptionsHolder.dataConfiguration().enabledFeatures();
            RegistryWrapper.Impl<Biome> biomeLookup = dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.BIOME);
            RegistryWrapper.Impl<StructureSet> structureLookup = dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET);
            RegistryWrapper.Impl<PlacedFeature> featuresLookup = dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.PLACED_FEATURE);
            //RegistryWrapper.Impl<Block> blockLookup = dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.BLOCK).withFeatureFilter(featureSet);
            FlatChunkGeneratorConfig defaultConfig = FlatChunkGeneratorConfig.getDefaultConfig(biomeLookup, structureLookup, featuresLookup);
            //FlatChunkGeneratorConfig currentConfig = this.parent.getConfig();
            Optional<RegistryEntry.Reference<Biome>> optBiomeEntry = Optional.empty();

            String name = matcher.group("name");
            String blocksString = matcher.group("blocks");
            String biomeName = matcher.group("biome");
            // TODO add back the features
            String iconItemName = matcher.group("icon");

            RegistryKey<Biome> biome = null;

            try
            {
                optBiomeEntry = dynamicRegistryManager.get(RegistryKeys.BIOME).getEntry(Identifier.ofVanilla(biomeName));
                biome = optBiomeEntry.flatMap(RegistryEntry.Reference::getKey).orElseThrow();
            }
            catch (Exception ignore) {}

            if (biome == null)
            {
                Tweakeroo.logger.error("Invalid biome while parsing flat world string: '{}'", biomeName);
                return false;
            }

            Item item = null;

            try
            {
                item = Registries.ITEM.get(Identifier.of(iconItemName));
            }
            catch (Exception ignore) {}

            if (item == null)
            {
                Tweakeroo.logger.error("Invalid item for icon while parsing flat world string: '{}'", iconItemName);
                return false;
            }

            List<FlatChunkGeneratorLayer> layers = MiscTweaks.parseBlockString(blocksString);

            if (layers == null)
            {
                Tweakeroo.logger.error("Failed to get the layers for the flat world preset");
                return false;
            }
            FlatChunkGeneratorConfig newConfig = defaultConfig.with(layers, defaultConfig.getStructureOverrides(), optBiomeEntry.orElseThrow());

            //new PresetsScreen.SuperflatPresetsListWidget.SuperflatPresetEntry(null);
            //addPreset(Text.translatable(name), item, biome, ImmutableSet.of(), false, false, layers);

            // TODO --> This might work (Test Me)
            //this.parent.setConfig(newConfig);

            return true;
        }
        else
        {
            Tweakeroo.logger.error("Flat world preset string did not match the regex");
        }

        return false;
    }
    */
}
