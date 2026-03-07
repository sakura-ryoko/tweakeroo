package fi.dy.masa.tweakeroo.config;

import java.nio.file.Files;
import java.nio.file.Path;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;

import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.util.*;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction.ListType;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.data.CachedTagManager;
import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
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
        public static final ConfigOptionList    	ACCURATE_PLACEMENT_PROTOCOL_MODE    = new ConfigOptionList  ("accuratePlacementProtocolMode", EasyPlacementProtocol.AUTO).apply(GENERIC_KEY);
        public static final ConfigBoolean       	ACCURATE_PLACEMENT_PROTOCOL         = new ConfigBoolean     ("accuratePlacementProtocol", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	AFTER_CLICKER_CLICK_COUNT           = new ConfigInteger     ("afterClickerClickCount",  1, 1, 32).apply(GENERIC_KEY);
        public static final ConfigDouble        	ANGEL_BLOCK_PLACEMENT_DISTANCE      = new ConfigDouble      ("angelBlockPlacementDistance",  3, 1, 5).apply(GENERIC_KEY);
        public static final ConfigBoolean       	AREA_SELECTION_USE_ALL              = new ConfigBoolean     ("areaSelectionUseAll", false).apply(GENERIC_KEY);
        public static final ConfigDouble        	BLOCK_REACH_DISTANCE                = new ConfigDouble      ("blockReachDistance", 4.5, 1, 64).apply(GENERIC_KEY);
        public static final ConfigOptionList    	BLOCK_TYPE_BREAK_RESTRICTION_WARN   = new ConfigOptionList  ("blockTypeBreakRestrictionWarn", MessageOutputType.MESSAGE).apply(GENERIC_KEY);
        public static final ConfigInteger       	BREAKING_GRID_SIZE                  = new ConfigInteger     ("breakingGridSize", 3, 1, 1000).apply(GENERIC_KEY);
        public static final ConfigInteger       	RESTRICTION_LAYER_HEIGHT            = new ConfigInteger     ("restrictionLayerHeight", 1, -1000, 1000, "The layer height for the layer restriction mode.\nTo quickly adjust the value, scroll while\nholding down the tweak toggle keybind.");
        public static final ConfigOptionList    	BREAKING_RESTRICTION_MODE           = new ConfigOptionList  ("breakingRestrictionMode", PlacementRestrictionMode.LINE).apply(GENERIC_KEY);
        public static final ConfigBoolean       	BUNDLE_DISPLAY_BACKGROUND_COLOR     = new ConfigBoolean     ("bundleDisplayBgColor", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	BUNDLE_DISPLAY_REQUIRE_SHIFT        = new ConfigBoolean     ("bundleDisplayRequireShift", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	BUNDLE_DISPLAY_ROW_WIDTH            = new ConfigInteger     ("bundleDisplayRowWidth", 9, 6, 9).apply(GENERIC_KEY);
        public static final ConfigColor         	CHAT_BACKGROUND_COLOR               = new ConfigColor       ("chatBackgroundColor", "#80000000").apply(GENERIC_KEY);
        public static final ConfigString        	CHAT_TIME_FORMAT                    = new ConfigString      ("chatTimeFormat", "[HH:mm:ss]").apply(GENERIC_KEY);
        public static final ConfigBoolean       	CLIENT_PLACEMENT_ROTATION           = new ConfigBoolean     ("clientPlacementRotation", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	CLIENT_PLACEMENT_VALIDATION         = new ConfigBoolean     ("clientPlacementValidation", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	CUSTOM_INVENTORY_GUI_SCALE          = new ConfigInteger     ("customInventoryGuiScale", 2, 1, 10).apply(GENERIC_KEY);
        public static final ConfigDouble        	DARKNESS_SCALE_OVERRIDE_VALUE       = new ConfigDouble      ("darknessScaleOverrideValue", 0.45D, 0.0D, 1.00D).apply(GENERIC_KEY);
        public static final ConfigBoolean       	DEBUG_LOGGING                       = new ConfigBoolean     ("debugLogging", false).apply(GENERIC_KEY);
        public static final ConfigOptionList    	ELYTRA_CAMERA_INDICATOR             = new ConfigOptionList  ("elytraCameraIndicator", ActiveMode.WITH_KEY).apply(GENERIC_KEY);
	    public static final ConfigBooleanHotkeyed   ENTITY_DATA_SYNC                    = new ConfigBooleanHotkeyed ("entityDataSync", false, "").apply(GENERIC_KEY);
	    public static final ConfigBooleanHotkeyed   ENTITY_DATA_SYNC_BACKUP             = new ConfigBooleanHotkeyed ("entityDataSyncBackup", false, "").apply(GENERIC_KEY);
	    public static final ConfigFloat             ENTITY_DATA_SYNC_CACHE_REFRESH      = new ConfigFloat       ("entityDataSyncCacheRefresh", 0.25f, 0.05f, 1.0f).apply(GENERIC_KEY);
	    public static final ConfigFloat             ENTITY_DATA_SYNC_CACHE_TIMEOUT      = new ConfigFloat       ("entityDataSyncCacheTimeout", 2.75f, 1.0f, 25.0f).apply(GENERIC_KEY);
        public static final ConfigDouble        	ENTITY_REACH_DISTANCE               = new ConfigDouble      ("entityReachDistance", 3.0, 1, 64).apply(GENERIC_KEY);
        public static final ConfigOptionList    	ENTITY_TYPE_ATTACK_RESTRICTION_WARN = new ConfigOptionList  ("entityTypeAttackRestrictionWarn", MessageOutputType.MESSAGE).apply(GENERIC_KEY);
        public static final ConfigInteger       	FAST_BLOCK_PLACEMENT_COUNT          = new ConfigInteger     ("fastBlockPlacementCount", 2, 1, 16).apply(GENERIC_KEY);
        public static final ConfigBoolean       	FAST_LEFT_CLICK_ALLOW_TOOLS         = new ConfigBoolean     ("fastLeftClickAllowTools", false).apply(GENERIC_KEY);
        public static final ConfigInteger       	FAST_LEFT_CLICK_COUNT               = new ConfigInteger     ("fastLeftClickCount",  10, 1, 64).apply(GENERIC_KEY);
        public static final ConfigBoolean       	FAST_PLACEMENT_REMEMBER_ALWAYS      = new ConfigBoolean     ("fastPlacementRememberOrientation", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	FAST_RIGHT_CLICK_COUNT              = new ConfigInteger     ("fastRightClickCount", 10, 1, 64).apply(GENERIC_KEY);
        public static final ConfigInteger       	FILL_CLONE_LIMIT                    = new ConfigInteger     ("fillCloneLimit", 10000000, 1, 1000000000).apply(GENERIC_KEY);
        public static final ConfigColor         	FLEXIBLE_PLACEMENT_OVERLAY_COLOR    = new ConfigColor       ("flexibleBlockPlacementOverlayColor", "#C03030F0").apply(GENERIC_KEY);
        public static final ConfigDouble        	FLY_DECELERATION_FACTOR             = new ConfigDouble      ("flyDecelerationFactor", 0.4, 0.0, 1.0).apply(GENERIC_KEY);
        public static final ConfigDouble        	FLY_SPEED_PRESET_1                  = new ConfigDouble      ("flySpeedPreset1", 0.01, 0, 4).apply(GENERIC_KEY);
        public static final ConfigDouble        	FLY_SPEED_PRESET_2                  = new ConfigDouble      ("flySpeedPreset2", 0.064, 0, 4).apply(GENERIC_KEY);
        public static final ConfigDouble        	FLY_SPEED_PRESET_3                  = new ConfigDouble      ("flySpeedPreset3", 0.128, 0, 4).apply(GENERIC_KEY);
        public static final ConfigDouble        	FLY_SPEED_PRESET_4                  = new ConfigDouble      ("flySpeedPreset4", 0.32, 0, 4).apply(GENERIC_KEY);
        public static final ConfigDouble        	FLY_SPEED_INCREMENT_1               = new ConfigDouble      ("flySpeedIncrement1", 0.128, -4, 4).apply(GENERIC_KEY);
        public static final ConfigDouble        	FLY_SPEED_INCREMENT_2               = new ConfigDouble      ("flySpeedIncrement2", -0.128, -4, 4).apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed   FREE_CAMERA_PLAYER_INPUTS           = new ConfigBooleanHotkeyed ("freeCameraPlayerInputs", false, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed   FREE_CAMERA_PLAYER_MOVEMENT         = new ConfigBooleanHotkeyed ("freeCameraPlayerMovement", false, "").apply(GENERIC_KEY);
		public static final ConfigBooleanHotkeyed	FREE_CAMERA_SHOW_HANDS				= new ConfigBooleanHotkeyed ("freeCameraShowHands", false, "").apply(GENERIC_KEY);
		public static final ConfigBooleanHotkeyed	FREE_CAMERA_SHOW_HOTBAR				= new ConfigBooleanHotkeyed ("freeCameraShowHotBar", false, "").apply(GENERIC_KEY);
	    public static final ConfigBooleanHotkeyed	FREE_CAMERA_SHOW_STATUS_BARS    	= new ConfigBooleanHotkeyed ("freeCameraShowStatusBars", false, "").apply(GENERIC_KEY);
        public static final ConfigDouble        	GAMMA_OVERRIDE_VALUE                = new ConfigDouble      ("gammaOverrideValue", 16, 0, 32).apply(GENERIC_KEY);
        public static final ConfigBoolean       	HAND_RESTOCK_PRE                    = new ConfigBoolean     ("handRestockPre", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	HAND_RESTOCK_PRE_THRESHOLD          = new ConfigInteger     ("handRestockPreThreshold", 6, 1, 64).apply(GENERIC_KEY);
        public static final ConfigBoolean       	HANGABLE_ENTITY_BYPASS_INVERSE      = new ConfigBoolean     ("hangableEntityBypassInverse", false).apply(GENERIC_KEY);
        public static final ConfigInteger       	HOTBAR_SLOT_CYCLE_MAX               = new ConfigInteger     ("hotbarSlotCycleMax", 2, 1, 9).apply(GENERIC_KEY);
        public static final ConfigInteger       	HOTBAR_SLOT_RANDOMIZER_MAX          = new ConfigInteger     ("hotbarSlotRandomizerMax", 5, 1, 9).apply(GENERIC_KEY);
        public static final ConfigOptionList    	HOTBAR_SWAP_OVERLAY_ALIGNMENT       = new ConfigOptionList  ("hotbarSwapOverlayAlignment", HudAlignment.BOTTOM_RIGHT).apply(GENERIC_KEY);
        public static final ConfigInteger       	HOTBAR_SWAP_OVERLAY_OFFSET_X        = new ConfigInteger     ("hotbarSwapOverlayOffsetX", 4).apply(GENERIC_KEY);
        public static final ConfigInteger       	HOTBAR_SWAP_OVERLAY_OFFSET_Y        = new ConfigInteger     ("hotbarSwapOverlayOffsetY", 4).apply(GENERIC_KEY);
        public static final ConfigBoolean       	INVENTORY_PREVIEW_VILLAGER_BG_COLOR = new ConfigBoolean     ("inventoryPreviewVillagerBGColor", false).apply(GENERIC_KEY);
        public static final ConfigInteger       	ITEM_SWAP_DURABILITY_THRESHOLD      = new ConfigInteger     ("itemSwapDurabilityThreshold", 20, 5, 10000).apply(GENERIC_KEY);
        public static final ConfigBoolean       	ITEM_USE_PACKET_CHECK_BYPASS        = new ConfigBoolean     ("itemUsePacketCheckBypass", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	MAP_PREVIEW_REQUIRE_SHIFT           = new ConfigBoolean     ("mapPreviewRequireShift", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	MAP_PREVIEW_SIZE                    = new ConfigInteger     ("mapPreviewSize", 160, 16, 512).apply(GENERIC_KEY);
        public static final ConfigInteger       	PERIODIC_ATTACK_INTERVAL            = new ConfigInteger     ("periodicAttackInterval", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
//        public static final ConfigBoolean       	PERIODIC_ATTACK_RESET_ON_ACTIVATE   = new ConfigBoolean     ("periodicAttackResetIntervalOnActivate", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	PERIODIC_USE_INTERVAL               = new ConfigInteger     ("periodicUseInterval", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
//        public static final ConfigBoolean       	PERIODIC_USE_RESET_ON_ACTIVATE      = new ConfigBoolean     ("periodicUseResetIntervalOnActivate", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	PERIODIC_HOLD_ATTACK_DURATION       = new ConfigInteger     ("periodicHoldAttackDuration", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
        public static final ConfigInteger       	PERIODIC_HOLD_ATTACK_INTERVAL       = new ConfigInteger     ("periodicHoldAttackInterval", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
//        public static final ConfigBoolean       	PERIODIC_HOLD_ATTACK_RESET_ON_ACTIVATE= new ConfigBoolean   ("periodicHoldAttackResetIntervalOnActivate", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	PERIODIC_HOLD_USE_DURATION          = new ConfigInteger     ("periodicHoldUseDuration", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
        public static final ConfigInteger       	PERIODIC_HOLD_USE_INTERVAL          = new ConfigInteger     ("periodicHoldUseInterval", 20, 0, Integer.MAX_VALUE).apply(GENERIC_KEY);
//        public static final ConfigBoolean       	PERIODIC_HOLD_USE_RESET_ON_ACTIVATE = new ConfigBoolean     ("periodicHoldUseResetIntervalOnActivate", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	PERMANENT_SNEAK_ALLOW_IN_GUIS       = new ConfigBoolean     ("permanentSneakAllowInGUIs", false).apply(GENERIC_KEY);
        public static final ConfigInteger       	PLACEMENT_GRID_SIZE                 = new ConfigInteger     ("placementGridSize", 3, 1, 1000).apply(GENERIC_KEY);
        public static final ConfigInteger       	PLACEMENT_LIMIT                     = new ConfigInteger     ("placementLimit", 3, 1, 10000).apply(GENERIC_KEY);
        public static final ConfigOptionList    	PLACEMENT_RESTRICTION_MODE          = new ConfigOptionList  ("placementRestrictionMode", PlacementRestrictionMode.FACE).apply(GENERIC_KEY);
        public static final ConfigBoolean       	PLACEMENT_RESTRICTION_TIED_TO_FAST  = new ConfigBoolean     ("placementRestrictionTiedToFast", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	POTION_WARNING_BENEFICIAL_ONLY      = new ConfigBoolean     ("potionWarningBeneficialOnly", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	POTION_WARNING_THRESHOLD            = new ConfigInteger     ("potionWarningThreshold", 600, 1, 1000000).apply(GENERIC_KEY);
        public static final ConfigBoolean       	REMEMBER_FLEXIBLE                   = new ConfigBoolean     ("rememberFlexibleFromClick", true).apply(GENERIC_KEY);
        public static final ConfigInteger       	RENDER_LIMIT_ITEM                   = new ConfigInteger     ("renderLimitItem", -1, -1, 10000).apply(GENERIC_KEY);
        public static final ConfigInteger       	RENDER_LIMIT_XP_ORB                 = new ConfigInteger     ("renderLimitXPOrb", -1, -1, 10000).apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed  	ROCKET_SWAP_ALLOW_EXPLOSIONS        = new ConfigBooleanHotkeyed("rocketSwapAllowExplosions", false, "").apply(GENERIC_KEY);
        public static final ConfigInteger       	SCAFFOLD_PLACE_DISTANCE             = new ConfigInteger     ("scaffoldPlaceDistance", 5, 1, 20).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SCAFFOLD_PLACE_VANILLA              = new ConfigBoolean     ("scaffoldPlaceVanilla", false).apply(GENERIC_KEY);
        public static final ConfigInteger       	SCULK_SENSOR_PULSE_LENGTH           = new ConfigInteger     ("sculkSensorPulseLength", 40, 0, 10000).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SELECTIVE_BLOCKS_TRACK_PISTONS      = new ConfigBoolean     ("selectiveBlocksTrackPistons", false).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SELECTIVE_BLOCKS_HIDE_PARTICLES     = new ConfigBoolean     ("selectiveBlocksHideParticles", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SELECTIVE_BLOCKS_HIDE_ENTITIES      = new ConfigBoolean     ("selectiveBlocksHideEntities", false).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SELECTIVE_BLOCKS_NO_HIT             = new ConfigBoolean     ("selectiveBlocksNoHit", true).apply(GENERIC_KEY);
	    public static final ConfigInteger       	SERVER_NBT_REQUEST_RATE             = new ConfigInteger     ("serverNbtRequestRate", 2).apply(GENERIC_KEY);
		public static final ConfigBoolean       	SHULKER_DISPLAY_BACKGROUND_COLOR    = new ConfigBoolean     ("shulkerDisplayBgColor", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SHULKER_DISPLAY_ENDER_CHEST         = new ConfigBoolean     ("shulkerDisplayEnderChest", false).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SHULKER_DISPLAY_REQUIRE_SHIFT       = new ConfigBoolean     ("shulkerDisplayRequireShift", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SLOT_SYNC_WORKAROUND                = new ConfigBoolean     ("slotSyncWorkaround", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SLOT_SYNC_WORKAROUND_ALWAYS         = new ConfigBoolean     ("slotSyncWorkaroundAlways", false).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SNAP_AIM_INDICATOR                  = new ConfigBoolean     ("snapAimIndicator", true).apply(GENERIC_KEY);
        public static final ConfigColor         	SNAP_AIM_INDICATOR_COLOR            = new ConfigColor       ("snapAimIndicatorColor", "#603030FF").apply(GENERIC_KEY);
        public static final ConfigOptionList    	SNAP_AIM_MODE                       = new ConfigOptionList  ("snapAimMode", SnapAimMode.YAW).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SNAP_AIM_ONLY_CLOSE_TO_ANGLE        = new ConfigBoolean     ("snapAimOnlyCloseToAngle", true).apply(GENERIC_KEY);
        public static final ConfigBoolean       	SNAP_AIM_PITCH_OVERSHOOT            = new ConfigBoolean     ("snapAimPitchOvershoot", false).apply(GENERIC_KEY);
        public static final ConfigDouble        	SNAP_AIM_PITCH_STEP                 = new ConfigDouble      ("snapAimPitchStep", 12.5, 0, 90).apply(GENERIC_KEY);
        public static final ConfigDouble        	SNAP_AIM_THRESHOLD_PITCH            = new ConfigDouble      ("snapAimThresholdPitch", 1.5).apply(GENERIC_KEY);
        public static final ConfigDouble        	SNAP_AIM_THRESHOLD_YAW              = new ConfigDouble      ("snapAimThresholdYaw", 5.0).apply(GENERIC_KEY);
        public static final ConfigDouble        	SNAP_AIM_YAW_STEP                   = new ConfigDouble      ("snapAimYawStep", 45, 0, 360).apply(GENERIC_KEY);
        public static final ConfigInteger       	STRUCTURE_BLOCK_MAX_SIZE            = new ConfigInteger     ("structureBlockMaxSize", 128, 1, 256).apply(GENERIC_KEY);
        public static final ConfigString        	TOOL_SWITCHABLE_SLOTS               = new ConfigString      ("toolSwitchableSlots", "1-9").apply(GENERIC_KEY);
        public static final ConfigString        	TOOL_SWITCH_IGNORED_SLOTS           = new ConfigString      ("toolSwitchIgnoredSlots", "").apply(GENERIC_KEY);
	    public static final ConfigBooleanHotkeyed	TOOL_SWAP_ALLOW_UNENCHANTED_TO_BREAK= new ConfigBooleanHotkeyed ("toolSwapAllowUnenchantedToBreak", false, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_BETTER_ENCHANTS			= new ConfigBooleanHotkeyed ("toolSwapBetterEnchants", true, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_PREFER_FORTUNE_OVERRIDE	= new ConfigBooleanHotkeyed ("toolSwapPreferFortuneOverride", false, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_PREFER_SILK_TOUCH			= new ConfigBooleanHotkeyed ("toolSwapPreferSilkTouch", true, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_BAMBOO_USES_SWORD_FIRST	= new ConfigBooleanHotkeyed ("toolSwapBambooUsesSwordFirst", true, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_LEAVES_USES_HOE_FIRST	    = new ConfigBooleanHotkeyed ("toolSwapLeavesUsesHoeFirst", false, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_NEEDS_SHEARS_FIRST		= new ConfigBooleanHotkeyed ("toolSwapNeedsShearsFirst", true, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_NEEDS_PICKAXE_FIRST		= new ConfigBooleanHotkeyed ("toolSwapNeedsPickaxeFirst", false, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_SILK_TOUCH_FIRST			= new ConfigBooleanHotkeyed ("toolSwapSilkTouchFirst", true, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_SILK_TOUCH_ORES			= new ConfigBooleanHotkeyed ("toolSwapSilkTouchOres", false, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_SILK_TOUCH_OVERRIDE		= new ConfigBooleanHotkeyed ("toolSwapSilkTouchOverride", false, "").apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	TOOL_SWAP_PICKAXE_OVERRIDE		    = new ConfigBooleanHotkeyed ("toolSwapPickaxeOverride", false, "").apply(GENERIC_KEY);
        public static final ConfigOptionList    	UTILITY_HAND_SLOT                   = new ConfigOptionList  ("utilityHandSlot", HandSlot.MAIN_HAND).apply(GENERIC_KEY);
        public static final ConfigBooleanHotkeyed	WEAPON_SWAP_BETTER_ENCHANTS			= new ConfigBooleanHotkeyed ("weaponSwapBetterEnchants", true, "").apply(GENERIC_KEY);
        public static final ConfigBoolean			ZOOM_ADJUST_MOUSE_SENSITIVITY       = new ConfigBoolean     ("zoomAdjustMouseSensitivity", true).apply(GENERIC_KEY);
        public static final ConfigDouble			ZOOM_FOV                            = new ConfigDouble      ("zoomFov", 30, 0.01, 359.99).apply(GENERIC_KEY);
        public static final ConfigDouble			ZOOM_FOV_DIFFERENCE                 = new ConfigDouble      ("zoomFovDifference", 1, 0.01, 359.99).apply(GENERIC_KEY);
        public static final ConfigDouble            ZOOM_FOV_DIFFERENCE_CTRL            = new ConfigDouble      ("zoomFovDifferenceCtrl", 5, 0, 359.99).apply(GENERIC_KEY);
        public static final ConfigBoolean			ZOOM_RESET_FOV_ON_ACTIVATE          = new ConfigBoolean     ("zoomResetFovOnActivate", true).apply(GENERIC_KEY);

        public static final ImmutableList<@NotNull IConfigBase> OPTIONS = ImmutableList.of(
                ACCURATE_PLACEMENT_PROTOCOL_MODE,
                ACCURATE_PLACEMENT_PROTOCOL,
                BUNDLE_DISPLAY_BACKGROUND_COLOR,
                BUNDLE_DISPLAY_REQUIRE_SHIFT,
                BUNDLE_DISPLAY_ROW_WIDTH,
                CLIENT_PLACEMENT_ROTATION,
                CLIENT_PLACEMENT_VALIDATION,
                DEBUG_LOGGING,
                ENTITY_DATA_SYNC,
                ENTITY_DATA_SYNC_BACKUP,
                ENTITY_DATA_SYNC_CACHE_REFRESH,
                ENTITY_DATA_SYNC_CACHE_TIMEOUT,
                FAST_LEFT_CLICK_ALLOW_TOOLS,
                FAST_PLACEMENT_REMEMBER_ALWAYS,
                FREE_CAMERA_PLAYER_INPUTS,
                FREE_CAMERA_PLAYER_MOVEMENT,
				FREE_CAMERA_SHOW_HANDS,
				FREE_CAMERA_SHOW_HOTBAR,
                FREE_CAMERA_SHOW_STATUS_BARS,
                HAND_RESTOCK_PRE,
                HANGABLE_ENTITY_BYPASS_INVERSE,
                INVENTORY_PREVIEW_VILLAGER_BG_COLOR,
                ITEM_USE_PACKET_CHECK_BYPASS,
                MAP_PREVIEW_REQUIRE_SHIFT,
                PERMANENT_SNEAK_ALLOW_IN_GUIS,
                PLACEMENT_RESTRICTION_TIED_TO_FAST,
                POTION_WARNING_BENEFICIAL_ONLY,
                REMEMBER_FLEXIBLE,
                SERVER_NBT_REQUEST_RATE,
                SHULKER_DISPLAY_BACKGROUND_COLOR,
                SHULKER_DISPLAY_ENDER_CHEST,
                SHULKER_DISPLAY_REQUIRE_SHIFT,
                SLOT_SYNC_WORKAROUND,
                SLOT_SYNC_WORKAROUND_ALWAYS,
                SNAP_AIM_INDICATOR,
                SNAP_AIM_ONLY_CLOSE_TO_ANGLE,
                SNAP_AIM_PITCH_OVERSHOOT,
                UTILITY_HAND_SLOT,
                ZOOM_ADJUST_MOUSE_SENSITIVITY,

                AREA_SELECTION_USE_ALL,
                RESTRICTION_LAYER_HEIGHT,
                BLOCK_TYPE_BREAK_RESTRICTION_WARN,
                BREAKING_RESTRICTION_MODE,
                ELYTRA_CAMERA_INDICATOR,
                ENTITY_TYPE_ATTACK_RESTRICTION_WARN,
                PLACEMENT_RESTRICTION_MODE,
                HOTBAR_SWAP_OVERLAY_ALIGNMENT,
                ROCKET_SWAP_ALLOW_EXPLOSIONS,
                SELECTIVE_BLOCKS_TRACK_PISTONS,
                SELECTIVE_BLOCKS_HIDE_PARTICLES,
                SELECTIVE_BLOCKS_HIDE_ENTITIES,
                SELECTIVE_BLOCKS_NO_HIT,
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
                DARKNESS_SCALE_OVERRIDE_VALUE,
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
                FLY_SPEED_INCREMENT_1,
                FLY_SPEED_INCREMENT_2,
                GAMMA_OVERRIDE_VALUE,
                HAND_RESTOCK_PRE_THRESHOLD,
                HOTBAR_SLOT_CYCLE_MAX,
                HOTBAR_SLOT_RANDOMIZER_MAX,
                HOTBAR_SWAP_OVERLAY_OFFSET_X,
                HOTBAR_SWAP_OVERLAY_OFFSET_Y,
                ITEM_SWAP_DURABILITY_THRESHOLD,
                MAP_PREVIEW_SIZE,
                PERIODIC_ATTACK_INTERVAL,
//                PERIODIC_ATTACK_RESET_ON_ACTIVATE,
                PERIODIC_USE_INTERVAL,
//                PERIODIC_USE_RESET_ON_ACTIVATE,
                PERIODIC_HOLD_ATTACK_DURATION,
                PERIODIC_HOLD_ATTACK_INTERVAL,
//                PERIODIC_HOLD_ATTACK_RESET_ON_ACTIVATE,
                PERIODIC_HOLD_USE_DURATION,
                PERIODIC_HOLD_USE_INTERVAL,
//                PERIODIC_HOLD_USE_RESET_ON_ACTIVATE,
                PLACEMENT_GRID_SIZE,
                PLACEMENT_LIMIT,
                POTION_WARNING_THRESHOLD,
                RENDER_LIMIT_ITEM,
                RENDER_LIMIT_XP_ORB,
                SCAFFOLD_PLACE_DISTANCE,
                SCAFFOLD_PLACE_VANILLA,
                SCULK_SENSOR_PULSE_LENGTH,
                SNAP_AIM_PITCH_STEP,
                SNAP_AIM_THRESHOLD_PITCH,
                SNAP_AIM_THRESHOLD_YAW,
                SNAP_AIM_YAW_STEP,
                STRUCTURE_BLOCK_MAX_SIZE,
                TOOL_SWITCHABLE_SLOTS,
                TOOL_SWAP_ALLOW_UNENCHANTED_TO_BREAK,
                TOOL_SWITCH_IGNORED_SLOTS,
                TOOL_SWAP_BETTER_ENCHANTS,
                TOOL_SWAP_PREFER_FORTUNE_OVERRIDE,
                TOOL_SWAP_PREFER_SILK_TOUCH,
                TOOL_SWAP_BAMBOO_USES_SWORD_FIRST,
                TOOL_SWAP_LEAVES_USES_HOE_FIRST,
                TOOL_SWAP_NEEDS_SHEARS_FIRST,
                TOOL_SWAP_NEEDS_PICKAXE_FIRST,
                TOOL_SWAP_SILK_TOUCH_FIRST,
                TOOL_SWAP_SILK_TOUCH_ORES,
                TOOL_SWAP_SILK_TOUCH_OVERRIDE,
                TOOL_SWAP_PICKAXE_OVERRIDE,
                WEAPON_SWAP_BETTER_ENCHANTS,
                ZOOM_FOV,
                ZOOM_FOV_DIFFERENCE,
                ZOOM_FOV_DIFFERENCE_CTRL,
                ZOOM_RESET_FOV_ON_ACTIVATE
        );

        public static final ImmutableList<@NotNull IHotkey> HOTKEYS = ImmutableList.of(
		        ENTITY_DATA_SYNC,
		        ENTITY_DATA_SYNC_BACKUP,
				FREE_CAMERA_PLAYER_INPUTS,
				FREE_CAMERA_PLAYER_MOVEMENT,
				FREE_CAMERA_SHOW_HANDS,
				FREE_CAMERA_SHOW_HOTBAR,
                ROCKET_SWAP_ALLOW_EXPLOSIONS,
                TOOL_SWAP_BETTER_ENCHANTS,
                TOOL_SWAP_PREFER_FORTUNE_OVERRIDE,
                TOOL_SWAP_PREFER_SILK_TOUCH,
                TOOL_SWAP_BAMBOO_USES_SWORD_FIRST,
                TOOL_SWAP_LEAVES_USES_HOE_FIRST,
                TOOL_SWAP_NEEDS_SHEARS_FIRST,
                TOOL_SWAP_NEEDS_PICKAXE_FIRST,
                TOOL_SWAP_SILK_TOUCH_FIRST,
                TOOL_SWAP_SILK_TOUCH_ORES,
                TOOL_SWAP_SILK_TOUCH_OVERRIDE,
                TOOL_SWAP_PICKAXE_OVERRIDE,
                WEAPON_SWAP_BETTER_ENCHANTS
        );
    }

    public static class Fixes
    {
        public static final ConfigBoolean STACKABLE_SHULKERS_IN_HOPPER_FIX  = new ConfigBoolean("stackableShulkersInHopperFix", true).apply(FIXES_KEY);
        //public static final ConfigBoolean ELYTRA_FIX                        = new ConfigBoolean("elytraFix", false).apply(FIXES_KEY);
        public static final ConfigBoolean MAC_HORIZONTAL_SCROLL             = new ConfigBoolean("macHorizontalScroll", false).apply(FIXES_KEY);
        public static final ConfigBoolean RAVAGER_CLIENT_BLOCK_BREAK_FIX    = new ConfigBoolean("ravagerClientBlockBreakFix", false).apply(FIXES_KEY);

        public static final ImmutableList<@NotNull IConfigBase> OPTIONS = ImmutableList.of(
                STACKABLE_SHULKERS_IN_HOPPER_FIX,
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
        public static final ConfigStringList ENTITY_WEAPON_MAPPING              = new ConfigStringList("entityWeaponMapping", ImmutableList.of("<default> => minecraft:mace, minecraft:netherite_sword, minecraft:diamond_sword, minecraft:iron_sword, minecraft:copper_sword, minecraft:golden_sword, minecraft:stone_sword, minecraft:wooden_sword", "minecraft:end_crystal, minecraft:item_frame, minecraft:glow_item_frame, minecraft:leash_knot => <ignore>", "minecraft:minecart, minecraft:chest_minecart, minecraft:furnace_minecart, minecraft:hopper_minecart, minecraft:hopper_minecart, minecraft:spawner_minecart, minecraft:tnt_minecart, minecraft:oak_boat, minecraft:oak_chest_boat, minecraft:spruce_boat, minecraft:spruce_chest_boat, minecraft:birch_boat, minecraft:birch_chest_boat, minecraft:jungle_boat, minecraft:jungle_chest_boat, minecraft:acacia_boat, minecraft:acacia_chest_boat, minecraft:cherry_boat, minecraft:cherry_chest_boat, minecraft:dark_oak_boat, minecraft:dark_oak_chest_boat, minecraft:pale_oak_boat, minecraft:pale_oak_chest_boat, minecraft:mangrove_boat, minecraft:mangrove_chest_boat, minecraft:bamboo_raft, minecraft:bamboo_chest_raft=> minecraft:mace, minecraft:netherite_axe, minecraft:diamond_axe, minecraft:iron_axe, minecraft:copper_axe, minecraft:golden_axe, minecraft:stone_axe, minecraft:wooden_axe")).apply(LISTS_KEY);
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
        public static final ConfigStringList PICKAXE_OVERRIDE                   = new ConfigStringList("pickaxeOverride", ImmutableList.of()).apply(LISTS_KEY);
        public static final ConfigOptionList POTION_WARNING_LIST_TYPE           = new ConfigOptionList("potionWarningListType", ListType.NONE).apply(LISTS_KEY);
        public static final ConfigStringList POTION_WARNING_BLACKLIST           = new ConfigStringList("potionWarningBlackList", ImmutableList.of("minecraft:hunger", "minecraft:mining_fatigue", "minecraft:nausea", "minecraft:poison", "minecraft:slowness", "minecraft:weakness")).apply(LISTS_KEY);
        public static final ConfigStringList POTION_WARNING_WHITELIST           = new ConfigStringList("potionWarningWhiteList", ImmutableList.of("minecraft:fire_resistance", "minecraft:invisibility", "minecraft:water_breathing")).apply(LISTS_KEY);
        public static final ConfigStringList REPAIR_MODE_SLOTS                  = new ConfigStringList("repairModeSlots", ImmutableList.of("mainhand", "offhand")).apply(LISTS_KEY);
        public static final ConfigOptionList SELECTIVE_BLOCKS_LIST_TYPE         = new ConfigOptionList("selectiveBlocksListType", ListType.NONE).apply(LISTS_KEY);
        public static final ConfigString     SELECTIVE_BLOCKS_WHITELIST         = new ConfigString("selectiveBlocksWhitelist", "").apply(LISTS_KEY);
        public static final ConfigString     SELECTIVE_BLOCKS_BLACKLIST         = new ConfigString("selectiveBlocksBlacklist", "").apply(LISTS_KEY);
        public static final ConfigStringList SILK_TOUCH_OVERRIDE                = new ConfigStringList("silkTouchOverride", ImmutableList.of()).apply(LISTS_KEY);
        public static final ConfigStringList UNSTACKING_ITEMS                   = new ConfigStringList("unstackingItems", ImmutableList.of("minecraft:bucket", "minecraft:glass_bottle")).apply(LISTS_KEY);

        public static final ImmutableList<@NotNull IConfigBase> OPTIONS = ImmutableList.of(
                BLOCK_TYPE_BREAK_RESTRICTION_LIST_TYPE,
                BLOCK_TYPE_BREAK_RESTRICTION_BLACKLIST,
                BLOCK_TYPE_BREAK_RESTRICTION_WHITELIST,
                //CREATIVE_EXTRA_ITEMS,
                ENTITY_TYPE_ATTACK_RESTRICTION_LIST_TYPE,
                ENTITY_TYPE_ATTACK_RESTRICTION_BLACKLIST,
                ENTITY_TYPE_ATTACK_RESTRICTION_WHITELIST,
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
                PICKAXE_OVERRIDE,
                POTION_WARNING_BLACKLIST,
                POTION_WARNING_WHITELIST,
                REPAIR_MODE_SLOTS,
                SILK_TOUCH_OVERRIDE,
                UNSTACKING_ITEMS,
                SELECTIVE_BLOCKS_LIST_TYPE,
                SELECTIVE_BLOCKS_WHITELIST,
                SELECTIVE_BLOCKS_BLACKLIST
        );
    }

    public static class Disable
    {
        public static final ConfigBooleanHotkeyed       DISABLE_ARMOR_STAND_RENDERING   = new ConfigBooleanHotkeyed("disableArmorStandRendering",           false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_ATMOSPHERIC_FOG         = new ConfigBooleanHotkeyed("disableAtmosphericFog",                false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_AXE_STRIPPING           = new ConfigBooleanHotkeyed("disableAxeStripping",                  false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BAT_SPAWNING            = new ConfigBooleanClient  ("disableBatSpawning",                   false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BEACON_BEAM_RENDERING   = new ConfigBooleanHotkeyed("disableBeaconBeamRendering",           false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BLOCK_BREAK_PARTICLES   = new ConfigBooleanHotkeyed("disableBlockBreakingParticles",        false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_BLOCK_BREAK_COOLDOWN    = new ConfigBooleanHotkeyed("disableBlockBreakCooldown",            false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_CLIENT_BLOCK_EVENTS     = new ConfigBooleanHotkeyed("disableClientBlockEvents",             false, "").apply(DISABLE_KEY);
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
        public static final ConfigBooleanHotkeyed       DISABLE_FIRE_OVERLAY            = new ConfigBooleanHotkeyed("disableFireOverlay",                   false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_FP_EFFECT_PARTICLES     = new ConfigBooleanHotkeyed("disableFirstPersonEffectParticles",    false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_FREEZE_OVERLAY          = new ConfigBooleanHotkeyed("disableFreezeOverlay",                 false, "").apply(DISABLE_KEY);
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
        public static final ConfigBooleanHotkeyed       DISABLE_TICKRATE_PLAYER_SLOWDOWN= new ConfigBooleanHotkeyed("disableTickRatePlayerSlowdown",        false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_TILE_ENTITY_RENDERING   = new ConfigBooleanHotkeyed("disableTileEntityRendering",           false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_TILE_ENTITY_TICKING     = new ConfigBooleanClient  ("disableTileEntityTicking",             false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_VILLAGER_TRADE_LOCKING  = new ConfigBooleanClient  ("disableVillagerTradeLocking",          false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_WALL_UNSPRINT           = new ConfigBooleanHotkeyed("disableWallUnsprint",                  false, "").apply(DISABLE_KEY);
        public static final ConfigBooleanHotkeyed       DISABLE_WORLD_VIEW_BOB          = new ConfigBooleanHotkeyed("disableWorldViewBob",                  false, "").apply(DISABLE_KEY);

        public static final ImmutableList<@NotNull IHotkeyTogglable> OPTIONS = ImmutableList.of(
                DISABLE_ARMOR_STAND_RENDERING,
                DISABLE_ATMOSPHERIC_FOG,
                DISABLE_AXE_STRIPPING,
                DISABLE_BAT_SPAWNING,
                DISABLE_BEACON_BEAM_RENDERING,
                DISABLE_BLOCK_BREAK_PARTICLES,
                DISABLE_BLOCK_BREAK_COOLDOWN,
                DISABLE_CLIENT_BLOCK_EVENTS,
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
                DISABLE_FIRE_OVERLAY,
                DISABLE_FP_EFFECT_PARTICLES,
                DISABLE_FREEZE_OVERLAY,
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
                DISABLE_TICKRATE_PLAYER_SLOWDOWN,
                DISABLE_TILE_ENTITY_RENDERING,
                DISABLE_TILE_ENTITY_TICKING,
                DISABLE_VILLAGER_TRADE_LOCKING,
                DISABLE_WALL_UNSPRINT,
                DISABLE_WORLD_VIEW_BOB
        );
    }

    public static class Internal
    {
        public static final ConfigDouble        DARKNESS_SCALE_VALUE_ORIGINAL       = new ConfigDouble      ("darknessScaleValueOriginal", 0.0D, 0.0D, 1.0D).apply(INTERNAL_KEY);
        public static final ConfigInteger       FLY_SPEED_PRESET                    = new ConfigInteger     ("flySpeedPreset", 0, 0, 3).apply(INTERNAL_KEY);
        public static final ConfigDouble        GAMMA_VALUE_ORIGINAL                = new ConfigDouble      ("gammaValueOriginal", 0, 0, 1).apply(INTERNAL_KEY);
        public static final ConfigInteger       HOTBAR_SCROLL_CURRENT_ROW           = new ConfigInteger     ("hotbarScrollCurrentRow", 3, 0, 3).apply(INTERNAL_KEY);
        public static final ConfigDouble        SLIME_BLOCK_SLIPPERINESS_ORIGINAL   = new ConfigDouble      ("slimeBlockSlipperinessOriginal", 0.8, 0, 1).apply(INTERNAL_KEY);
//        public static final ConfigDouble        SNAP_AIM_LAST_PITCH                 = new ConfigDouble      ("snapAimLastPitch", 0, -135, 135).apply(INTERNAL_KEY);
//        public static final ConfigDouble        SNAP_AIM_LAST_YAW                   = new ConfigDouble      ("snapAimLastYaw", 0, 0, 360).apply(INTERNAL_KEY);
        public static final ConfigInteger       SHULKER_MAX_STACK_SIZE              = new ConfigInteger     ("shulkerMaxStackSize", 64, 1, 99).apply(INTERNAL_KEY);
        public static final ConfigBoolean       SILENCE_NIGHT_VISION_WARN           = new ConfigBoolean     ("silenceNightVisionWarning", false);

        public static final ImmutableList<@NotNull IConfigBase> OPTIONS = ImmutableList.of(
                DARKNESS_SCALE_VALUE_ORIGINAL,
                FLY_SPEED_PRESET,
                GAMMA_VALUE_ORIGINAL,
                HOTBAR_SCROLL_CURRENT_ROW,
                SLIME_BLOCK_SLIPPERINESS_ORIGINAL,
                SHULKER_MAX_STACK_SIZE,
                SILENCE_NIGHT_VISION_WARN
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
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);

        if (Files.exists(configFile) && Files.isReadable(configFile))
        {
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);

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
                //Tweakeroo.debugLog("loadFromFile(): Successfully loaded config file '{}'.", configFile.toAbsolutePath());
            }
        }
        else
        {
            Tweakeroo.LOGGER.error("loadFromFile(): Failed to load config file '{}'.", configFile.toAbsolutePath());
        }

        // TODO 1.19.3+
        //CreativeExtraItems.setCreativeExtraItems(Lists.CREATIVE_EXTRA_ITEMS.getStrings());

        InventoryUtils.setToolSwitchableSlots(Generic.TOOL_SWITCHABLE_SLOTS.getStringValue());
        InventoryUtils.setToolSwitchIgnoreSlots(Generic.TOOL_SWITCH_IGNORED_SLOTS.getStringValue());
        CachedTagManager.parseSilkTouchOverride(Lists.SILK_TOUCH_OVERRIDE.getStrings());
        CachedTagManager.parsePickaxeOverride(Lists.PICKAXE_OVERRIDE.getStrings());
        InventoryUtils.setRepairModeSlots(Lists.REPAIR_MODE_SLOTS.getStrings());
        InventoryUtils.setUnstackingItems(Lists.UNSTACKING_ITEMS.getStrings());
        InventoryUtils.setWeaponMapping(Lists.ENTITY_WEAPON_MAPPING.getStrings());

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

        RenderTweaks.rebuildLists();

        MiscTweaks.ENTITY_TYPE_ATTACK_RESTRICTION.setListType((ListType) Lists.ENTITY_TYPE_ATTACK_RESTRICTION_LIST_TYPE.getOptionListValue());
        MiscTweaks.ENTITY_TYPE_ATTACK_RESTRICTION.setListContents(
                Lists.ENTITY_TYPE_ATTACK_RESTRICTION_BLACKLIST.getStrings(),
                Lists.ENTITY_TYPE_ATTACK_RESTRICTION_WHITELIST.getStrings());

        if (Minecraft.getInstance().level == null)
        {
            // Turn off after loading the configs, just in case it was enabled in the config somehow.
            // But only if we are currently not in a world, since changing configs also re-loads them when closing the menu.
            FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(false);
        }
    }

    public static void saveToFile()
    {
        Path dir = FileUtils.getConfigDirectoryAsPath();

        if (!Files.exists(dir))
        {
            FileUtils.createDirectoriesIfMissing(dir);
            //Tweakeroo.debugLog("saveToFile(): Creating directory '{}'.", dir.toAbsolutePath());
        }

        if (Files.isDirectory(dir))
        {
            JsonObject root = new JsonObject();

            ConfigUtils.writeConfigBase(root, "Fixes", Configs.Fixes.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Generic", Configs.Generic.OPTIONS);
            ConfigUtils.writeConfigBase(root, "GenericHotkeys", Hotkeys.HOTKEY_LIST);
            ConfigUtils.writeConfigBase(root, "Internal", Configs.Internal.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Lists", Configs.Lists.OPTIONS);
            ConfigUtils.writeHotkeyToggleOptions(root, "DisableHotkeys", "DisableToggles", Disable.OPTIONS);
            ConfigUtils.writeHotkeyToggleOptions(root, "TweakHotkeys", "TweakToggles", FeatureToggle.VALUES);

            JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
        }
        else
        {
            Tweakeroo.LOGGER.error("saveToFile(): Config Folder '{}' does not exist!", dir.toAbsolutePath());
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
