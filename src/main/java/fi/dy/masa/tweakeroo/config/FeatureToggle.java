package fi.dy.masa.tweakeroo.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigNotifiable;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;

public enum FeatureToggle implements IHotkeyTogglable, IConfigNotifiable<IConfigBoolean>
{
    TWEAK_ACCURATE_BLOCK_PLACEMENT  ("tweakAccurateBlockPlacement",         false, ""),
    TWEAK_AFTER_CLICKER             ("tweakAfterClicker",                   false, "",    KeybindSettings.INGAME_BOTH),
    TWEAK_AIM_LOCK                  ("tweakAimLock",                        false, ""),
    TWEAK_ANGEL_BLOCK               ("tweakAngelBlock",                     false, ""),
    TWEAK_AUTO_SWITCH_ELYTRA        ("tweakAutoSwitchElytra",               false, ""),
    TWEAK_BLOCK_REACH_OVERRIDE      ("tweakBlockReachOverride",             false, true,  ""),
    TWEAK_BLOCK_TYPE_BREAK_RESTRICTION("tweakBlockTypeBreakRestriction",    false, ""),
    TWEAK_BREAKING_GRID             ("tweakBreakingGrid",                   false, "",    KeybindSettings.INGAME_BOTH),
    TWEAK_BREAKING_RESTRICTION      ("tweakBreakingRestriction",            false, ""),
    TWEAK_CHAT_BACKGROUND_COLOR     ("tweakChatBackgroundColor",            false, ""),
    TWEAK_CHAT_PERSISTENT_TEXT      ("tweakChatPersistentText",             false, ""),
    TWEAK_CHAT_TIMESTAMP            ("tweakChatTimestamp",                  false, ""),
    TWEAK_COMMAND_BLOCK_EXTRA_FIELDS("tweakCommandBlockExtraFields",        false, ""),
    // TODO 1.19.3+
    //TWEAK_CREATIVE_EXTRA_ITEMS      ("tweakCreativeExtraItems",             false, ""),
    // TODO/FIXME 1.19+ the mixin needs an access widener now
    //TWEAK_CUSTOM_FLAT_PRESETS       ("tweakCustomFlatPresets",              false, ""),
    TWEAK_CUSTOM_FLY_DECELERATION   ("tweakCustomFlyDeceleration",          false, ""),
    TWEAK_CUSTOM_INVENTORY_GUI_SCALE("tweakCustomInventoryScreenScale",     false, ""),
    TWEAK_ELYTRA_CAMERA             ("tweakElytraCamera",                   false, ""),
    TWEAK_ENTITY_REACH_OVERRIDE      ("tweakEntityReachOverride",           false, true, ""),
    TWEAK_ENTITY_TYPE_ATTACK_RESTRICTION("tweakEntityTypeAttackRestriction",false, ""),
    TWEAK_SHULKERBOX_STACKING       ("tweakEmptyShulkerBoxesStack",         false, true, ""),
    TWEAK_EXPLOSION_REDUCED_PARTICLES ("tweakExplosionReducedParticles",    false, ""),
    TWEAK_F3_CURSOR                 ("tweakF3Cursor",                       false, ""),
    TWEAK_FAKE_SNEAKING             ("tweakFakeSneaking",                   false, ""),
    TWEAK_FAKE_SNEAK_PLACEMENT      ("tweakFakeSneakPlacement",             false, ""),
    TWEAK_FAST_BLOCK_PLACEMENT      ("tweakFastBlockPlacement",             false, ""),
    TWEAK_FAST_LEFT_CLICK           ("tweakFastLeftClick",                  false, ""),
    TWEAK_FAST_RIGHT_CLICK          ("tweakFastRightClick",                 false, ""),
    TWEAK_FILL_CLONE_LIMIT          ("tweakFillCloneLimit",                 false, true, ""),
    TWEAK_FLY_SPEED                 ("tweakFlySpeed",                       false, "",    KeybindSettings.INGAME_BOTH),
    TWEAK_FLEXIBLE_BLOCK_PLACEMENT  ("tweakFlexibleBlockPlacement",         false, ""),
    TWEAK_FREE_CAMERA               ("tweakFreeCamera",                     false, ""),
    TWEAK_GAMMA_OVERRIDE            ("tweakGammaOverride",                  false, ""),
    TWEAK_HAND_RESTOCK              ("tweakHandRestock",                    false, ""),
    TWEAK_HANGABLE_ENTITY_BYPASS    ("tweakHangableEntityBypass",           false, ""),
    TWEAK_HOLD_ATTACK               ("tweakHoldAttack",                     false, ""),
    TWEAK_HOLD_USE                  ("tweakHoldUse",                        false, ""),
    TWEAK_HOTBAR_SCROLL             ("tweakHotbarScroll",                   false, ""),
    TWEAK_HOTBAR_SLOT_CYCLE         ("tweakHotbarSlotCycle",                false, "",    KeybindSettings.INGAME_BOTH),
    TWEAK_HOTBAR_SLOT_RANDOMIZER    ("tweakHotbarSlotRandomizer",           false, "",    KeybindSettings.INGAME_BOTH),
    TWEAK_HOTBAR_SWAP               ("tweakHotbarSwap",                     false, ""),
    TWEAK_INVENTORY_PREVIEW         ("tweakInventoryPreview",               false, true, ""),
    TWEAK_ITEM_UNSTACKING_PROTECTION("tweakItemUnstackingProtection",       false, ""),
    TWEAK_LAVA_VISIBILITY           ("tweakLavaVisibility",                 false, ""),
    TWEAK_MAP_PREVIEW               ("tweakMapPreview",                     false, ""),
    TWEAK_MOVEMENT_KEYS             ("tweakMovementKeysLast",               false, ""),
    TWEAK_PERIODIC_ATTACK           ("tweakPeriodicAttack",                 false, ""),
    TWEAK_PERIODIC_USE              ("tweakPeriodicUse",                    false, ""),
    TWEAK_PERIODIC_HOLD_ATTACK      ("tweakPeriodicHoldAttack",             false, ""),
    TWEAK_PERIODIC_HOLD_USE         ("tweakPeriodicHoldUse",                false, ""),
    TWEAK_PERMANENT_SNEAK           ("tweakPermanentSneak",                 false, ""),
    TWEAK_PERMANENT_SPRINT          ("tweakPermanentSprint",                false, ""),
    TWEAK_PLACEMENT_GRID            ("tweakPlacementGrid",                  false, "",    KeybindSettings.INGAME_BOTH),
    TWEAK_PLACEMENT_LIMIT           ("tweakPlacementLimit",                 false, "",    KeybindSettings.INGAME_BOTH),
    TWEAK_PLACEMENT_RESTRICTION     ("tweakPlacementRestriction",           false, ""),
    TWEAK_PLACEMENT_REST_FIRST      ("tweakPlacementRestrictionFirst",      false, ""),
    TWEAK_PLACEMENT_REST_HAND       ("tweakPlacementRestrictionHand",       false, ""),
    TWEAK_PLAYER_INVENTORY_PEEK     ("tweakPlayerInventoryPeek",            false, ""),
    TWEAK_POTION_WARNING            ("tweakPotionWarning",                  false, ""),
    TWEAK_PRINT_DEATH_COORDINATES   ("tweakPrintDeathCoordinates",          false, ""),
    TWEAK_PICK_BEFORE_PLACE         ("tweakPickBeforePlace",                false, ""),
    TWEAK_PLAYER_LIST_ALWAYS_ON     ("tweakPlayerListAlwaysVisible",        false, ""),
    TWEAK_RENDER_EDGE_CHUNKS        ("tweakRenderEdgeChunks",               false, ""),
    TWEAK_RENDER_INVISIBLE_ENTITIES ("tweakRenderInvisibleEntities",        false, ""),
    TWEAK_RENDER_LIMIT_ENTITIES     ("tweakRenderLimitEntities",            false, ""),
    TWEAK_REPAIR_MODE               ("tweakRepairMode",                     false, ""),
    TWEAK_SCULK_PULSE_LENGTH        ("tweakSculkPulseLength",               false, true, ""),
    TWEAK_SERVER_DATA_SYNC          ("tweakServerDataSync",                 false, ""),
    TWEAK_SHULKERBOX_DISPLAY        ("tweakShulkerBoxDisplay",              false, ""),
    TWEAK_SIGN_COPY                 ("tweakSignCopy",                       false, ""),
    TWEAK_SNAP_AIM                  ("tweakSnapAim",                        false, "",    KeybindSettings.INGAME_BOTH),
    TWEAK_SNAP_AIM_LOCK             ("tweakSnapAimLock",                    false, ""),
    TWEAK_SNEAK_1_15_2              ("tweakSneak_1.15.2",                   false, "","tweakeroo.config.feature_toggle.comment.tweakSneak_1_15_2", "tweakeroo.config.feature_toggle.prettyName.tweakSneak_1_15_2", "tweakeroo.config.feature_toggle.name.tweakSneak_1_15_2"),
    TWEAK_SPECTATOR_TELEPORT        ("tweakSpectatorTeleport",              false, ""),
    TWEAK_STRUCTURE_BLOCK_LIMIT     ("tweakStructureBlockLimit",            false, true, ""),
    TWEAK_SWAP_ALMOST_BROKEN_TOOLS  ("tweakSwapAlmostBrokenTools",          false, ""),
    TWEAK_TAB_COMPLETE_COORDINATE   ("tweakTabCompleteCoordinate",          false, ""),
    TWEAK_TOOL_SWITCH               ("tweakToolSwitch",                     false, ""),
    TWEAK_WEAPON_SWITCH             ("tweakWeaponSwitch",                   false, ""),
    TWEAK_Y_MIRROR                  ("tweakYMirror",                        false, ""),
    TWEAK_ZOOM                      ("tweakZoom",                           false, "",    KeybindSettings.INGAME_BOTH);

