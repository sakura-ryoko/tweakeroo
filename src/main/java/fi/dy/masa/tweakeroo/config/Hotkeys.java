package fi.dy.masa.tweakeroo.config;

import java.util.List;
import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.hotkeys.KeybindSettings.Context;
import fi.dy.masa.tweakeroo.Reference;

public class Hotkeys
{
    private static final String HOTKEY_KEY = Reference.MOD_ID+".config.hotkey";

    public static final ConfigHotkey ACCURATE_BLOCK_PLACEMENT_IN        = new ConfigHotkey("accurateBlockPlacementInto",        "", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey ACCURATE_BLOCK_PLACEMENT_REVERSE   = new ConfigHotkey("accurateBlockPlacementReverse",     "", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey BREAKING_RESTRICTION_MODE_COLUMN   = new ConfigHotkey("breakingRestrictionModeColumn",     "").apply(HOTKEY_KEY);
    public static final ConfigHotkey BREAKING_RESTRICTION_MODE_DIAGONAL = new ConfigHotkey("breakingRestrictionModeDiagonal",   "").apply(HOTKEY_KEY);
    public static final ConfigHotkey BREAKING_RESTRICTION_MODE_FACE     = new ConfigHotkey("breakingRestrictionModeFace",       "").apply(HOTKEY_KEY);
    public static final ConfigHotkey BREAKING_RESTRICTION_MODE_LAYER    = new ConfigHotkey("breakingRestrictionModeLayer",      "").apply(HOTKEY_KEY);
    public static final ConfigHotkey BREAKING_RESTRICTION_MODE_LINE     = new ConfigHotkey("breakingRestrictionModeLine",       "").apply(HOTKEY_KEY);
    public static final ConfigHotkey BREAKING_RESTRICTION_MODE_PLANE    = new ConfigHotkey("breakingRestrictionModePlane",      "").apply(HOTKEY_KEY);
    public static final ConfigHotkey COPY_SIGN_TEXT                     = new ConfigHotkey("copySignText",                      "").apply(HOTKEY_KEY);
    public static final ConfigHotkey ELYTRA_CAMERA                      = new ConfigHotkey("elytraCamera",                      "").apply(HOTKEY_KEY);
    public static final ConfigHotkey FLEXIBLE_BLOCK_PLACEMENT_ADJACENT  = new ConfigHotkey("flexibleBlockPlacementAdjacent",    "", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey FLEXIBLE_BLOCK_PLACEMENT_OFFSET    = new ConfigHotkey("flexibleBlockPlacementOffset",      "LEFT_CONTROL", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey FLEXIBLE_BLOCK_PLACEMENT_ROTATION  = new ConfigHotkey("flexibleBlockPlacementRotation",    "LEFT_ALT", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey FLY_PRESET_1                       = new ConfigHotkey("flyPreset1",                        "").apply(HOTKEY_KEY);
    public static final ConfigHotkey FLY_PRESET_2                       = new ConfigHotkey("flyPreset2",                        "").apply(HOTKEY_KEY);
    public static final ConfigHotkey FLY_PRESET_3                       = new ConfigHotkey("flyPreset3",                        "").apply(HOTKEY_KEY);
    public static final ConfigHotkey FLY_PRESET_4                       = new ConfigHotkey("flyPreset4",                        "").apply(HOTKEY_KEY);
    public static final ConfigHotkey FREE_CAMERA_PLAYER_INPUTS          = new ConfigHotkey("freeCameraPlayerInputs",            "").apply(HOTKEY_KEY);
    public static final ConfigHotkey FREE_CAMERA_PLAYER_MOVEMENT        = new ConfigHotkey("freeCameraPlayerMovement",          "").apply(HOTKEY_KEY);
    public static final ConfigHotkey HOTBAR_SCROLL                      = new ConfigHotkey("hotbarScroll",                      "", KeybindSettings.RELEASE_ALLOW_EXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey HOTBAR_SWAP_BASE                   = new ConfigHotkey("hotbarSwapBase",                    "", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey HOTBAR_SWAP_1                      = new ConfigHotkey("hotbarSwap1",                       "").apply(HOTKEY_KEY);
    public static final ConfigHotkey HOTBAR_SWAP_2                      = new ConfigHotkey("hotbarSwap2",                       "").apply(HOTKEY_KEY);
    public static final ConfigHotkey HOTBAR_SWAP_3                      = new ConfigHotkey("hotbarSwap3",                       "").apply(HOTKEY_KEY);
    public static final ConfigHotkey INVENTORY_PREVIEW                  = new ConfigHotkey("inventoryPreview",                  "LEFT_ALT", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey OPEN_CONFIG_GUI                    = new ConfigHotkey("openConfigGui",                     "X,C").apply(HOTKEY_KEY);
    public static final ConfigHotkey PLACEMENT_Y_MIRROR                 = new ConfigHotkey("placementYMirror",                  "", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey PLAYER_INVENTORY_PEEK              = new ConfigHotkey("playerInventoryPeek",               "", KeybindSettings.PRESS_ALLOWEXTRA).apply(HOTKEY_KEY);
    public static final ConfigHotkey PLACEMENT_RESTRICTION_MODE_COLUMN  = new ConfigHotkey("placementRestrictionModeColumn",    "Z,3").apply(HOTKEY_KEY);
    public static final ConfigHotkey PLACEMENT_RESTRICTION_MODE_DIAGONAL= new ConfigHotkey("placementRestrictionModeDiagonal",  "Z,5").apply(HOTKEY_KEY);
    public static final ConfigHotkey PLACEMENT_RESTRICTION_MODE_FACE    = new ConfigHotkey("placementRestrictionModeFace",      "Z,2").apply(HOTKEY_KEY);
    public static final ConfigHotkey PLACEMENT_RESTRICTION_MODE_LAYER   = new ConfigHotkey("placementRestrictionModeLayer",     "Z,6").apply(HOTKEY_KEY);
    public static final ConfigHotkey PLACEMENT_RESTRICTION_MODE_LINE    = new ConfigHotkey("placementRestrictionModeLine",      "Z,4").apply(HOTKEY_KEY);
    public static final ConfigHotkey PLACEMENT_RESTRICTION_MODE_PLANE   = new ConfigHotkey("placementRestrictionModePlane",     "Z,1").apply(HOTKEY_KEY);
    public static final ConfigHotkey SIT_DOWN_NEARBY_PETS               = new ConfigHotkey("sitDownNearbyPets",                 "").apply(HOTKEY_KEY);
    public static final ConfigHotkey SKIP_ALL_RENDERING                 = new ConfigHotkey("skipAllRendering",                  "").apply(HOTKEY_KEY);
    public static final ConfigHotkey SKIP_WORLD_RENDERING               = new ConfigHotkey("skipWorldRendering",                "").apply(HOTKEY_KEY);
    public static final ConfigHotkey STAND_UP_NEARBY_PETS               = new ConfigHotkey("standUpNearbyPets",                 "").apply(HOTKEY_KEY);
    public static final ConfigHotkey SWAP_ELYTRA_CHESTPLATE             = new ConfigHotkey("swapElytraChestplate",              "").apply(HOTKEY_KEY);
    public static final ConfigHotkey TOGGLE_AP_PROTOCOL                 = new ConfigHotkey("toggleAccuratePlacementProtocol",   "").apply(HOTKEY_KEY);
    public static final ConfigHotkey TOGGLE_GRAB_CURSOR                 = new ConfigHotkey("toggleGrabCursor",                  "").apply(HOTKEY_KEY);
    public static final ConfigHotkey TOOL_PICK                          = new ConfigHotkey("toolPick",                          "").apply(HOTKEY_KEY);
    public static final ConfigHotkey WRITE_MAPS_AS_IMAGES               = new ConfigHotkey("writeMapsAsImages",                 "").apply(HOTKEY_KEY);
    public static final ConfigHotkey ZOOM_ACTIVATE                      = new ConfigHotkey("zoomActivate",                      "", KeybindSettings.create(Context.INGAME, KeyAction.BOTH, true, false, false, false, false)).apply(HOTKEY_KEY);

    public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
            ACCURATE_BLOCK_PLACEMENT_IN,
            ACCURATE_BLOCK_PLACEMENT_REVERSE,
            BREAKING_RESTRICTION_MODE_COLUMN,
            BREAKING_RESTRICTION_MODE_DIAGONAL,
            BREAKING_RESTRICTION_MODE_FACE,
            BREAKING_RESTRICTION_MODE_LAYER,
            BREAKING_RESTRICTION_MODE_LINE,
            BREAKING_RESTRICTION_MODE_PLANE,
            COPY_SIGN_TEXT,
            ELYTRA_CAMERA,
            FLEXIBLE_BLOCK_PLACEMENT_ADJACENT,
            FLEXIBLE_BLOCK_PLACEMENT_OFFSET,
            FLEXIBLE_BLOCK_PLACEMENT_ROTATION,
            FLY_PRESET_1,
            FLY_PRESET_2,
            FLY_PRESET_3,
            FLY_PRESET_4,
            FREE_CAMERA_PLAYER_INPUTS,
            FREE_CAMERA_PLAYER_MOVEMENT,
            HOTBAR_SCROLL,
            HOTBAR_SWAP_BASE,
            HOTBAR_SWAP_1,
            HOTBAR_SWAP_2,
            HOTBAR_SWAP_3,
            INVENTORY_PREVIEW,
            OPEN_CONFIG_GUI,
            PLACEMENT_Y_MIRROR,
            PLAYER_INVENTORY_PEEK,
            PLACEMENT_RESTRICTION_MODE_COLUMN,
            PLACEMENT_RESTRICTION_MODE_DIAGONAL,
            PLACEMENT_RESTRICTION_MODE_FACE,
            PLACEMENT_RESTRICTION_MODE_LAYER,
            PLACEMENT_RESTRICTION_MODE_LINE,
            PLACEMENT_RESTRICTION_MODE_PLANE,
            SIT_DOWN_NEARBY_PETS,
            SKIP_ALL_RENDERING,
            SKIP_WORLD_RENDERING,
            STAND_UP_NEARBY_PETS,
            SWAP_ELYTRA_CHESTPLATE,
            TOGGLE_AP_PROTOCOL,
            TOGGLE_GRAB_CURSOR,
            TOOL_PICK,
            WRITE_MAPS_AS_IMAGES,
            ZOOM_ACTIVATE
    );
}
