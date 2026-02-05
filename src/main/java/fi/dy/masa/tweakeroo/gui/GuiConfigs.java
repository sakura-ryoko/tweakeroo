package fi.dy.masa.tweakeroo.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IConfigGuiAllTab;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;

public class GuiConfigs extends GuiConfigsBase implements IConfigGuiAllTab
{
    // If you have an add-on mod, you can append stuff to these GUI lists by re-assigning a new list to it.
    // I'd recommend using your own config handler for the config serialization to/from config files.
    // Although the config dirty marking stuff probably is a mess in this old malilib code base for that stuff...
    public static ImmutableList<FeatureToggle> TWEAK_LIST = FeatureToggle.VALUES;
    public static ImmutableList<IHotkeyTogglable> YEET_LIST = Configs.Disable.OPTIONS;

    private static ConfigGuiTab tab = ConfigGuiTab.TWEAKS;

    public GuiConfigs()
    {
        super(10, 50, Reference.MOD_ID, null, "tweakeroo.gui.title.configs", String.format("%s", Reference.MOD_VERSION));
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;

        for (ConfigGuiTab tab : ConfigGuiTab.values())
        {
            if (!this.useAllTab() && tab == ConfigGuiTab.ALL) continue;
            x += this.createButton(x, y, -1, tab);
        }
    }

    private int createButton(int x, int y, int width, ConfigGuiTab tab)
    {
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
        button.setEnabled(GuiConfigs.tab != tab);
        this.addButton(button, new ButtonListener(tab, this));

        return button.getWidth() + 2;
    }

    @Override
    protected int getConfigWidth()
    {
        ConfigGuiTab tab = GuiConfigs.tab;

        if (tab == ConfigGuiTab.GENERIC)
        {
            return 170;
        }
        else if (tab == ConfigGuiTab.FIXES)
        {
            return 60;
        }
        else if (tab == ConfigGuiTab.LISTS)
        {
            return 200;
        }

        return 260;
    }

    @Override
    protected boolean useKeybindSearch()
    {
        return GuiConfigs.tab == ConfigGuiTab.ALL ||
               GuiConfigs.tab == ConfigGuiTab.TWEAKS ||
               GuiConfigs.tab == ConfigGuiTab.GENERIC_HOTKEYS ||
               GuiConfigs.tab == ConfigGuiTab.DISABLES;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs()
    {
        List<? extends IConfigBase> configs;
        ConfigGuiTab tab = GuiConfigs.tab;

        if (tab == ConfigGuiTab.ALL && this.useAllTab())
        {
            return this.getAllConfigs();
        }
        else if (tab == ConfigGuiTab.GENERIC)
        {
            configs = Configs.Generic.OPTIONS;
        }
        else if (tab == ConfigGuiTab.FIXES)
        {
            configs = Configs.Fixes.OPTIONS;
        }
        else if (tab == ConfigGuiTab.LISTS)
        {
            configs = Configs.Lists.OPTIONS;
        }
        else if (tab == ConfigGuiTab.DISABLES)
        {
            return ConfigOptionWrapper.createFor(YEET_LIST);
        }
        else if (tab == ConfigGuiTab.TWEAKS)
        {
            return ConfigOptionWrapper.createFor(TWEAK_LIST.stream().map(this::wrapConfig).toList());
        }
        else if (tab == ConfigGuiTab.GENERIC_HOTKEYS)
        {
            configs = Hotkeys.HOTKEY_LIST;
        }
        else
        {
            return Collections.emptyList();
        }

        return ConfigOptionWrapper.createFor(configs);
    }

    protected BooleanHotkeyGuiWrapper wrapConfig(FeatureToggle config)
    {
        return new BooleanHotkeyGuiWrapper(config.getName(), config, config.getKeybind());
    }

    @Override
    public boolean useAllTab()
    {
        return true;
    }

    @Override
    public List<ConfigOptionWrapper> getAllConfigs()
    {
        List<ConfigOptionWrapper> configs = new ArrayList<>();

        configs.addAll(ConfigOptionWrapper.createFor(Configs.Generic.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(Configs.Fixes.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(Configs.Lists.OPTIONS));
        configs.addAll(ConfigOptionWrapper.createFor(YEET_LIST));
        configs.addAll(ConfigOptionWrapper.createFor(TWEAK_LIST.stream().map(this::wrapConfig).toList()));
        configs.addAll(ConfigOptionWrapper.createFor(Hotkeys.HOTKEY_LIST));

        return configs;
    }

    private record ButtonListener(ConfigGuiTab tab, GuiConfigs parent) implements IButtonActionListener
    {
        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            GuiConfigs.tab = this.tab;
            this.parent.reCreateListWidget(); // apply the new config width
            if (this.parent.getListWidget() != null)
            {
                this.parent.getListWidget().resetScrollbarPosition();
            }
            this.parent.initGui();
        }
    }

    public enum ConfigGuiTab
    {
        ALL             (IConfigGuiAllTab.getTranslationKey()),
        GENERIC         ("tweakeroo.gui.button.config_gui.generic"),
        FIXES           ("tweakeroo.gui.button.config_gui.fixes"),
        LISTS           ("tweakeroo.gui.button.config_gui.lists"),
        TWEAKS          ("tweakeroo.gui.button.config_gui.tweaks"),
        GENERIC_HOTKEYS ("tweakeroo.gui.button.config_gui.generic_hotkeys"),
        DISABLES        ("tweakeroo.gui.button.config_gui.disables");

        private final String translationKey;

        ConfigGuiTab(String translationKey)
        {
            this.translationKey = translationKey;
        }

        public String getDisplayName()
        {
            return StringUtils.translate(this.translationKey);
        }
    }
}