    public static final ImmutableList<FeatureToggle> VALUES = ImmutableList.copyOf(values());

    private final static String FEATURE_KEY = Reference.MOD_ID+ ".config.feature_toggle";

    private final String name;
    private final String comment;
    private final String prettyName;
    private final String translatedName;
    private final IKeybind keybind;
    private final boolean defaultValueBoolean;
    private final boolean singlePlayer;
    private boolean valueBoolean;
    private IValueChangeCallback<IConfigBoolean> callback;

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey)
    {
        this(name, defaultValue, false, defaultHotkey, KeybindSettings.DEFAULT,
                FEATURE_KEY +".comment."+name,
                FEATURE_KEY +".prettyName."+name,
                FEATURE_KEY +".name."+name);
    }

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, KeybindSettings settings)
    {
        this(name, defaultValue, false, defaultHotkey, settings,
                FEATURE_KEY +".comment."+name,
                FEATURE_KEY +".prettyName."+name,
                FEATURE_KEY +".name."+name);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, KeybindSettings.DEFAULT,
                FEATURE_KEY +".comment."+name,
                FEATURE_KEY +".prettyName."+name,
                FEATURE_KEY +".name."+name);
    }

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, String comment, String prettyName, String translatedName)
    {
        this(name, defaultValue, false, defaultHotkey,
                comment,
                prettyName,
                translatedName);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, String comment, String prettyName, String translatedName)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, KeybindSettings.DEFAULT,
                comment,
                prettyName,
                translatedName);
    }

    // Backwards Compatible constructors - START
    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, String comment)
    {
        this(name, defaultValue, false, defaultHotkey, KeybindSettings.DEFAULT,
                comment,
                FEATURE_KEY +".prettyName."+name,
                FEATURE_KEY +".name."+name);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, String comment)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, KeybindSettings.DEFAULT,
                comment,
                FEATURE_KEY +".prettyName."+name,
                FEATURE_KEY +".name."+name);
    }

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, KeybindSettings settings, String comment)
    {
        this(name, defaultValue, false, defaultHotkey, settings,
                comment,
                FEATURE_KEY +".prettyName."+name,
                FEATURE_KEY +".name."+name);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, KeybindSettings settings, String comment)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, settings,
                comment,
                FEATURE_KEY +".prettyName."+name,
                FEATURE_KEY +".name."+name);
    }

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, String comment, String prettyName)
    {
        this(name, defaultValue, false, defaultHotkey,
                comment,
                prettyName,
                FEATURE_KEY +".name."+name);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, String comment, String prettyName)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, KeybindSettings.DEFAULT,
                comment,
                prettyName,
                FEATURE_KEY +".name."+name);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, KeybindSettings settings, String comment, String prettyName)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, settings,
                comment,
                prettyName,
                FEATURE_KEY +".name."+name);
    }
    // Backwards Compatible constructors - END

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, KeybindSettings settings, String comment, String prettyName, String translatedName)
    {
        this.name = name;
        this.valueBoolean = defaultValue;
        this.defaultValueBoolean = defaultValue;
        this.singlePlayer = singlePlayer;
        this.comment = comment;
        this.prettyName = prettyName;
        this.translatedName = translatedName;
        this.keybind = KeybindMulti.fromStorageString(defaultHotkey, settings);
        this.keybind.setCallback(new KeyCallbackToggleBooleanConfigWithMessage(this));
    }

    @Override
    public ConfigType getType()
    {
        return ConfigType.HOTKEY;
    }

    @Override
    public String getName()
    {
        if (this.singlePlayer)
        {
            return GuiBase.TXT_GOLD + this.name + GuiBase.TXT_RST;
        }

        return this.name;
    }

    @Override
    public String getConfigGuiDisplayName()
    {
        // This doesn't get called ?
        String name = StringUtils.getTranslatedOrFallback(this.getTranslatedName(), this.getName());

        if (this.singlePlayer)
        {
            return GuiBase.TXT_GOLD + name + GuiBase.TXT_RST;
        }

        return name;
    }

    @Override
    public String getPrettyName()
    {
        return StringUtils.getTranslatedOrFallback(this.prettyName,
                !this.prettyName.isEmpty() ? this.prettyName : StringUtils.splitCamelCase(this.name.substring(5)));
    }

    @Override
    public String getStringValue()
    {
        return String.valueOf(this.valueBoolean);
    }

    @Override
    public String getDefaultStringValue()
    {
        return String.valueOf(this.defaultValueBoolean);
    }

    @Override
    public void setValueFromString(String value)
    {
    }

    @Override
    public void onValueChanged()
    {
        if (this.callback != null)
        {
            this.callback.onValueChanged(this);
        }
    }

    @Override
    public void setValueChangeCallback(IValueChangeCallback<IConfigBoolean> callback)
    {
        this.callback = callback;
    }

    @Override
    public String getComment()
    {
        String comment = StringUtils.getTranslatedOrFallback(this.comment, this.comment);

        if (comment != null && this.singlePlayer)
        {
            return comment + "\n" + StringUtils.translate("tweakeroo.label.config_comment.single_player_only");
        }

        return comment;
    }

    @Override
    public String getTranslatedName()
    {
        return this.translatedName;
    }

    @Override
    public IKeybind getKeybind()
    {
        return this.keybind;
    }

    @Override
    public boolean getBooleanValue()
    {
        return this.valueBoolean;
    }

    @Override
    public boolean getDefaultBooleanValue()
    {
        return this.defaultValueBoolean;
    }

    @Override
    public void setBooleanValue(boolean value)
    {
        boolean oldValue = this.valueBoolean;
        this.valueBoolean = value;

        if (oldValue != this.valueBoolean)
        {
            this.onValueChanged();
        }
    }

    @Override
    public boolean isModified()
    {
        return this.valueBoolean != this.defaultValueBoolean;
    }

    @Override
    public boolean isModified(String newValue)
    {
        return Boolean.parseBoolean(newValue) != this.defaultValueBoolean;
    }

    @Override
    public void resetToDefault()
    {
        this.valueBoolean = this.defaultValueBoolean;
    }

    @Override
    public JsonElement getAsJsonElement()
    {
        return new JsonPrimitive(this.valueBoolean);
    }

    @Override
    public void setValueFromJsonElement(JsonElement element)
    {
        try
        {
            if (element.isJsonPrimitive())
            {
                this.valueBoolean = element.getAsBoolean();
            }
            else
            {
                Tweakeroo.logger.warn("Failed to set config value for '{}' from the JSON element '{}'", this.getName(), element);
            }
        }
        catch (Exception e)
        {
            Tweakeroo.logger.warn("Failed to set config value for '{}' from the JSON element '{}'", this.getName(), element, e);
        }
    }
}
