package fi.dy.masa.tweakeroo.event;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.util.MiscUtils;
import fi.dy.masa.tweakeroo.util.SnapAimMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler
{
    private static final InputHandler INSTANCE = new InputHandler();
    private LeftRight lastSidewaysInput = LeftRight.NONE;
    private ForwardBack lastForwardInput = ForwardBack.NONE;

    private InputHandler()
    {
        super();
    }

    public static InputHandler getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void addKeysToMap(IKeybindManager manager)
    {
        for (FeatureToggle toggle : FeatureToggle.values())
        {
            manager.addKeybindToMap(toggle.getKeybind());
        }

        for (IHotkey hotkey : Hotkeys.HOTKEY_LIST)
        {
            manager.addKeybindToMap(hotkey.getKeybind());
        }

        for (IHotkey hotkey : Configs.Generic.HOTKEYS)
        {
            manager.addKeybindToMap(hotkey.getKeybind());
        }

        for (IHotkey hotkey : Configs.Disable.OPTIONS)
        {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
    }

    @Override
    public void addHotkeys(IKeybindManager manager)
    {
        manager.addHotkeysForCategory(Reference.MOD_NAME, "tweakeroo.hotkeys.category.disable_toggle_hotkeys", Configs.Disable.OPTIONS);
        manager.addHotkeysForCategory(Reference.MOD_NAME, "tweakeroo.hotkeys.category.generic_hotkeys", Hotkeys.HOTKEY_LIST);
        manager.addHotkeysForCategory(Reference.MOD_NAME, "tweakeroo.hotkeys.category.generic_config_hotkeys", Configs.Generic.HOTKEYS);
        manager.addHotkeysForCategory(Reference.MOD_NAME, "tweakeroo.hotkeys.category.tweak_toggle_hotkeys", ImmutableList.copyOf(FeatureToggle.values()));
    }

    @Override
    public boolean onKeyInput(KeyEvent input, boolean eventKeyState)
    {
        Minecraft mc = Minecraft.getInstance();

        // Not in a GUI
        if (GuiUtils.getCurrentScreen() == null && eventKeyState)
        {
            this.storeLastMovementDirection(input, mc);
        }

        MiscUtils.checkZoomStatus();

        return false;
    }

    @Override
    public boolean onMouseClick(MouseButtonEvent click, boolean eventButtonState)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null || mc.gameMode == null || mc.hitResult == null ||
            GuiUtils.getCurrentScreen() != null)
        {
            return false;
        }

        if (mc.player.isCreative() && FeatureToggle.TWEAK_ANGEL_BLOCK.getBooleanValue() && eventButtonState &&
            mc.options.keyUse.matchesMouse(click) && mc.hitResult.getType() == HitResult.Type.MISS)
        {
            Vec3 eyePos = mc.player.getEyePosition();
            Vec3 rotVec = mc.player.getViewVector(1.0f);

            Vec3 vec3d = eyePos.add(rotVec.scale(Configs.Generic.ANGEL_BLOCK_PLACEMENT_DISTANCE.getDoubleValue()));
            BlockHitResult context = mc.level.clip(new ClipContext(eyePos, vec3d, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, mc.player));
            
            for (InteractionHand hand : InteractionHand.values())
            {
                ItemStack stack = mc.player.getItemInHand(hand);
                if (stack.isEmpty() == false && stack.getItem() instanceof BlockItem)
                {
                    mc.gameMode.useItemOn(mc.player, hand, context);
                    mc.player.swing(hand);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double dWheel)
    {
        // Not in a GUI
        if (GuiUtils.getCurrentScreen() == null && dWheel != 0)
        {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            String preGreen = GuiBase.TXT_GREEN;
            String rst = GuiBase.TXT_RST;

            if (FeatureToggle.TWEAK_HOTBAR_SCROLL.getBooleanValue() && Hotkeys.HOTBAR_SCROLL.getKeybind().isKeybindHeld())
            {
                int currentRow = Configs.Internal.HOTBAR_SCROLL_CURRENT_ROW.getIntegerValue();

                int newRow = currentRow + (dWheel < 0 ? 1 : -1);
                int max = 2;
                if      (newRow < 0) { newRow = max; }
                else if (newRow > max) { newRow = 0; }

                Configs.Internal.HOTBAR_SCROLL_CURRENT_ROW.setIntegerValue(newRow);

                return true;
            }
            else if (FeatureToggle.TWEAK_FLY_SPEED.getKeybind().isKeybindHeld())
            {
                ConfigDouble config = Configs.getActiveFlySpeedConfig();
                double newValue = config.getDoubleValue() + (dWheel > 0 ? 0.005 : -0.005);
                config.setDoubleValue(newValue);
                KeyCallbackAdjustable.setValueChanged();

                String strIndex = preGreen + (Configs.Internal.FLY_SPEED_PRESET.getIntegerValue() + 1) + rst;
                String strValue = preGreen + String.format("%.3f", config.getDoubleValue()) + rst;
                InfoUtils.printActionbarMessage("tweakeroo.message.set_fly_speed_to", strIndex, strValue);

                return true;
            }
            else if (FeatureToggle.TWEAK_AFTER_CLICKER.getKeybind().isKeybindHeld())
            {
                int newValue = Configs.Generic.AFTER_CLICKER_CLICK_COUNT.getIntegerValue() + (dWheel > 0 ? 1 : -1);
                Configs.Generic.AFTER_CLICKER_CLICK_COUNT.setIntegerValue(newValue);
                KeyCallbackAdjustable.setValueChanged();

                String strValue = preGreen + Configs.Generic.AFTER_CLICKER_CLICK_COUNT.getIntegerValue() + rst;
                InfoUtils.printActionbarMessage("tweakeroo.message.set_after_clicker_count_to", strValue);

                return true;
            }
            else if (FeatureToggle.TWEAK_PLACEMENT_LIMIT.getKeybind().isKeybindHeld())
            {
                int newValue = Configs.Generic.PLACEMENT_LIMIT.getIntegerValue() + (dWheel > 0 ? 1 : -1);
                Configs.Generic.PLACEMENT_LIMIT.setIntegerValue(newValue);
                KeyCallbackAdjustable.setValueChanged();

                String strValue = preGreen + Configs.Generic.PLACEMENT_LIMIT.getIntegerValue() + rst;
                InfoUtils.printActionbarMessage("tweakeroo.message.set_placement_limit_to", strValue);

                return true;
            }
            else if (FeatureToggle.TWEAK_HOTBAR_SLOT_CYCLE.getKeybind().isKeybindHeld())
            {
                int newValue = Configs.Generic.HOTBAR_SLOT_CYCLE_MAX.getIntegerValue() + (dWheel > 0 ? 1 : -1);
                Configs.Generic.HOTBAR_SLOT_CYCLE_MAX.setIntegerValue(newValue);
                KeyCallbackAdjustable.setValueChanged();

                String strValue = preGreen + Configs.Generic.HOTBAR_SLOT_CYCLE_MAX.getIntegerValue() + rst;
                InfoUtils.printActionbarMessage("tweakeroo.message.set_hotbar_slot_cycle_max_to", strValue);

                return true;
            }
            else if (FeatureToggle.TWEAK_HOTBAR_SLOT_RANDOMIZER.getKeybind().isKeybindHeld())
            {
                int newValue = Configs.Generic.HOTBAR_SLOT_RANDOMIZER_MAX.getIntegerValue() + (dWheel > 0 ? 1 : -1);
                Configs.Generic.HOTBAR_SLOT_RANDOMIZER_MAX.setIntegerValue(newValue);
                KeyCallbackAdjustable.setValueChanged();

                String strValue = preGreen + Configs.Generic.HOTBAR_SLOT_RANDOMIZER_MAX.getIntegerValue() + rst;
                InfoUtils.printActionbarMessage("tweakeroo.message.set_hotbar_slot_randomizer_max_to", strValue);

                return true;
            }
            else if (FeatureToggle.TWEAK_BREAKING_GRID.getKeybind().isKeybindHeld())
            {
                int newValue = Configs.Generic.BREAKING_GRID_SIZE.getIntegerValue() + (dWheel > 0 ? 1 : -1);
                Configs.Generic.BREAKING_GRID_SIZE.setIntegerValue(newValue);
                KeyCallbackAdjustable.setValueChanged();

                String strValue = preGreen + Configs.Generic.BREAKING_GRID_SIZE.getIntegerValue() + rst;
                InfoUtils.printActionbarMessage("tweakeroo.message.set_breaking_grid_size_to", strValue);

                return true;
            }
            else if (FeatureToggle.TWEAK_PLACEMENT_GRID.getKeybind().isKeybindHeld())
            {
                int newValue = Configs.Generic.PLACEMENT_GRID_SIZE.getIntegerValue() + (dWheel > 0 ? 1 : -1);
                Configs.Generic.PLACEMENT_GRID_SIZE.setIntegerValue(newValue);
                KeyCallbackAdjustable.setValueChanged();

                String strValue = preGreen + Configs.Generic.PLACEMENT_GRID_SIZE.getIntegerValue() + rst;
                InfoUtils.printActionbarMessage("tweakeroo.message.set_placement_grid_size_to", strValue);

                return true;
            }
            else if (FeatureToggle.TWEAK_SNAP_AIM.getKeybind().isKeybindHeld())
            {
                SnapAimMode mode = (SnapAimMode) Configs.Generic.SNAP_AIM_MODE.getOptionListValue();
                ConfigDouble config = mode == SnapAimMode.PITCH ? Configs.Generic.SNAP_AIM_PITCH_STEP : Configs.Generic.SNAP_AIM_YAW_STEP;

                double newValue = config.getDoubleValue() * (dWheel > 0 ? 2 : 0.5);
                config.setDoubleValue(newValue);
                KeyCallbackAdjustable.setValueChanged();

                String val = preGreen + String.valueOf(config.getDoubleValue()) + rst;
                String key = mode == SnapAimMode.PITCH ? "tweakeroo.message.set_snap_aim_pitch_step_to" : "tweakeroo.message.set_snap_aim_yaw_step_to";

                InfoUtils.printActionbarMessage(key, val);

                return true;
            }
            else if (FeatureToggle.TWEAK_ZOOM.getKeybind().isKeybindHeld() ||
                     (FeatureToggle.TWEAK_ZOOM.getBooleanValue() && Hotkeys.ZOOM_ACTIVATE.getKeybind().isKeybindHeld()))
            {
                double diff = GuiBase.isCtrlDown() ?
                        Configs.Generic.ZOOM_FOV_DIFFERENCE_CTRL.getDoubleValue() :
                        Configs.Generic.ZOOM_FOV_DIFFERENCE.getDoubleValue();
                
                double newValue = Configs.Generic.ZOOM_FOV.getDoubleValue() + (dWheel < 0 ? diff : -diff);
                Configs.Generic.ZOOM_FOV.setDoubleValue(newValue);

                // Only prevent the next trigger when adjusting the value with the actual toggle key held
                if (FeatureToggle.TWEAK_ZOOM.getKeybind().isKeybindHeld())
                {
                    KeyCallbackAdjustable.setValueChanged();
                }

                String strValue = String.format("%s%.1f%s", preGreen, Configs.Generic.ZOOM_FOV.getDoubleValue(), rst);
                InfoUtils.printActionbarMessage("tweakeroo.message.set_zoom_fov_to", strValue);

                return true;
            }
            else if (FeatureToggle.TWEAK_SPYGLASS_USES_TWEAK_ZOOM.getBooleanValue() &&
                     !FeatureToggle.TWEAK_ZOOM.getKeybind().isKeybindHeld() &&
                     player != null && player.isScoping())
            {
                double diff = GuiBase.isCtrlDown() ?
                              Configs.Generic.ZOOM_FOV_DIFFERENCE_CTRL.getDoubleValue() :
                              Configs.Generic.ZOOM_FOV_DIFFERENCE.getDoubleValue();

                double newValue = Configs.Generic.ZOOM_FOV.getDoubleValue() + (dWheel < 0 ? diff : -diff);
                Configs.Generic.ZOOM_FOV.setDoubleValue(newValue);

                String strValue = String.format("%s%.1f%s", preGreen, Configs.Generic.ZOOM_FOV.getDoubleValue(), rst);
                InfoUtils.printActionbarMessage("tweakeroo.message.set_zoom_fov_to", strValue);

                return true;
            }

//            else if (FeatureToggle.TWEAK_PERIODIC_ATTACK.getKeybind().isKeybindHeld())
//            {
//                int newValue = Configs.Generic.PERIODIC_ATTACK_INTERVAL.getIntegerValue() + (dWheel > 0 ? 1 : -1);
//                Configs.Generic.PERIODIC_ATTACK_INTERVAL.setIntegerValue(newValue);
//                KeyCallbackAdjustable.setValueChanged();
//
//                String strValue = preGreen + Configs.Generic.PERIODIC_ATTACK_INTERVAL.getIntegerValue() + rst;
//                InfoUtils.printActionbarMessage("tweakeroo.message.set_periodic_attack_interval_to", strValue);
//
//                return true;
//            }
//            else if (FeatureToggle.TWEAK_PERIODIC_USE.getKeybind().isKeybindHeld())
//            {
//                int newValue = Configs.Generic.PERIODIC_USE_INTERVAL.getIntegerValue() + (dWheel > 0 ? 1 : -1);
//                Configs.Generic.PERIODIC_USE_INTERVAL.setIntegerValue(newValue);
//                KeyCallbackAdjustable.setValueChanged();
//
//                String strValue = preGreen + Configs.Generic.PERIODIC_USE_INTERVAL.getIntegerValue() + rst;
//                InfoUtils.printActionbarMessage("tweakeroo.message.set_periodic_use_interval_to", strValue);
//
//                return true;
//            }
//            else if (FeatureToggle.TWEAK_PERIODIC_HOLD_ATTACK.getKeybind().isKeybindHeld())
//            {
//                int newValue = Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.getIntegerValue() + (dWheel > 0 ? 1 : -1);
//                Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.setIntegerValue(newValue);
//                KeyCallbackAdjustable.setValueChanged();
//
//                String strValue = preGreen + Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.getIntegerValue() + rst;
//                InfoUtils.printActionbarMessage("tweakeroo.message.set_periodic_hold_attack_interval_to", strValue);
//
//                return true;
//            }
//            else if (FeatureToggle.TWEAK_PERIODIC_HOLD_USE.getKeybind().isKeybindHeld())
//            {
//                int newValue = Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.getIntegerValue() + (dWheel > 0 ? 1 : -1);
//                Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.setIntegerValue(newValue);
//                KeyCallbackAdjustable.setValueChanged();
//
//                String strValue = preGreen + Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.getIntegerValue() + rst;
//                InfoUtils.printActionbarMessage("tweakeroo.message.set_periodic_hold_use_interval_to", strValue);
//
//                return true;
//            }
        }

        return false;
    }

    public LeftRight getLastSidewaysInput()
    {
        return this.lastSidewaysInput;
    }

    public ForwardBack getLastForwardInput()
    {
        return this.lastForwardInput;
    }

    private void storeLastMovementDirection(KeyEvent input, Minecraft mc)
    {
        if (mc.options.keyUp.matches(input))
        {
            this.lastForwardInput = ForwardBack.FORWARD;
        }
        else if (mc.options.keyDown.matches(input))
        {
            this.lastForwardInput = ForwardBack.BACK;
        }
        else if (mc.options.keyLeft.matches(input))
        {
            this.lastSidewaysInput = LeftRight.LEFT;
        }
        else if (mc.options.keyRight.matches(input))
        {
            this.lastSidewaysInput = LeftRight.RIGHT;
        }
    }

    public void handleMovementKeys(ClientInput input)
    {
        Options settings = Minecraft.getInstance().options;
        Input m = input.keyPresses;

        if (settings.keyLeft.isDown() && settings.keyRight.isDown())
        {
            if (this.lastSidewaysInput == LeftRight.LEFT)
            {
                //m.movementSideways = 1;
                input.keyPresses = new Input(m.forward(), m.backward(), true, false, m.jump(), m.shift(), m.sprint());
            }
            else if (this.lastSidewaysInput == LeftRight.RIGHT)
            {
                //m.movementSideways = -1;
                input.keyPresses =  new Input(m.forward(), m.backward(), false, true, m.jump(), m.shift(), m.sprint());
            }
        }

        if (settings.keyDown.isDown() && settings.keyUp.isDown())
        {
            if (this.lastForwardInput == ForwardBack.FORWARD)
            {
                //m.movementForward = 1;
                input.keyPresses = new Input(true, false, m.left(), m.right(), m.jump(), m.shift(), m.sprint());
            }
            else if (this.lastForwardInput == ForwardBack.BACK)
            {
                //m.movementForward = -1;
                input.keyPresses = new Input(false, true, m.left(), m.right(), m.jump(), m.shift(), m.sprint());
            }
        }
    }

    public enum LeftRight
    {
        NONE,
        LEFT,
        RIGHT
    }

    public enum ForwardBack
    {
        NONE,
        FORWARD,
        BACK
    }
}
