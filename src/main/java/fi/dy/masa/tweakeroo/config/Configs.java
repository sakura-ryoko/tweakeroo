package fi.dy.masa.tweakeroo.config;

import java.io.File;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.HudAlignment;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.util.ActiveMode;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.MessageOutputType;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction.ListType;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.util.EasyPlacementProtocol;
import fi.dy.masa.tweakeroo.util.InventoryUtils;
import fi.dy.masa.tweakeroo.util.PlacementRestrictionMode;
import fi.dy.masa.tweakeroo.util.SnapAimMode;

public class Configs implements IConfigHandler
{
    private static final String CONFIG_FILE_NAME = Reference.MOD_ID + ".json";

    private static final String GENERIC_KEY = Reference.MOD_ID+".config.generic";
    private static final String FIXES_KEY = Reference.MOD_ID+".config.fixes";
    private static final String LISTS_KEY = Reference.MOD_ID+".config.lists";
    private static final String DISABLE_KEY = Reference.MOD_ID+".config.disable";
    private static final String INTERNAL_KEY = Reference.MOD_ID+".config.internal";

    public static class Generic
    {
        public static final ConfigOptionList    ACCURATE_PLACEMENT_PROTOCOL_MODE    = new ConfigOptionList  ("accuratePlacementProtocolMode", EasyPlacementProtocol.AUTO).apply(GENERIC_KEY);
        public static final ConfigBoolean       ACCURATE_PLACEMENT_PROTOCOL         = new ConfigBoolean     ("accuratePlacementProtocol", true).apply(GENERIC_KEY);
        public static final ConfigInteger       AFTER_CLICKER_CLICK_COUNT           = new ConfigInteger     ("afterClickerClickCount",  1, 1, 32).apply(GENERIC_KEY);
        public static final ConfigDouble        ANGEL_BLOCK_PLACEMENT_DISTANCE      = new ConfigDouble      ("angelBlockPlacementDistance",  3, 1, 5).apply(GENERIC_KEY);
        public static final ConfigDouble        BLOCK_REACH_DISTANCE                = new ConfigDouble      ("blockReachDistance", 4.5, 1, 64).apply(GENERIC_KEY);
        public static final ConfigOptionList    BLOCK_TYPE_BREAK_RESTRICTION_WARN   = new ConfigOptionList  ("blockTypeBreakRestrictionWarn", MessageOutputType.MESSAGE).apply(GENERIC_KEY);
        public static final ConfigInteger       BREAKING_GRID_SIZE                  = new ConfigInteger     ("breakingGridSize", 3, 1, 1000).apply(GENERIC_KEY);
        public static final ConfigOptionList    BREAKING_RESTRICTION_MODE           = new ConfigOptionList  ("breakingRestrictionMode", PlacementRestrictionMode.LINE).apply(GENERIC_KEY);
        public static final ConfigColor         CHAT_BACKGROUND_COLOR               = new ConfigColor       ("chatBackgroundColor", "#80000000").apply(GENERIC_KEY);
        public static final ConfigString        CHAT_TIME_FORMAT                    = new ConfigString      ("chatTimeFormat", "[HH:mm:ss]").apply(GENERIC_KEY);
        public static final ConfigBoolean       CLIENT_PLACEMENT_ROTATION           = new ConfigBoolean     ("clientPlacementRotation", true).apply(GENERIC_KEY);
        public static final ConfigInteger       CUSTOM_INVENTORY_GUI_SCALE          = new ConfigInteger     ("customInventoryGuiScale", 2, 1, 10).apply(GENERIC_KEY);
        public static final ConfigBoolean       DEBUG_LOGGING                       = new ConfigBoolean     ("debugLogging", false).apply(GENERIC_KEY);
        public static final ConfigOptionList    ELYTRA_CAMERA_INDICATOR             = new ConfigOptionList  ("elytraCameraIndicator", ActiveMode.WITH_KEY).apply(GENERIC_KEY);
        public static final ConfigDouble        ENTITY_REACH_DISTANCE               = new ConfigDouble      ("entityReachDistance", 3.0, 1, 64).apply(GENERIC_KEY);
        public static final ConfigOptionList    ENTITY_TYPE_ATTACK_RESTRICTION_WARN = new ConfigOptionList  ("entityTypeAttackRestrictionWarn", MessageOutputType.MESSAGE).apply(GENERIC_KEY);
        public static final ConfigInteger       FAST_BLOCK_PLACEMENT_COUNT          = new ConfigInteger     ("fastBlockPlacementCount", 2, 1, 16).apply(GENERIC_KEY);
        public static final ConfigBoolean       FAST_LEFT_CLICK_ALLOW_TOOLS         = new ConfigBoolean     ("fastLeftClickAllowTools", false).apply(GENERIC_KEY);
        public static final ConfigInteger       FAST_LEFT_CLICK_COUNT               = new ConfigInteger     ("fastLeftClickCount",  10, 1, 64).apply(GENERIC_KEY);
        public static final ConfigBoolean       FAST_PLACEMENT_REMEMBER_ALWAYS      = new ConfigBoolean     ("fastPlacementRememberOrientation", true).apply(GENERIC_KEY);
        public static final ConfigInteger       FAST_RIGHT_CLICK_COUNT              = new ConfigInteger     ("fastRightClickCount", 10, 1, 64).apply(GENERIC_KEY);
        public static final ConfigInteger       FILL_CLONE_LIMIT                    = new ConfigInteger     ("fillCloneLimit", 10000000, 1, 1000000000).apply(GENERIC_KEY);
        public static final ConfigColor         FLEXIBLE_PLACEMENT_OVERLAY_COLOR    = new ConfigColor       ("flexibleBlockPlacementOverlayColor", "#C03030F0").apply(GENERIC_KEY);
        public static final ConfigDouble        FLY_DECELERATION_FACTOR             = new ConfigDouble      ("flyDecelerationFactor", 0.4, 0.0, 1.0).apply(GENERIC_KEY);
        public static final ConfigDouble        FLY_SPEED_PRESET_1                  = new ConfigDouble      ("flySpeedPreset1", 0.01, 0, 4).apply(GENERIC_KEY);
        public static final ConfigDouble        FLY_SPEED_PRESET_2                  = new ConfigDouble      ("flySpeedPreset2", 0.064, 0, 4).apply(GENERIC_KEY);
        public static final ConfigDouble        FLY_SPEED_PRESET_3                  = new ConfigDouble      ("flySpeedPreset3", 0.128, 0, 4).apply(GENERIC_KEY);
        public static final ConfigDouble        FLY_SPEED_PRESET_4                  = new ConfigDouble      ("flySpeedPreset4", 0.32, 0, 4).apply(GENERIC_KEY);
        public static final ConfigBoolean       FREE_CAMERA_PLAYER_INPUTS           = new ConfigBoolean     ("freeCameraPlayerInputs", false).apply(GENERIC_KEY);
        public static final ConfigBoolean       FREE_CAMERA_PLAYER_MOVEMENT         = new ConfigBoolean     ("freeCameraPlayerMovement", false).apply(GENERIC_KEY);
        public static final ConfigDouble        GAMMA_OVERRIDE_VALUE                = new ConfigDouble      ("gammaOverrideValue", 16, 0, 32).apply(GENERIC_KEY);
        public static final ConfigBoolean       HAND_RESTOCK_PRE                    = new ConfigBoolean     ("handRestockPre", true).apply(GENERIC_KEY);
        public static final ConfigInteger       HAND_RESTOCK_PRE_THRESHOLD          = new ConfigInteger     ("handRestockPreThreshold", 6, 1, 64).apply(GENERIC_KEY);
        public static final ConfigBoolean       HANGABLE_ENTITY_BYPASS_INVERSE      = new ConfigBoolean     ("hangableEntityBypassInverse", false).apply(GENERIC_KEY);
        public static final ConfigInteger       HOTBAR_SLOT_CYCLE_MAX               = new ConfigInteger     ("hotbarSlotCycleMax", 2, 1, 9).apply(GENERIC_KEY);
        public static final ConfigInteger       HOTBAR_SLOT_RANDOMIZER_MAX          = new ConfigInteger     ("hotbarSlotRandomizerMax", 5, 1, 9).apply(GENERIC_KEY);
        public static final ConfigOptionList    HOTBAR_SWAP_OVERLAY_ALIGNMENT       = new ConfigOptionList  ("hotbarSwapOverlayAlignment", HudAlignment.BOTTOM_RIGHT).apply(GENERIC_KEY);
        public static final ConfigInteger       HOTBAR_SWAP_OVERLAY_OFFSET_X        = new ConfigInteger     ("hotbarSwapOverlayOffsetX", 4).apply(GENERIC_KEY);
        public static final ConfigInteger       HOTBAR_SWAP_OVERLAY_OFFSET_Y        = new ConfigInteger     ("hotbarSwapOverlayOffsetY", 4).apply(GENERIC_KEY);
        public static final ConfigInteger       ITEM_SWAP_DURABILITY_THRESHOLD      = new ConfigInteger     ("itemSwapDurabilityThreshold", 20, 5, 10000).apply(GENERIC_KEY);
        public static final ConfigBoolean       ITEM_USE_PACKET_CHECK_BYPASS        = new ConfigBoolean     ("itemUsePacketCheckBypass", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       MAP_PREVIEW_REQUIRE_SHIFT           = new ConfigBoolean     ("mapPreviewRequireShift", true).apply(GENERIC_KEY);
        public static final ConfigInteger       MAP_PREVIEW_SIZE                    = new ConfigInteger     ("mapPreviewSize", 160, 16, 512).apply(GENERIC_KEY);
        public static final ConfigInteger       PERIODIC_ATTACK_INTERVAL            = new ConfigInteger     ("periodicAttackInterval", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
        public static final ConfigBoolean       PERIODIC_ATTACK_RESET_ON_ACTIVATE   = new ConfigBoolean     ("periodicAttackResetIntervalOnActivate", true).apply(GENERIC_KEY);
        public static final ConfigInteger       PERIODIC_USE_INTERVAL               = new ConfigInteger     ("periodicUseInterval", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
        public static final ConfigBoolean       PERIODIC_USE_RESET_ON_ACTIVATE      = new ConfigBoolean     ("periodicUseResetIntervalOnActivate", true).apply(GENERIC_KEY);
        public static final ConfigInteger       PERIODIC_HOLD_ATTACK_DURATION       = new ConfigInteger     ("periodicHoldAttackDuration", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
        public static final ConfigInteger       PERIODIC_HOLD_ATTACK_INTERVAL       = new ConfigInteger     ("periodicHoldAttackInterval", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
        public static final ConfigBoolean       PERIODIC_HOLD_ATTACK_RESET_ON_ACTIVATE= new ConfigBoolean   ("periodicHoldAttackResetIntervalOnActivate", true).apply(GENERIC_KEY);
        public static final ConfigInteger       PERIODIC_HOLD_USE_DURATION          = new ConfigInteger     ("periodicHoldUseDuration", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
        public static final ConfigInteger       PERIODIC_HOLD_USE_INTERVAL          = new ConfigInteger     ("periodicHoldUseInterval", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
        public static final ConfigBoolean       PERIODIC_HOLD_USE_RESET_ON_ACTIVATE = new ConfigBoolean     ("periodicHoldUseResetIntervalOnActivate", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       PERMANENT_SNEAK_ALLOW_IN_GUIS       = new ConfigBoolean     ("permanentSneakAllowInGUIs", false).apply(GENERIC_KEY);
        public static final ConfigInteger       PLACEMENT_GRID_SIZE                 = new ConfigInteger     ("placementGridSize", 3, 1, 1000).apply(GENERIC_KEY);
        public static final ConfigInteger       PLACEMENT_LIMIT                     = new ConfigInteger     ("placementLimit", 3, 1, 10000).apply(GENERIC_KEY);
        public static final ConfigOptionList    PLACEMENT_RESTRICTION_MODE          = new ConfigOptionList  ("placementRestrictionMode", PlacementRestrictionMode.FACE).apply(GENERIC_KEY);
        public static final ConfigBoolean       PLACEMENT_RESTRICTION_TIED_TO_FAST  = new ConfigBoolean     ("placementRestrictionTiedToFast", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       POTION_WARNING_BENEFICIAL_ONLY      = new ConfigBoolean     ("potionWarningBeneficialOnly", true).apply(GENERIC_KEY);
        public static final ConfigInteger       POTION_WARNING_THRESHOLD            = new ConfigInteger     ("potionWarningThreshold", 600, 1, 1000000).apply(GENERIC_KEY);
        public static final ConfigBoolean       REMEMBER_FLEXIBLE                   = new ConfigBoolean     ("rememberFlexibleFromClick", true).apply(GENERIC_KEY);
        public static final ConfigInteger       RENDER_LIMIT_ITEM                   = new ConfigInteger     ("renderLimitItem", -1, -1, 10000).apply(GENERIC_KEY);
        public static final ConfigInteger       RENDER_LIMIT_XP_ORB                 = new ConfigInteger     ("renderLimitXPOrb", -1, -1, 10000).apply(GENERIC_KEY);
        public static final ConfigInteger       SCULK_SENSOR_PULSE_LENGTH           = new ConfigInteger     ("sculkSensorPulseLength", 40, 0, 10000).apply(GENERIC_KEY);
        public static final ConfigInteger       SERVER_NBT_REQUEST_RATE             = new ConfigInteger     ("serverNbtRequestRate", 2).apply(GENERIC_KEY);
        public static final ConfigBoolean       SHULKER_DISPLAY_BACKGROUND_COLOR    = new ConfigBoolean     ("shulkerDisplayBgColor", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       SHULKER_DISPLAY_REQUIRE_SHIFT       = new ConfigBoolean     ("shulkerDisplayRequireShift", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       SLOT_SYNC_WORKAROUND                = new ConfigBoolean     ("slotSyncWorkaround", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       SLOT_SYNC_WORKAROUND_ALWAYS         = new ConfigBoolean     ("slotSyncWorkaroundAlways", false).apply(GENERIC_KEY);
        public static final ConfigBoolean       SNAP_AIM_INDICATOR                  = new ConfigBoolean     ("snapAimIndicator", true).apply(GENERIC_KEY);
        public static final ConfigColor         SNAP_AIM_INDICATOR_COLOR            = new ConfigColor       ("snapAimIndicatorColor", "#603030FF").apply(GENERIC_KEY);
        public static final ConfigOptionList    SNAP_AIM_MODE                       = new ConfigOptionList  ("snapAimMode", SnapAimMode.YAW).apply(GENERIC_KEY);
        public static final ConfigBoolean       SNAP_AIM_ONLY_CLOSE_TO_ANGLE        = new ConfigBoolean     ("snapAimOnlyCloseToAngle", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       SNAP_AIM_PITCH_OVERSHOOT            = new ConfigBoolean     ("snapAimPitchOvershoot", false).apply(GENERIC_KEY);
        public static final ConfigDouble        SNAP_AIM_PITCH_STEP                 = new ConfigDouble      ("snapAimPitchStep", 12.5, 0, 90).apply(GENERIC_KEY);
        public static final ConfigDouble        SNAP_AIM_THRESHOLD_PITCH            = new ConfigDouble      ("snapAimThresholdPitch", 1.5).apply(GENERIC_KEY);
        public static final ConfigDouble        SNAP_AIM_THRESHOLD_YAW              = new ConfigDouble      ("snapAimThresholdYaw", 5.0).apply(GENERIC_KEY);
        public static final ConfigDouble        SNAP_AIM_YAW_STEP                   = new ConfigDouble      ("snapAimYawStep", 45, 0, 360).apply(GENERIC_KEY);
        public static final ConfigInteger       STRUCTURE_BLOCK_MAX_SIZE            = new ConfigInteger     ("structureBlockMaxSize", 128, 1, 256).apply(GENERIC_KEY);
        public static final ConfigString        TOOL_SWITCHABLE_SLOTS               = new ConfigString      ("toolSwitchableSlots", "1-9").apply(GENERIC_KEY);
        public static final ConfigString        TOOL_SWITCH_IGNORED_SLOTS           = new ConfigString      ("toolSwitchIgnoredSlots", "").apply(GENERIC_KEY);
        public static final ConfigBoolean       TOOL_SWAP_BETTER_ENCHANTS           = new ConfigBoolean     ("toolSwapBetterEnchants",   false).apply(GENERIC_KEY);
        public static final ConfigBoolean       WEAPON_SWAP_BETTER_ENCHANTS         = new ConfigBoolean     ("weaponSwapBetterEnchants", false).apply(GENERIC_KEY);
        public static final ConfigBoolean       ZOOM_ADJUST_MOUSE_SENSITIVITY       = new ConfigBoolean     ("zoomAdjustMouseSensitivity", true).apply(GENERIC_KEY);
        public static final ConfigDouble        ZOOM_FOV                            = new ConfigDouble      ("zoomFov", 30, 0.01, 359.99).apply(GENERIC_KEY);
        public static final ConfigBoolean       ZOOM_RESET_FOV_ON_ACTIVATE          = new ConfigBoolean     ("zoomResetFovOnActivate", true).apply(GENERIC_KEY);

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                ACCURATE_PLACEMENT_PROTOCOL_MODE,
                ACCURATE_PLACEMENT_PROTOCOL,
                CLIENT_PLACEMENT_ROTATION,
                DEBUG_LOGGING,
                FAST_LEFT_CLICK_ALLOW_TOOLS,
                FAST_PLACEMENT_REMEMBER_ALWAYS,
                FREE_CAMERA_PLAYER_INPUTS,
                FREE_CAMERA_PLAYER_MOVEMENT,
                HAND_RESTOCK_PRE,
                HANGABLE_ENTITY_BYPASS_INVERSE,
                ITEM_USE_PACKET_CHECK_BYPASS,
                MAP_PREVIEW_REQUIRE_SHIFT,
                PERMANENT_SNEAK_ALLOW_IN_GUIS,
                PLACEMENT_RESTRICTION_TIED_TO_FAST,
                POTION_WARNING_BENEFICIAL_ONLY,
                REMEMBER_FLEXIBLE,
                SHULKER_DISPLAY_BACKGROUND_COLOR,
                SHULKER_DISPLAY_REQUIRE_SHIFT,
                SLOT_SYNC_WORKAROUND,
                SLOT_SYNC_WORKAROUND_ALWAYS,
                SNAP_AIM_INDICATOR,
                SNAP_AIM_ONLY_CLOSE_TO_ANGLE,
                SNAP_AIM_PITCH_OVERSHOOT,
                ZOOM_ADJUST_MOUSE_SENSITIVITY,

                BLOCK_TYPE_BREAK_RESTRICTION_WARN,
                BREAKING_RESTRICTION_MODE,
                ELYTRA_CAMERA_INDICATOR,
                ENTITY_TYPE_ATTACK_RESTRICTION_WARN,
                PLACEMENT_RESTRICTION_MODE,
                HOTBAR_SWAP_OVERLAY_ALIGNMENT,
                SNAP_AIM_MODE,

                CHAT_TIME_FORMAT,
                CHAT_BACKGROUND_COLOR,
                FLEXIBLE_PLACEMENT_OVERLAY_COLOR,
                SNAP_AIM_INDICATOR_COLOR,

                AFTER_CLICKER_CLICK_COUNT,
                BLOCK_REACH_DISTANCE,
                ANGEL_BLOCK_PLACEMENT_DISTANCE,
                BREAKING_GRID_SIZE,
                CUSTOM_INVENTORY_GUI_SCALE,
                ENTITY_REACH_DISTANCE,
                FAST_BLOCK_PLACEMENT_COUNT,
                FAST_LEFT_CLICK_COUNT,
                FAST_RIGHT_CLICK_COUNT,
                FILL_CLONE_LIMIT,
                FLY_DECELERATION_FACTOR,
                FLY_SPEED_PRESET_1,
                FLY_SPEED_PRESET_2,
                FLY_SPEED_PRESET_3,
                FLY_SPEED_PRESET_4,
                GAMMA_OVERRIDE_VALUE,
                HAND_RESTOCK_PRE_THRESHOLD,
                HOTBAR_SLOT_CYCLE_MAX,
                HOTBAR_SLOT_RANDOMIZER_MAX,
                HOTBAR_SWAP_OVERLAY_OFFSET_X,
                HOTBAR_SWAP_OVERLAY_OFFSET_Y,
                ITEM_SWAP_DURABILITY_THRESHOLD,
                MAP_PREVIEW_SIZE,
                PERIODIC_ATTACK_INTERVAL,
                PERIODIC_ATTACK_RESET_ON_ACTIVATE,
                PERIODIC_USE_INTERVAL,
                PERIODIC_USE_RESET_ON_ACTIVATE,
                PERIODIC_HOLD_ATTACK_DURATION,
                PERIODIC_HOLD_ATTACK_INTERVAL,
                PERIODIC_HOLD_ATTACK_RESET_ON_ACTIVATE,
                PERIODIC_HOLD_USE_DURATION,
                PERIODIC_HOLD_USE_INTERVAL,
                PERIODIC_HOLD_USE_RESET_ON_ACTIVATE,
                PLACEMENT_GRID_SIZE,
                PLACEMENT_LIMIT,
                POTION_WARNING_THRESHOLD,
                RENDER_LIMIT_ITEM,
                RENDER_LIMIT_XP_ORB,
                SCULK_SENSOR_PULSE_LENGTH,
                SERVER_NBT_REQUEST_RATE,
                SNAP_AIM_PITCH_STEP,
                SNAP_AIM_THRESHOLD_PITCH,
                SNAP_AIM_THRESHOLD_YAW,
                SNAP_AIM_YAW_STEP,
                STRUCTURE_BLOCK_MAX_SIZE,
                TOOL_SWITCHABLE_SLOTS,
                TOOL_SWITCH_IGNORED_SLOTS,
                TOOL_SWAP_BETTER_ENCHANTS,
                WEAPON_SWAP_BETTER_ENCHANTS,
                ZOOM_FOV,
                ZOOM_RESET_FOV_ON_ACTIVATE
        );
    }

    public static class Fixes
    {
        //public static final ConfigBoolean ELYTRA_FIX                        = new ConfigBoolean("elytraFix", false).apply(FIXES_KEY);
        public static final ConfigBoolean MAC_HORIZONTAL_SCROLL             = new ConfigBoolean("macHorizontalScroll", false).apply(FIXES_KEY);
        public static final ConfigBoolean RAVAGER_CLIENT_BLOCK_BREAK_FIX    = new ConfigBoolean("ravagerClientBlockBreakFix", false).apply(FIXES_KEY);

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                //ELYTRA_FIX,
                MAC_HORIZONTAL_SCROLL,
                RAVAGER_CLIENT_BLOCK_BREAK_FIX
        );
    }

    public static class Lists
    {
        public static final ConfigOptionList BLOCK_TYPE_BREAK_RESTRICTION_LIST_TYPE = new ConfigOptionList("blockTypeBreakRestrictionListType", ListType.BLACKLIST).apply(LISTS_KEY);
        public static final ConfigStringList BLOCK_TYPE_BREAK_RESTRICTION_BLACKLIST = new ConfigStringList("blockTypeBreakRestrictionBlackList", ImmutableList.of("minecraft:budding_amethyst")).apply(LISTS_KEY);
        public static final ConfigStringList BLOCK_TYPE_BREAK_RESTRICTION_WHITELIST = new ConfigStringList("blockTypeBreakRestrictionWhiteList", ImmutableList.of()).apply(LISTS_KEY);
        //public static final ConfigStringList CREATIVE_EXTRA_ITEMS               = new ConfigStringList("creativeExtraItems", ImmutableList.of("minecraft:command_block", "minecraft:chain_command_block", "minecraft:repeating_command_block", "minecraft:dragon_egg", "minecraft:structure_void", "minecraft:structure_block", "minecraft:structure_block{BlockEntityTag:{mode:\"SAVE\"}}", "minecraft:structure_block{BlockEntityTag:{mode:\"LOAD\"}}", "minecraft:structure_block{BlockEntityTag:{mode:\"CORNER\"}}")).apply(LISTS_KEY);
        public static final ConfigOptionList ENTITY_TYPE_ATTACK_RESTRICTION_LIST_TYPE = new ConfigOptionList("entityTypeAttackRestrictionListType", ListType.BLACKLIST).apply(LISTS_KEY);
        public static final ConfigStringList ENTITY_TYPE_ATTACK_RESTRICTION_BLACKLIST = new ConfigStringList("entityTypeAttackRestrictionBlackList", ImmutableList.of("minecraft:villager")).apply(LISTS_KEY);
        public static final ConfigStringList ENTITY_TYPE_ATTACK_RESTRICTION_WHITELIST = new ConfigStringList("entityTypeAttackRestrictionWhiteList", ImmutableList.of()).apply(LISTS_KEY);
        public static final ConfigStringList PREFER_SILK_TOUCH                  = new ConfigStringList("preferSilkTouch", ImmutableList.of("minecraft:ender_chest")).apply(LISTS_KEY);
        public static final ConfigStringList ENTITY_WEAPON_MAPPING              = new ConfigStringList("entityWeaponMapping", ImmutableList.of("<default> => minecraft:mace, minecraft:netherite_sword, minecraft:diamond_sword, minecraft:iron_sword, minecraft:golden_sword, minecraft:stone_sword, minecraft:wooden_sword", "minecraft:end_crystal, minecraft:item_frame, minecraft:glow_item_frame, minecraft:leash_knot => <ignore>", "minecraft:minecart, minecraft:chest_minecart, minecraft:furnace_minecart, minecraft:hopper_minecart, minecraft:hopper_minecart, minecraft:spawner_minecart, minecraft:tnt_minecart, minecraft:boat=> minecraft:mace, minecraft:netherite_axe, minecraft:diamond_axe, minecraft:iron_axe, minecraft:golden_axe, minecraft:stone_axe, minecraft:wooden_axe")).apply(LISTS_KEY);
        public static final ConfigOptionList FAST_PLACEMENT_ITEM_LIST_TYPE      = new ConfigOptionList("fastPlacementItemListType", ListType.BLACKLIST).apply(LISTS_KEY);
        public static final ConfigStringList FAST_PLACEMENT_ITEM_BLACKLIST      = new ConfigStringList("fastPlacementItemBlackList", ImmutableList.of("minecraft:ender_chest", "minecraft:white_shulker_box")).apply(LISTS_KEY);
        public static final ConfigStringList FAST_PLACEMENT_ITEM_WHITELIST      = new ConfigStringList("fastPlacementItemWhiteList", ImmutableList.of()).apply(LISTS_KEY);
        public static final ConfigOptionList FAST_RIGHT_CLICK_BLOCK_LIST_TYPE   = new ConfigOptionList("fastRightClickBlockListType", ListType.BLACKLIST).apply(LISTS_KEY);
        public static final ConfigStringList FAST_RIGHT_CLICK_BLOCK_BLACKLIST   = new ConfigStringList("fastRightClickBlockBlackList", ImmutableList.of("minecraft:chest", "minecraft:ender_chest", "minecraft:trapped_chest", "minecraft:white_shulker_box")).apply(LISTS_KEY);
        public static final ConfigStringList FAST_RIGHT_CLICK_BLOCK_WHITELIST   = new ConfigStringList("fastRightClickBlockWhiteList", ImmutableList.of()).apply(LISTS_KEY);
        public static final ConfigOptionList FAST_RIGHT_CLICK_ITEM_LIST_TYPE    = new ConfigOptionList("fastRightClickListType", ListType.NONE).apply(LISTS_KEY);
        public static final ConfigStringList FAST_RIGHT_CLICK_ITEM_BLACKLIST    = new ConfigStringList("fastRightClickBlackList", ImmutableList.of("minecraft:firework_rocket")).apply(LISTS_KEY);
        public static final ConfigStringList FAST_RIGHT_CLICK_ITEM_WHITELIST    = new ConfigStringList("fastRightClickWhiteList", ImmutableList.of("minecraft:bucket", "minecraft:water_bucket", "minecraft:lava_bucket", "minecraft:glass_bottle")).apply(LISTS_KEY);
        //public static final ConfigStringList FLAT_WORLD_PRESETS                 = new ConfigStringList("flatWorldPresets", ImmutableList.of("White Glass;1*minecraft:white_stained_glass;minecraft:plains;;minecraft:white_stained_glass", "Glass;1*minecraft:glass;minecraft:plains;;minecraft:glass")).apply(LISTS_KEY);
        public static final ConfigOptionList HAND_RESTOCK_LIST_TYPE             = new ConfigOptionList("handRestockListType", ListType.NONE).apply(LISTS_KEY);
        public static final ConfigStringList HAND_RESTOCK_BLACKLIST             = new ConfigStringList("handRestockBlackList", ImmutableList.of("minecraft:bucket", "minecraft:lava_bucket", "minecraft:water_bucket")).apply(LISTS_KEY);
        public static final ConfigStringList HAND_RESTOCK_WHITELIST             = new ConfigStringList("handRestockWhiteList", ImmutableList.of()).apply(LISTS_KEY);
        public static final ConfigOptionList POTION_WARNING_LIST_TYPE           = new ConfigOptionList("potionWarningListType", ListType.NONE).apply(LISTS_KEY);
        public static final ConfigStringList POTION_WARNING_BLACKLIST           = new ConfigStringList("potionWarningBlackList", ImmutableList.of("minecraft:hunger", "minecraft:mining_fatigue", "minecraft:nausea", "minecraft:poison", "minecraft:slowness", "minecraft:weakness")).apply(LISTS_KEY);
        public static final ConfigStringList POTION_WARNING_WHITELIST           = new ConfigStringList("potionWarningWhiteList", ImmutableList.of("minecraft:fire_resistance", "minecraft:invisibility", "minecraft:water_breathing")).apply(LISTS_KEY);
        public static final ConfigStringList REPAIR_MODE_SLOTS                  = new ConfigStringList("repairModeSlots", ImmutableList.of("mainhand", "offhand")).apply(LISTS_KEY);
        public static final ConfigStringList UNSTACKING_ITEMS                   = new ConfigStringList("unstackingItems", ImmutableList.of("minecraft:bucket", "minecraft:glass_bottle")).apply(LISTS_KEY);

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                BLOCK_TYPE_BREAK_RESTRICTION_LIST_TYPE,
                BLOCK_TYPE_BREAK_RESTRICTION_BLACKLIST,
                BLOCK_TYPE_BREAK_RESTRICTION_WHITELIST,
                //CREATIVE_EXTRA_ITEMS,
                ENTITY_TYPE_ATTACK_RESTRICTION_LIST_TYPE,
                ENTITY_TYPE_ATTACK_RESTRICTION_BLACKLIST,
                ENTITY_TYPE_ATTACK_RESTRICTION_WHITELIST,
                PREFER_SILK_TOUCH,
                ENTITY_WEAPON_MAPPING,
                FAST_PLACEMENT_ITEM_LIST_TYPE,
                FAST_RIGHT_CLICK_BLOCK_LIST_TYPE,
                FAST_RIGHT_CLICK_ITEM_LIST_TYPE,
                POTION_WARNING_LIST_TYPE,
                FAST_PLACEMENT_ITEM_BLACKLIST,
                FAST_PLACEMENT_ITEM_WHITELIST,
                FAST_RIGHT_CLICK_BLOCK_BLACKLIST,
                FAST_RIGHT_CLICK_BLOCK_WHITELIST,
                FAST_RIGHT_CLICK_ITEM_BLACKLIST,
                FAST_RIGHT_CLICK_ITEM_WHITELIST,
                //FLAT_WORLD_PRESETS,
                HAND_RESTOCK_LIST_TYPE,
                HAND_RESTOCK_BLACKLIST,
                HAND_RESTOCK_WHITELIST,
                POTION_WARNING_BLACKLIST,
                POTION_WARNING_WHITELIST,
                REPAIR_MODE_SLOTS,
                UNSTACKING_ITEMS
        );
    }

    public static class Disable
    {
        public static final ConfigBooleanHotkeyed       DISABLE_ARMOR_STAND_RENDERING   = new ConfigBooleanHotkeyed("disableArmorStandRendering",           false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_AXE_STRIPPING           = new ConfigBooleanHotkeyed("disableAxeStripping",                  false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BAT_SPAWNING            = new ConfigBooleanClient  ("disableBatSpawning",                   false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BEACON_BEAM_RENDERING   = new ConfigBooleanHotkeyed("disableBeaconBeamRendering",           false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BLOCK_BREAK_PARTICLES   = new ConfigBooleanHotkeyed("disableBlockBreakingParticles",        false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BLOCK_BREAK_COOLDOWN    = new ConfigBooleanHotkeyed("disableBlockBreakCooldown",            false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_DOUBLE_TAP_SPRINT       = new ConfigBooleanHotkeyed("disableDoubleTapSprint",               false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BOSS_BAR                = new ConfigBooleanHotkeyed("disableBossBar",                       false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BOSS_FOG                = new ConfigBooleanHotkeyed("disableBossFog",                       false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_CHUNK_RENDERING         = new ConfigBooleanHotkeyed("disableChunkRendering",                false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_CLIENT_ENTITY_UPDATES   = new ConfigBooleanHotkeyed("disableClientEntityUpdates",           false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_CLIENT_LIGHT_UPDATES    = new ConfigBooleanHotkeyed("disableClientLightUpdates",            false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_CONSTANT_CHUNK_SAVING   = new ConfigBooleanHotkeyed("disableConstantChunkSaving",           false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_CREATIVE_INFESTED_BLOCKS= new ConfigBooleanHotkeyed("disableCreativeMenuInfestedBlocks",    false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_DEAD_MOB_RENDERING      = new ConfigBooleanHotkeyed("disableDeadMobRendering",              false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_DEAD_MOB_TARGETING      = new ConfigBooleanHotkeyed("disableDeadMobTargeting",              false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_ENTITY_RENDERING        = new ConfigBooleanHotkeyed("disableEntityRendering",               false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_ENTITY_TICKING          = new ConfigBooleanClient  ("disableEntityTicking",                 false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_FALLING_BLOCK_RENDER    = new ConfigBooleanHotkeyed("disableFallingBlockEntityRendering",   false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_FP_EFFECT_PARTICLES     = new ConfigBooleanHotkeyed("disableFirstPersonEffectParticles",    false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_INVENTORY_EFFECTS       = new ConfigBooleanHotkeyed("disableInventoryEffectRendering",      false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_ITEM_SWITCH_COOLDOWN    = new ConfigBooleanHotkeyed("disableItemSwitchRenderCooldown",      false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_MOB_SPAWNER_MOB_RENDER  = new ConfigBooleanHotkeyed("disableMobSpawnerMobRendering",        false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_NAUSEA_EFFECT           = new ConfigBooleanHotkeyed("disableNauseaEffect",                  false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_NETHER_FOG              = new ConfigBooleanHotkeyed("disableNetherFog",                     false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_NETHER_PORTAL_SOUND     = new ConfigBooleanHotkeyed("disableNetherPortalSound",             false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_OBSERVER                = new ConfigBooleanClient  ("disableObserver",                      false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_OFFHAND_RENDERING       = new ConfigBooleanHotkeyed("disableOffhandRendering",              false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_PARTICLES               = new ConfigBooleanHotkeyed("disableParticles",                     false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_PORTAL_GUI_CLOSING      = new ConfigBooleanHotkeyed("disablePortalGuiClosing",              false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_RAIN_EFFECTS            = new ConfigBooleanHotkeyed("disableRainEffects",                   false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_RENDERING_SCAFFOLDING   = new ConfigBooleanHotkeyed("disableRenderingScaffolding",          false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_RENDER_DISTANCE_FOG     = new ConfigBooleanHotkeyed("disableRenderDistanceFog",             false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_SCOREBOARD_RENDERING    = new ConfigBooleanHotkeyed("disableScoreboardRendering",           false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_SHULKER_BOX_TOOLTIP     = new ConfigBooleanHotkeyed("disableShulkerBoxTooltip",             false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_SHOVEL_PATHING          = new ConfigBooleanHotkeyed("disableShovelPathing",                 false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_SIGN_GUI                = new ConfigBooleanHotkeyed("disableSignGui",                       false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_SKY_DARKNESS            = new ConfigBooleanHotkeyed("disableSkyDarkness",                   false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_SLIME_BLOCK_SLOWDOWN    = new ConfigBooleanHotkeyed("disableSlimeBlockSlowdown",            false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_STATUS_EFFECT_HUD       = new ConfigBooleanHotkeyed("disableStatusEffectHud",               false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_TILE_ENTITY_RENDERING   = new ConfigBooleanHotkeyed("disableTileEntityRendering",           false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_TILE_ENTITY_TICKING     = new ConfigBooleanClient  ("disableTileEntityTicking",             false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_VILLAGER_TRADE_LOCKING  = new ConfigBooleanClient  ("disableVillagerTradeLocking",          false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_WALL_UNSPRINT           = new ConfigBooleanHotkeyed("disableWallUnsprint",                  false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_WORLD_VIEW_BOB          = new ConfigBooleanHotkeyed("disableWorldViewBob",                  false, "").apply(DISABLE_KEY);

        public static final ImmutableList<IHotkeyTogglable> OPTIONS = ImmutableList.of(
                DISABLE_ARMOR_STAND_RENDERING,
                DISABLE_AXE_STRIPPING,
                DISABLE_BAT_SPAWNING,
                DISABLE_BEACON_BEAM_RENDERING,
                DISABLE_BLOCK_BREAK_PARTICLES,
                DISABLE_BLOCK_BREAK_COOLDOWN,
                DISABLE_DOUBLE_TAP_SPRINT,
                DISABLE_BOSS_BAR,
                DISABLE_BOSS_FOG,
                DISABLE_CHUNK_RENDERING,
                DISABLE_CLIENT_ENTITY_UPDATES,
                DISABLE_CLIENT_LIGHT_UPDATES,
                DISABLE_CONSTANT_CHUNK_SAVING,
                DISABLE_CREATIVE_INFESTED_BLOCKS,
                DISABLE_DEAD_MOB_RENDERING,
                DISABLE_DEAD_MOB_TARGETING,
                DISABLE_ENTITY_RENDERING,
                DISABLE_ENTITY_TICKING,
                DISABLE_FALLING_BLOCK_RENDER,
                DISABLE_FP_EFFECT_PARTICLES,
                DISABLE_INVENTORY_EFFECTS,
                DISABLE_ITEM_SWITCH_COOLDOWN,
                DISABLE_MOB_SPAWNER_MOB_RENDER,
                DISABLE_NAUSEA_EFFECT,
                DISABLE_NETHER_FOG,
                DISABLE_NETHER_PORTAL_SOUND,
                DISABLE_OBSERVER,
                DISABLE_OFFHAND_RENDERING,
                DISABLE_PARTICLES,
                DISABLE_PORTAL_GUI_CLOSING,
                DISABLE_RAIN_EFFECTS,
                DISABLE_RENDERING_SCAFFOLDING,
                DISABLE_RENDER_DISTANCE_FOG,
                DISABLE_SCOREBOARD_RENDERING,
                DISABLE_SHULKER_BOX_TOOLTIP,
                DISABLE_SHOVEL_PATHING,
                DISABLE_SIGN_GUI,
                DISABLE_SKY_DARKNESS,
                DISABLE_SLIME_BLOCK_SLOWDOWN,
                DISABLE_STATUS_EFFECT_HUD,
                DISABLE_TILE_ENTITY_RENDERING,
                DISABLE_TILE_ENTITY_TICKING,
                DISABLE_VILLAGER_TRADE_LOCKING,
                DISABLE_WALL_UNSPRINT,
                DISABLE_WORLD_VIEW_BOB
        );
    }

    public static class Internal
    {
        public static final ConfigInteger       FLY_SPEED_PRESET                    = new ConfigInteger     ("flySpeedPreset", 0, 0, 3).apply(INTERNAL_KEY);
        public static final ConfigDouble        GAMMA_VALUE_ORIGINAL                = new ConfigDouble      ("gammaValueOriginal", 0, 0, 1).apply(INTERNAL_KEY);
        public static final ConfigInteger       HOTBAR_SCROLL_CURRENT_ROW           = new ConfigInteger     ("hotbarScrollCurrentRow", 3, 0, 3).apply(INTERNAL_KEY);
        public static final ConfigDouble        SLIME_BLOCK_SLIPPERINESS_ORIGINAL   = new ConfigDouble      ("slimeBlockSlipperinessOriginal", 0.8, 0, 1).apply(INTERNAL_KEY);
        public static final ConfigDouble        SNAP_AIM_LAST_PITCH                 = new ConfigDouble      ("snapAimLastPitch", 0, -135, 135).apply(INTERNAL_KEY);
        public static final ConfigDouble        SNAP_AIM_LAST_YAW                   = new ConfigDouble      ("snapAimLastYaw", 0, 0, 360).apply(INTERNAL_KEY);

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                FLY_SPEED_PRESET,
                GAMMA_VALUE_ORIGINAL,
                HOTBAR_SCROLL_CURRENT_ROW,
                SLIME_BLOCK_SLIPPERINESS_ORIGINAL,
                SNAP_AIM_LAST_YAW
        );
    }

    public static ConfigDouble getActiveFlySpeedConfig()
    {
        return switch (Internal.FLY_SPEED_PRESET.getIntegerValue())
        {
            case 1 -> Generic.FLY_SPEED_PRESET_2;
            case 2 -> Generic.FLY_SPEED_PRESET_3;
            case 3 -> Generic.FLY_SPEED_PRESET_4;
            default -> Generic.FLY_SPEED_PRESET_1;
        };
    }

    public static void loadFromFile()
    {
        File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME);

        if (configFile.exists() && configFile.isFile() && configFile.canRead())
        {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject())
            {
                JsonObject root = element.getAsJsonObject();

                ConfigUtils.readConfigBase(root, "Fixes", Configs.Fixes.OPTIONS);
                ConfigUtils.readConfigBase(root, "Generic", Configs.Generic.OPTIONS);
                ConfigUtils.readConfigBase(root, "GenericHotkeys", Hotkeys.HOTKEY_LIST);
                ConfigUtils.readConfigBase(root, "Internal", Configs.Internal.OPTIONS);
                ConfigUtils.readConfigBase(root, "Lists", Configs.Lists.OPTIONS);
                ConfigUtils.readHotkeyToggleOptions(root, "DisableHotkeys", "DisableToggles", Disable.OPTIONS);
                ConfigUtils.readHotkeyToggleOptions(root, "TweakHotkeys", "TweakToggles", FeatureToggle.VALUES);
            }
        }

        // TODO 1.19.3+
        //CreativeExtraItems.setCreativeExtraItems(Lists.CREATIVE_EXTRA_ITEMS.getStrings());

        InventoryUtils.setToolSwitchableSlots(Generic.TOOL_SWITCHABLE_SLOTS.getStringValue());
        InventoryUtils.setToolSwitchIgnoreSlots(Generic.TOOL_SWITCH_IGNORED_SLOTS.getStringValue());
        InventoryUtils.setRepairModeSlots(Lists.REPAIR_MODE_SLOTS.getStrings());
        InventoryUtils.setUnstackingItems(Lists.UNSTACKING_ITEMS.getStrings());
        InventoryUtils.setWeaponMapping(Lists.ENTITY_WEAPON_MAPPING.getStrings());
        InventoryUtils.setPreferSilkTouchList(Lists.PREFER_SILK_TOUCH.getStrings());

        PlacementTweaks.BLOCK_TYPE_BREAK_RESTRICTION.setListType((ListType) Lists.BLOCK_TYPE_BREAK_RESTRICTION_LIST_TYPE.getOptionListValue());
        PlacementTweaks.BLOCK_TYPE_BREAK_RESTRICTION.setListContents(
                Lists.BLOCK_TYPE_BREAK_RESTRICTION_BLACKLIST.getStrings(),
                Lists.BLOCK_TYPE_BREAK_RESTRICTION_WHITELIST.getStrings());

        PlacementTweaks.FAST_RIGHT_CLICK_BLOCK_RESTRICTION.setListType((ListType) Lists.FAST_RIGHT_CLICK_BLOCK_LIST_TYPE.getOptionListValue());
        PlacementTweaks.FAST_RIGHT_CLICK_BLOCK_RESTRICTION.setListContents(
                Lists.FAST_RIGHT_CLICK_BLOCK_BLACKLIST.getStrings(),
                Lists.FAST_RIGHT_CLICK_BLOCK_WHITELIST.getStrings());

        PlacementTweaks.FAST_RIGHT_CLICK_ITEM_RESTRICTION.setListType((ListType) Lists.FAST_RIGHT_CLICK_ITEM_LIST_TYPE.getOptionListValue());
        PlacementTweaks.FAST_RIGHT_CLICK_ITEM_RESTRICTION.setListContents(
                Lists.FAST_RIGHT_CLICK_ITEM_BLACKLIST.getStrings(),
                Lists.FAST_RIGHT_CLICK_ITEM_WHITELIST.getStrings());

        PlacementTweaks.FAST_PLACEMENT_ITEM_RESTRICTION.setListType((ListType) Lists.FAST_PLACEMENT_ITEM_LIST_TYPE.getOptionListValue());
        PlacementTweaks.FAST_PLACEMENT_ITEM_RESTRICTION.setListContents(
                Lists.FAST_PLACEMENT_ITEM_BLACKLIST.getStrings(),
                Lists.FAST_PLACEMENT_ITEM_WHITELIST.getStrings());

        PlacementTweaks.HAND_RESTOCK_RESTRICTION.setListType((ListType) Lists.HAND_RESTOCK_LIST_TYPE.getOptionListValue());
        PlacementTweaks.HAND_RESTOCK_RESTRICTION.setListContents(
                Lists.HAND_RESTOCK_BLACKLIST.getStrings(),
                Lists.HAND_RESTOCK_WHITELIST.getStrings());

        MiscTweaks.POTION_RESTRICTION.setListType((ListType) Lists.POTION_WARNING_LIST_TYPE.getOptionListValue());
        MiscTweaks.POTION_RESTRICTION.setListContents(
                Lists.POTION_WARNING_BLACKLIST.getStrings(),
                Lists.POTION_WARNING_WHITELIST.getStrings());

        MiscTweaks.ENTITY_TYPE_ATTACK_RESTRICTION.setListType((ListType) Lists.ENTITY_TYPE_ATTACK_RESTRICTION_LIST_TYPE.getOptionListValue());
        MiscTweaks.ENTITY_TYPE_ATTACK_RESTRICTION.setListContents(
                Lists.ENTITY_TYPE_ATTACK_RESTRICTION_BLACKLIST.getStrings(),
                Lists.ENTITY_TYPE_ATTACK_RESTRICTION_WHITELIST.getStrings());

        if (MinecraftClient.getInstance().world == null)
        {
            // Turn off after loading the configs, just in case it was enabled in the config somehow.
            // But only if we are currently not in a world, since changing configs also re-loads them when closing the menu.
            FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(false);
        }
    }

    public static void saveToFile()
    {
        File dir = FileUtils.getConfigDirectory();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs())
        {
            JsonObject root = new JsonObject();

            ConfigUtils.writeConfigBase(root, "Fixes", Configs.Fixes.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Generic", Configs.Generic.OPTIONS);
            ConfigUtils.writeConfigBase(root, "GenericHotkeys", Hotkeys.HOTKEY_LIST);
            ConfigUtils.writeConfigBase(root, "Internal", Configs.Internal.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Lists", Configs.Lists.OPTIONS);
            ConfigUtils.writeHotkeyToggleOptions(root, "DisableHotkeys", "DisableToggles", Disable.OPTIONS);
            ConfigUtils.writeHotkeyToggleOptions(root, "TweakHotkeys", "TweakToggles", FeatureToggle.VALUES);

            JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load()
    {
        loadFromFile();
    }

    @Override
    public void save()
    {
        saveToFile();
    }
}
