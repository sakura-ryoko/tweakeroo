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
import fi.dy.masa.tweakeroo.Tweakeroo;

public enum FeatureToggle implements IHotkeyTogglable, IConfigNotifiable<IConfigBoolean>
{
    TWEAK_ACCURATE_BLOCK_PLACEMENT  ("tweakAccurateBlockPlacement",         false, "",    "tweakeroo.config.feature_toggle.comment.tweakAccurateBlockPlacement", "", "tweakeroo.config.feature_toggle.name.tweakAccurateBlockPlacement"),
    TWEAK_AFTER_CLICKER             ("tweakAfterClicker",                   false, "",    KeybindSettings.INGAME_BOTH, "tweakeroo.config.feature_toggle.comment.tweakAfterClicker", "tweakeroo.config.feature_toggle.name.tweakAfterClicker"),
    TWEAK_AIM_LOCK                  ("tweakAimLock",                        false, "",    "tweakeroo.config.feature_toggle.comment.tweakAimLock", "", "tweakeroo.config.feature_toggle.name.tweakAimLock"),
    TWEAK_ANGEL_BLOCK               ("tweakAngelBlock",                     false, "",    "tweakeroo.config.feature_toggle.comment.tweakAngelBlock", "", "tweakeroo.config.feature_toggle.name.tweakAngelBlock"),
    TWEAK_AUTO_BAD_OMEN             ("tweakAutoBadOmen",                    false, "",    "tweakeroo.config.feature_toggle.comment.tweakAutoBadOmen", "", "tweakeroo.config.feature_toggle.name.tweakAutoBadOmen"),
    TWEAK_AUTO_SWITCH_ELYTRA        ("tweakAutoSwitchElytra",               false, "",    "tweakeroo.config.feature_toggle.comment.tweakAutoSwitchElytra", "", "tweakeroo.config.feature_toggle.name.tweakAutoSwitchElytra"),
    TWEAK_BLOCK_REACH_OVERRIDE      ("tweakBlockReachOverride",             false, true,  "",    "tweakeroo.config.feature_toggle.comment.tweakBlockReachOverride", "", "tweakeroo.config.feature_toggle.name.tweakBlockReachOverride"),
    TWEAK_BLOCK_TYPE_BREAK_RESTRICTION("tweakBlockTypeBreakRestriction",    false, "",    "tweakeroo.config.feature_toggle.comment.tweakBlockTypeBreakRestriction", "", "tweakeroo.config.feature_toggle.name.tweakBlockTypeBreakRestriction"),
    TWEAK_BREAKING_GRID             ("tweakBreakingGrid",                   false, "",    KeybindSettings.INGAME_BOTH, "tweakeroo.config.feature_toggle.comment.tweakBreakingGrid", "tweakeroo.config.feature_toggle.name.tweakBreakingGrid"),
    TWEAK_BREAKING_RESTRICTION      ("tweakBreakingRestriction",            false, "",    "tweakeroo.config.feature_toggle.comment.tweakBreakingRestriction", "", "tweakeroo.config.feature_toggle.name.tweakBreakingRestriction"),
    TWEAK_CHAT_BACKGROUND_COLOR     ("tweakChatBackgroundColor",            false, "",    "tweakeroo.config.feature_toggle.comment.tweakChatBackgroundColor", "", "tweakeroo.config.feature_toggle.name.tweakChatBackgroundColor"),
    TWEAK_CHAT_PERSISTENT_TEXT      ("tweakChatPersistentText",             false, "",    "tweakeroo.config.feature_toggle.comment.tweakChatPersistentText", "", "tweakeroo.config.feature_toggle.name.tweakChatPersistentText"),
    TWEAK_CHAT_TIMESTAMP            ("tweakChatTimestamp",                  false, "",    "tweakeroo.config.feature_toggle.comment.tweakChatTimestamp", "", "tweakeroo.config.feature_toggle.name.tweakChatTimestamp"),
    TWEAK_COMMAND_BLOCK_EXTRA_FIELDS("tweakCommandBlockExtraFields",        false, "",    "tweakeroo.config.feature_toggle.comment.tweakCommandBlockExtraFields", "", "tweakeroo.config.feature_toggle.name.tweakCommandBlockExtraFields"),
    // TODO 1.19.3+
    //TWEAK_CREATIVE_EXTRA_ITEMS      ("tweakCreativeExtraItems",             false, "",    "tweakeroo.config.feature_toggle.comment.tweakCreativeExtraItems", "", "tweakeroo.config.feature_toggle.name.tweakCreativeExtraItems"),
    // TODO/FIXME 1.19+ the mixin needs an access widener now
    //TWEAK_CUSTOM_FLAT_PRESETS       ("tweakCustomFlatPresets",              false, "",    "tweakeroo.config.feature_toggle.comment.tweakCustomFlatPresets", "", "tweakeroo.config.feature_toggle.name.tweakCustomFlatPresets"),
    TWEAK_CUSTOM_FLY_DECELERATION   ("tweakCustomFlyDeceleration",          false, "",    "tweakeroo.config.feature_toggle.comment.tweakCustomFlyDeceleration", "", "tweakeroo.config.feature_toggle.name.tweakCustomFlyDeceleration"),
    TWEAK_CUSTOM_INVENTORY_GUI_SCALE("tweakCustomInventoryScreenScale",     false, "",    "tweakeroo.config.feature_toggle.comment.tweakCustomInventoryScreenScale", "", "tweakeroo.config.feature_toggle.name.tweakCustomInventoryScreenScale"),
    TWEAK_ELYTRA_CAMERA             ("tweakElytraCamera",                   false, "",    "tweakeroo.config.feature_toggle.comment.tweakElytraCamera", "", "tweakeroo.config.feature_toggle.name.tweakElytraCamera"),
    TWEAK_ENTITY_REACH_OVERRIDE      ("tweakEntityReachOverride",           false, true, "",    "tweakeroo.config.feature_toggle.comment.tweakEntityReachOverride", "", "tweakeroo.config.feature_toggle.name.tweakEntityReachOverride"),
    TWEAK_ENTITY_TYPE_ATTACK_RESTRICTION("tweakEntityTypeAttackRestriction",false, "",    "tweakeroo.config.feature_toggle.comment.tweakEntityTypeAttackRestriction", "", "tweakeroo.config.feature_toggle.name.tweakEntityTypeAttackRestriction"),
    TWEAK_SHULKERBOX_STACKING       ("tweakEmptyShulkerBoxesStack",         false, true, "",    "tweakeroo.config.feature_toggle.comment.tweakEmptyShulkerBoxesStack", "", "tweakeroo.config.feature_toggle.name.tweakEmptyShulkerBoxesStack"),
    TWEAK_EXPLOSION_REDUCED_PARTICLES ("tweakExplosionReducedParticles",    false, "",    "tweakeroo.config.feature_toggle.comment.tweakExplosionReducedParticles", "", "tweakeroo.config.feature_toggle.name.tweakExplosionReducedParticles"),
    TWEAK_F3_CURSOR                 ("tweakF3Cursor",                       false, "",    "tweakeroo.config.feature_toggle.comment.tweakF3Cursor", "", "tweakeroo.config.feature_toggle.name.tweakF3Cursor"),
    TWEAK_FAKE_SNEAKING             ("tweakFakeSneaking",                   false, "",    "tweakeroo.config.feature_toggle.comment.tweakFakeSneaking", "", "tweakeroo.config.feature_toggle.name.tweakFakeSneaking"),
    TWEAK_FAKE_SNEAK_PLACEMENT      ("tweakFakeSneakPlacement",             false, "",    "tweakeroo.config.feature_toggle.comment.tweakFakeSneakPlacement", "", "tweakeroo.config.feature_toggle.name.tweakFakeSneakPlacement"),
    TWEAK_FAST_BLOCK_PLACEMENT      ("tweakFastBlockPlacement",             false, "",    "tweakeroo.config.feature_toggle.comment.tweakFastBlockPlacement", "", "tweakeroo.config.feature_toggle.name.tweakFastBlockPlacement"),
    TWEAK_FAST_LEFT_CLICK           ("tweakFastLeftClick",                  false, "",    "tweakeroo.config.feature_toggle.comment.tweakFastLeftClick", "", "tweakeroo.config.feature_toggle.name.tweakFastLeftClick"),
    TWEAK_FAST_RIGHT_CLICK          ("tweakFastRightClick",                 false, "",    "tweakeroo.config.feature_toggle.comment.tweakFastRightClick", "", "tweakeroo.config.feature_toggle.name.tweakFastRightClick"),
    TWEAK_FILL_CLONE_LIMIT          ("tweakFillCloneLimit",                 false, true, "",    "tweakeroo.config.feature_toggle.comment.tweakFillCloneLimit", "", "tweakeroo.config.feature_toggle.name.tweakFillCloneLimit"),
    TWEAK_FLY_SPEED                 ("tweakFlySpeed",                       false, "",    KeybindSettings.INGAME_BOTH, "tweakeroo.config.feature_toggle.comment.tweakFlySpeed", "tweakeroo.config.feature_toggle.name.tweakFlySpeed"),
    TWEAK_FLEXIBLE_BLOCK_PLACEMENT  ("tweakFlexibleBlockPlacement",         false, "",    "tweakeroo.config.feature_toggle.comment.tweakFlexibleBlockPlacement", "", "tweakeroo.config.feature_toggle.name.tweakFlexibleBlockPlacement"),
    TWEAK_FREE_CAMERA               ("tweakFreeCamera",                     false, "",    "tweakeroo.config.feature_toggle.comment.tweakFreeCamera", "", "tweakeroo.config.feature_toggle.name.tweakFreeCamera"),
    TWEAK_GAMMA_OVERRIDE            ("tweakGammaOverride",                  false, "",    "tweakeroo.config.feature_toggle.comment.tweakGammaOverride", "", "tweakeroo.config.feature_toggle.name.tweakGammaOverride"),
    TWEAK_HAND_RESTOCK              ("tweakHandRestock",                    false, "",    "tweakeroo.config.feature_toggle.comment.tweakHandRestock", "", "tweakeroo.config.feature_toggle.name.tweakHandRestock"),
    TWEAK_HANGABLE_ENTITY_BYPASS    ("tweakHangableEntityBypass",           false, "",    "tweakeroo.config.feature_toggle.comment.tweakHangableEntityBypass", "", "tweakeroo.config.feature_toggle.name.tweakHangableEntityBypass"),
    TWEAK_HOLD_ATTACK               ("tweakHoldAttack",                     false, "",    "tweakeroo.config.feature_toggle.comment.tweakHoldAttack", "", "tweakeroo.config.feature_toggle.name.tweakHoldAttack"),
    TWEAK_HOLD_USE                  ("tweakHoldUse",                        false, "",    "tweakeroo.config.feature_toggle.comment.tweakHoldUse", "", "tweakeroo.config.feature_toggle.name.tweakHoldUse"),
    TWEAK_HOTBAR_SCROLL             ("tweakHotbarScroll",                   false, "",    "tweakeroo.config.feature_toggle.comment.tweakHotbarScroll", "", "tweakeroo.config.feature_toggle.name.tweakHotbarScroll"),
    TWEAK_HOTBAR_SLOT_CYCLE         ("tweakHotbarSlotCycle",                false, "",    KeybindSettings.INGAME_BOTH, "tweakeroo.config.feature_toggle.comment.tweakHotbarSlotCycle", "tweakeroo.config.feature_toggle.name.tweakHotbarSlotCycle"),
    TWEAK_HOTBAR_SLOT_RANDOMIZER    ("tweakHotbarSlotRandomizer",           false, "",    KeybindSettings.INGAME_BOTH, "tweakeroo.config.feature_toggle.comment.tweakHotbarSlotRandomizer", "tweakeroo.config.feature_toggle.name.tweakHotbarSlotRandomizer"),
    TWEAK_HOTBAR_SWAP               ("tweakHotbarSwap",                     false, "",    "tweakeroo.config.feature_toggle.comment.tweakHotbarSwap", "", "tweakeroo.config.feature_toggle.name.tweakHotbarSwap"),
    TWEAK_INVENTORY_PREVIEW         ("tweakInventoryPreview",               false, true, "",    "tweakeroo.config.feature_toggle.comment.tweakInventoryPreview", "", "tweakeroo.config.feature_toggle.name.tweakInventoryPreview"),
    TWEAK_ITEM_UNSTACKING_PROTECTION("tweakItemUnstackingProtection",       false, "",    "tweakeroo.config.feature_toggle.comment.tweakItemUnstackingProtection", "", "tweakeroo.config.feature_toggle.name.tweakItemUnstackingProtection"),
    TWEAK_LAVA_VISIBILITY           ("tweakLavaVisibility",                 false, "",    "tweakeroo.config.feature_toggle.comment.tweakLavaVisibility", "", "tweakeroo.config.feature_toggle.name.tweakLavaVisibility"),
    TWEAK_MAP_PREVIEW               ("tweakMapPreview",                     false, "",    "tweakeroo.config.feature_toggle.comment.tweakMapPreview", "", "tweakeroo.config.feature_toggle.name.tweakMapPreview"),
    TWEAK_MOVEMENT_KEYS             ("tweakMovementKeysLast",               false, "",    "tweakeroo.config.feature_toggle.comment.tweakMovementKeysLast", "", "tweakeroo.config.feature_toggle.name.tweakMovementKeysLast"),
    TWEAK_NO_BLOCK_BREAK_CD         ("tweakNoBlockBreakCooldown",           false, "",    "tweakeroo.config.feature_toggle.comment.tweakNoBlockBreakCooldown", "", "tweakeroo.config.feature_toggle.name.tweakNoBlockBreakCooldown"),
    TWEAK_PERIODIC_ATTACK           ("tweakPeriodicAttack",                 false, "",    "tweakeroo.config.feature_toggle.comment.tweakPeriodicAttack", "", "tweakeroo.config.feature_toggle.name.tweakPeriodicAttack"),
    TWEAK_PERIODIC_USE              ("tweakPeriodicUse",                    false, "",    "tweakeroo.config.feature_toggle.comment.tweakPeriodicUse", "", "tweakeroo.config.feature_toggle.name.tweakPeriodicUse"),
    TWEAK_PERIODIC_HOLD_ATTACK      ("tweakPeriodicHoldAttack",             false, "",    "tweakeroo.config.feature_toggle.comment.tweakPeriodicHoldAttack", "", "tweakeroo.config.feature_toggle.name.tweakPeriodicHoldAttack"),
    TWEAK_PERIODIC_HOLD_USE         ("tweakPeriodicHoldUse",                false, "",    "tweakeroo.config.feature_toggle.comment.tweakPeriodicHoldUse", "", "tweakeroo.config.feature_toggle.name.tweakPeriodicHoldUse"),
    TWEAK_PERMANENT_SNEAK           ("tweakPermanentSneak",                 false, "",    "tweakeroo.config.feature_toggle.comment.tweakPermanentSneak", "", "tweakeroo.config.feature_toggle.name.tweakPermanentSneak"),
    TWEAK_PERMANENT_SPRINT          ("tweakPermanentSprint",                false, "",    "tweakeroo.config.feature_toggle.comment.tweakPermanentSprint", "", "tweakeroo.config.feature_toggle.name.tweakPermanentSprint"),
    TWEAK_PLACEMENT_GRID            ("tweakPlacementGrid",                  false, "",    KeybindSettings.INGAME_BOTH, "tweakeroo.config.feature_toggle.comment.tweakPlacementGrid", "tweakeroo.config.feature_toggle.name.tweakPlacementGrid"),
    TWEAK_PLACEMENT_LIMIT           ("tweakPlacementLimit",                 false, "",    KeybindSettings.INGAME_BOTH, "tweakeroo.config.feature_toggle.comment.tweakPlacementLimit", "tweakeroo.config.feature_toggle.name.tweakPlacementLimit"),
    TWEAK_PLACEMENT_RESTRICTION     ("tweakPlacementRestriction",           false, "",    "tweakeroo.config.feature_toggle.comment.tweakPlacementRestriction", "", "tweakeroo.config.feature_toggle.name.tweakPlacementRestriction"),
    TWEAK_PLACEMENT_REST_FIRST      ("tweakPlacementRestrictionFirst",      false, "",    "tweakeroo.config.feature_toggle.comment.tweakPlacementRestrictionFirst", "", "tweakeroo.config.feature_toggle.name.tweakPlacementRestrictionFirst"),
    TWEAK_PLACEMENT_REST_HAND       ("tweakPlacementRestrictionHand",       false, "",    "tweakeroo.config.feature_toggle.comment.tweakPlacementRestrictionHand", "", "tweakeroo.config.feature_toggle.name.tweakPlacementRestrictionHand"),
    TWEAK_PLAYER_INVENTORY_PEEK     ("tweakPlayerInventoryPeek",            false, "",    "tweakeroo.config.feature_toggle.comment.tweakPlayerInventoryPeek", "", "tweakeroo.config.feature_toggle.name.tweakPlayerInventoryPeek"),
    TWEAK_POTION_WARNING            ("tweakPotionWarning",                  false, "",    "tweakeroo.config.feature_toggle.comment.tweakPotionWarning", "", "tweakeroo.config.feature_toggle.name.tweakPotionWarning"),
    TWEAK_PRINT_DEATH_COORDINATES   ("tweakPrintDeathCoordinates",          false, "",    "tweakeroo.config.feature_toggle.comment.tweakPrintDeathCoordinates", "", "tweakeroo.config.feature_toggle.name.tweakPrintDeathCoordinates"),
    TWEAK_PICK_BEFORE_PLACE         ("tweakPickBeforePlace",                false, "",    "tweakeroo.config.feature_toggle.comment.tweakPickBeforePlace", "", "tweakeroo.config.feature_toggle.name.tweakPickBeforePlace"),
    TWEAK_PLAYER_LIST_ALWAYS_ON     ("tweakPlayerListAlwaysVisible",        false, "",    "tweakeroo.config.feature_toggle.comment.tweakPlayerListAlwaysVisible", "", "tweakeroo.config.feature_toggle.name.tweakPlayerListAlwaysVisible"),
    TWEAK_RENDER_EDGE_CHUNKS        ("tweakRenderEdgeChunks",               false, "",    "tweakeroo.config.feature_toggle.comment.tweakRenderEdgeChunks", "", "tweakeroo.config.feature_toggle.name.tweakRenderEdgeChunks"),
    TWEAK_RENDER_INVISIBLE_ENTITIES ("tweakRenderInvisibleEntities",        false, "",    "tweakeroo.config.feature_toggle.comment.tweakRenderInvisibleEntities", "", "tweakeroo.config.feature_toggle.name.tweakRenderInvisibleEntities"),
    TWEAK_RENDER_LIMIT_ENTITIES     ("tweakRenderLimitEntities",            false, "",    "tweakeroo.config.feature_toggle.comment.tweakRenderLimitEntities", "", "tweakeroo.config.feature_toggle.name.tweakRenderLimitEntities"),
    TWEAK_REPAIR_MODE               ("tweakRepairMode",                     false, "",    "tweakeroo.config.feature_toggle.comment.tweakRepairMode", "", "tweakeroo.config.feature_toggle.name.tweakRepairMode"),
    TWEAK_SCULK_PULSE_LENGTH        ("tweakSculkPulseLength",               false, true, "",    "tweakeroo.config.feature_toggle.comment.tweakSculkPulseLength", "", "tweakeroo.config.feature_toggle.name.tweakSculkPulseLength"),
    TWEAK_SERVER_DATA_SYNC          ("tweakServerDataSync",                 false, "",    "tweakeroo.config.feature_toggle.comment.tweakServerDataSync", "", "tweakeroo.config.feature_toggle.name.tweakServerDataSync"),
    TWEAK_SHULKERBOX_DISPLAY        ("tweakShulkerBoxDisplay",              false, "",    "tweakeroo.config.feature_toggle.comment.tweakShulkerBoxDisplay", "", "tweakeroo.config.feature_toggle.name.tweakShulkerBoxDisplay"),
    TWEAK_SIGN_COPY                 ("tweakSignCopy",                       false, "",    "tweakeroo.config.feature_toggle.comment.tweakSignCopy", "", "tweakeroo.config.feature_toggle.name.tweakSignCopy"),
    TWEAK_SNAP_AIM                  ("tweakSnapAim",                        false, "",    KeybindSettings.INGAME_BOTH, "tweakeroo.config.feature_toggle.comment.tweakSnapAim", "tweakeroo.config.feature_toggle.name.tweakSnapAim"),
    TWEAK_SNAP_AIM_LOCK             ("tweakSnapAimLock",                    false, "",    "tweakeroo.config.feature_toggle.comment.tweakSnapAimLock", "", "tweakeroo.config.feature_toggle.name.tweakSnapAimLock"),
    TWEAK_SNEAK_1_15_2              ("tweakSneak_1.15.2",                   false, "",    "tweakeroo.config.feature_toggle.comment.tweakSneak_1_15_2", "", "tweakeroo.config.feature_toggle.name.tweakSneak_1_15_2"),
    TWEAK_SPECTATOR_TELEPORT        ("tweakSpectatorTeleport",              false, "",    "tweakeroo.config.feature_toggle.comment.tweakSpectatorTeleport", "", "tweakeroo.config.feature_toggle.name.tweakSpectatorTeleport"),
    TWEAK_STRUCTURE_BLOCK_LIMIT     ("tweakStructureBlockLimit",            false, true, "",    "tweakeroo.config.feature_toggle.comment.tweakStructureBlockLimit", "", "tweakeroo.config.feature_toggle.name.tweakStructureBlockLimit"),
    TWEAK_SWAP_ALMOST_BROKEN_TOOLS  ("tweakSwapAlmostBrokenTools",          false, "",    "tweakeroo.config.feature_toggle.comment.tweakSwapAlmostBrokenTools", "", "tweakeroo.config.feature_toggle.name.tweakSwapAlmostBrokenTools"),
    TWEAK_TAB_COMPLETE_COORDINATE   ("tweakTabCompleteCoordinate",          false, "",    "tweakeroo.config.feature_toggle.comment.tweakTabCompleteCoordinate", "", "tweakeroo.config.feature_toggle.name.tweakTabCompleteCoordinate"),
    TWEAK_TOOL_SWITCH               ("tweakToolSwitch",                     false, "",    "tweakeroo.config.feature_toggle.comment.tweakToolSwitch", "", "tweakeroo.config.feature_toggle.name.tweakToolSwitch"),
    TWEAK_WEAPON_SWITCH             ("tweakWeaponSwitch",                   false, "",    "tweakeroo.config.feature_toggle.comment.tweakWeaponSwitch", "", "tweakeroo.config.feature_toggle.name.tweakWeaponSwitch"),
    TWEAK_Y_MIRROR                  ("tweakYMirror",                        false, "",    "tweakeroo.config.feature_toggle.comment.tweakYMirror", "", "tweakeroo.config.feature_toggle.name.tweakYMirror"),
    TWEAK_ZOOM                      ("tweakZoom",                           false, "",    KeybindSettings.INGAME_BOTH, "tweakeroo.config.feature_toggle.comment.tweakZoom", "tweakeroo.config.feature_toggle.name.tweakZoom");

    public static final ImmutableList<FeatureToggle> VALUES = ImmutableList.copyOf(values());

    private final String name;
    private final String comment;
    private final String prettyName;
    private final String translatedName;
    private final IKeybind keybind;
    private final boolean defaultValueBoolean;
    private final boolean singlePlayer;
    private boolean valueBoolean;
    private IValueChangeCallback<IConfigBoolean> callback;

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, String comment)
    {
        this(name, defaultValue, false, defaultHotkey, KeybindSettings.DEFAULT, comment);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, String comment)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, KeybindSettings.DEFAULT, comment, name);
    }

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, KeybindSettings settings, String comment)
    {
        this(name, defaultValue, false, defaultHotkey, settings, comment, name);
    }

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, KeybindSettings settings, String comment, String translatedName)
    {
        this(name, defaultValue, false, defaultHotkey, settings, comment, translatedName);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, KeybindSettings settings, String comment)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, settings, comment, StringUtils.splitCamelCase(name.substring(5)), name);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, KeybindSettings settings, String comment, String translatedName)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, settings, comment, StringUtils.splitCamelCase(name.substring(5)), translatedName);
    }

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, String comment, String prettyName)
    {
        this(name, defaultValue, false, defaultHotkey, comment, prettyName, name);
    }

    FeatureToggle(String name, boolean defaultValue, String defaultHotkey, String comment, String prettyName, String translatedName)
    {
        this(name, defaultValue, false, defaultHotkey, comment, prettyName, translatedName);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, String comment, String prettyName)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, KeybindSettings.DEFAULT, comment, prettyName, name);
    }

    FeatureToggle(String name, boolean defaultValue, boolean singlePlayer, String defaultHotkey, String comment, String prettyName, String translatedName)
    {
        this(name, defaultValue, singlePlayer, defaultHotkey, KeybindSettings.DEFAULT, comment, prettyName, translatedName);
    }

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
        return this.prettyName.isEmpty() == false ? this.prettyName : this.getName();
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
