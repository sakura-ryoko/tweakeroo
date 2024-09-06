package fi.dy.masa.tweakeroo.util;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.text.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.PositionUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.mixin.IMixinAxeItem;
import fi.dy.masa.tweakeroo.mixin.IMixinClientWorld;
import fi.dy.masa.tweakeroo.mixin.IMixinCommandBlockExecutor;
import fi.dy.masa.tweakeroo.mixin.IMixinShovelItem;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;

public class MiscUtils
{
    // name;blocks;biome;options;iconitem
    public static final Pattern PATTERN_WORLD_PRESET = Pattern.compile("^(?<name>[a-zA-Z0-9_/&*#!=()\\[\\]{} -]+);(?<blocks>[a-z0-9_:.*,-]+);(?<biome>[a-z0-9_:.-]+);(?<options>[a-z0-9_, ()=]*);(?<icon>[a-z0-9_:.-]+)$");

    private static SignText previousSignText;
    private static String previousChatText = "";
    private static final Date DATE = new Date();
    private static double lastRealPitch;
    private static double lastRealYaw;
    private static double mouseSensitivity = -1.0F;
    private static boolean zoomActive;

    private static boolean periodicAttackActive;
    private static boolean periodicUseActive;
    private static boolean periodicHoldAttackActive;
    private static boolean periodicHoldUseActive;

    private static PostKeyAction lastZoomValue;
    private static PostKeyAction lastPeriodicAttackValue;
    private static PostKeyAction lastPeriodicUseValue;
    private static PostKeyAction lastPeriodicHoldAttackValue;
    private static PostKeyAction lastPeriodicHoldUseValue;

    public static void handlePlayerDeceleration()
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        Input input = player.input;

        //if (input.jumping || input.sneaking ||
        if (input.playerInput.jump() || input.playerInput.sneak() ||
            player.forwardSpeed != 0 || player.sidewaysSpeed != 0 || player.getAbilities().flying == false)
        {
            return;
        }

        double factor = Configs.Generic.FLY_DECELERATION_FACTOR.getDoubleValue();
        player.setVelocity(player.getVelocity().multiply(factor));
    }

    public static Vec3d calculatePlayerMotionWithDeceleration(Vec3d lastMotion,
                                                              double rampAmount,
                                                              double decelerationFactor)
    {
        GameOptions options = MinecraftClient.getInstance().options;
        int forward = 0;
        int vertical = 0;
        int strafe = 0;

        if (options.forwardKey.isPressed()) { forward += 1;  }
        if (options.backKey.isPressed())    { forward -= 1;  }
        if (options.leftKey.isPressed())    { strafe += 1;   }
        if (options.rightKey.isPressed())   { strafe -= 1;   }
        if (options.jumpKey.isPressed())    { vertical += 1; }
        if (options.sneakKey.isPressed())   { vertical -= 1; }

        double speed = (forward != 0 && strafe != 0) ? 1.2 : 1.0;
        double forwardRamped  = getRampedMotion(lastMotion.x, forward , rampAmount, decelerationFactor) / speed;
        double verticalRamped = getRampedMotion(lastMotion.y, vertical, rampAmount, decelerationFactor);
        double strafeRamped   = getRampedMotion(lastMotion.z, strafe  , rampAmount, decelerationFactor) / speed;

        return new Vec3d(forwardRamped, verticalRamped, strafeRamped);
    }

    public static double getRampedMotion(double current, int input, double rampAmount, double decelerationFactor)
    {
        if (input != 0)
        {
            if (input < 0)
            {
                rampAmount *= -1.0;
            }

            // Immediately kill the motion when changing direction to the opposite
            if ((input < 0) != (current < 0.0))
            {
                current = 0.0;
            }

            current = MathHelper.clamp(current + rampAmount, -1.0, 1.0);
        }
        else
        {
            current *= decelerationFactor;
        }

        return current;
    }

    public static boolean isZoomActive()
    {
        return FeatureToggle.TWEAK_ZOOM.getBooleanValue() &&
               Hotkeys.ZOOM_ACTIVATE.getKeybind().isKeybindHeld();
    }

    public static void checkZoomStatus()
    {
        if (zoomActive && isZoomActive() == false)
        {
            onZoomDeactivated();
        }
    }

    public static void onZoomActivated()
    {
        if (Configs.Generic.ZOOM_ADJUST_MOUSE_SENSITIVITY.getBooleanValue())
        {
            setMouseSensitivityForZoom();
            lastZoomValue = new PostKeyAction(Configs.Generic.ZOOM_FOV.getDoubleValue());
        }

        zoomActive = true;
    }

    public static void onZoomDeactivated()
    {
        if (zoomActive)
        {
            resetMouseSensitivityForZoom();
            if (lastZoomValue != null && lastZoomValue.isActive())
            {
                if (lastZoomValue.getLastDoubleValue() != Configs.Generic.ZOOM_FOV.getDoubleValue() &&
                    Configs.Generic.ZOOM_RESET_FOV_ON_ACTIVATE.getBooleanValue())
                {
                    Configs.Generic.ZOOM_FOV.setDoubleValue(lastZoomValue.getLastDoubleValue());
                }

                lastZoomValue.setActionHandled();
            }

            // Refresh the rendered chunks when exiting zoom mode
            MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate();

            zoomActive = false;
        }
    }

    public static void setMouseSensitivityForZoom()
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        double fov = Configs.Generic.ZOOM_FOV.getDoubleValue();
        double origFov = mc.options.getFov().getValue();

        if (fov < origFov)
        {
            // Only store it once
            if (mouseSensitivity <= 0.0 || mouseSensitivity > 1.0)
            {
                mouseSensitivity = mc.options.getMouseSensitivity().getValue();
            }

            double min = 0.04;
            double sens = min + (0.5 - min) * (1.0 - (origFov - fov) / origFov);
            mc.options.getMouseSensitivity().setValue(Math.min(mouseSensitivity, sens));
        }
    }

    public static void resetMouseSensitivityForZoom()
    {
        if (mouseSensitivity > 0.0)
        {
            MinecraftClient.getInstance().options.getMouseSensitivity().setValue(mouseSensitivity);
            mouseSensitivity = -1.0;
        }
    }

    public boolean isPeriodicAttackActive()
    {
        return periodicAttackActive;
    }

    public static void onPeriodicAttackActivated()
    {
        lastPeriodicAttackValue = new PostKeyAction(Configs.Generic.PERIODIC_ATTACK_INTERVAL.getIntegerValue());
        periodicAttackActive = true;
    }

    public static void onPeriodicAttackDeactivated()
    {
        if (periodicAttackActive)
        {
            if (lastPeriodicAttackValue != null && lastPeriodicAttackValue.isActive())
            {
                if (lastPeriodicAttackValue.getLastIntValue() != Configs.Generic.PERIODIC_ATTACK_INTERVAL.getIntegerValue() &&
                    Configs.Generic.PERIODIC_ATTACK_RESET_ON_ACTIVATE.getBooleanValue())
                {
                    Configs.Generic.PERIODIC_ATTACK_INTERVAL.setIntegerValue(lastPeriodicAttackValue.getLastIntValue());
                }
                
                lastPeriodicAttackValue.setActionHandled();
            }

            periodicAttackActive = false;
        }
    }

    public boolean isPeriodicUseActive()
    {
        return periodicUseActive;
    }

    public static void onPeriodicUseActivated()
    {
        lastPeriodicUseValue = new PostKeyAction(Configs.Generic.PERIODIC_USE_INTERVAL.getIntegerValue());
        periodicUseActive = true;
    }

    public static void onPeriodicUseDeactivated()
    {
        if (periodicUseActive)
        {
            if (lastPeriodicUseValue != null && lastPeriodicUseValue.isActive())
            {
                if (lastPeriodicUseValue.getLastIntValue() != Configs.Generic.PERIODIC_USE_INTERVAL.getIntegerValue() &&
                    Configs.Generic.PERIODIC_USE_RESET_ON_ACTIVATE.getBooleanValue())
                {
                    Configs.Generic.PERIODIC_USE_INTERVAL.setIntegerValue(lastPeriodicUseValue.getLastIntValue());
                }

                lastPeriodicUseValue.setActionHandled();
            }

            periodicUseActive = false;
        }
    }

    public boolean isPeriodicHoldAttackActive()
    {
        return periodicHoldAttackActive;
    }

    public static void onPeriodicHoldAttackActivated()
    {
        lastPeriodicHoldAttackValue = new PostKeyAction(Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.getIntegerValue());
        periodicHoldAttackActive = true;
    }

    public static void onPeriodicHoldAttackDeactivated()
    {
        if (periodicHoldAttackActive)
        {
            if (lastPeriodicHoldAttackValue != null && lastPeriodicHoldAttackValue.isActive())
            {
                if (lastPeriodicHoldAttackValue.getLastIntValue() != Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.getIntegerValue() &&
                    Configs.Generic.PERIODIC_HOLD_ATTACK_RESET_ON_ACTIVATE.getBooleanValue())
                {
                    Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.setIntegerValue(lastPeriodicHoldAttackValue.getLastIntValue());
                }
                
                lastPeriodicHoldAttackValue.setActionHandled();
            }

            periodicHoldAttackActive = false;
        }
    }

    public boolean isPeriodicHoldUseActive()
    {
        return periodicHoldUseActive;
    }

    public static void onPeriodicHoldUseActivated()
    {
        lastPeriodicHoldUseValue = new PostKeyAction(Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.getIntegerValue());
        periodicHoldUseActive = true;
    }

    public static void onPeriodicHoldUseDeactivated()
    {
        if (periodicHoldUseActive)
        {
            if (lastPeriodicHoldUseValue != null && lastPeriodicHoldUseValue.isActive())
            {
                if (lastPeriodicHoldUseValue.getLastIntValue() != Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.getIntegerValue() &&
                    Configs.Generic.PERIODIC_HOLD_USE_RESET_ON_ACTIVATE.getBooleanValue())
                {
                    Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.setIntegerValue(lastPeriodicHoldUseValue.getLastIntValue());
                }

                lastPeriodicHoldUseValue.setActionHandled();
            }

            periodicHoldUseActive = false;
        }
    }

    public static boolean isStrippableLog(World world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        return IMixinAxeItem.tweakeroo_getStrippedBlocks().containsKey(state.getBlock());
    }

    public static boolean isShovelPathConvertableBlock(World world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        return IMixinShovelItem.tweakeroo_getPathStates().containsKey(state.getBlock());
    }

    public static boolean getUpdateExec(CommandBlockBlockEntity te)
    {
        return ((IMixinCommandBlockExecutor) te.getCommandExecutor()).getUpdateLastExecution();
    }

    public static void setUpdateExec(CommandBlockBlockEntity te, boolean value)
    {
        ((IMixinCommandBlockExecutor) te.getCommandExecutor()).setUpdateLastExecution(value);
    }

    public static void printDeathCoordinates(MinecraftClient mc)
    {
        BlockPos pos = PositionUtils.getEntityBlockPos(mc.player);
        String dim = mc.player.getEntityWorld().getRegistryKey().getValue().toString();
        String str = StringUtils.translate("tweakeroo.message.death_coordinates",
                                           pos.getX(), pos.getY(), pos.getZ(), dim);
        MutableText message = Text.literal(str);
        Style style = message.getStyle();
        String coords = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        style = style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, coords));
        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(coords)));
        message.setStyle(style);
        mc.inGameHud.getChatHud().addMessage(message);
        Tweakeroo.logger.info(str);
    }

    public static String getChatTimestamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat(Configs.Generic.CHAT_TIME_FORMAT.getStringValue());
        DATE.setTime(System.currentTimeMillis());
        return sdf.format(DATE);
    }

    public static void setLastChatText(String text)
    {
        previousChatText = text;
    }

    public static String getLastChatText()
    {
        return previousChatText;
    }

    public static int getChatBackgroundColor(int colorOrig)
    {
        int newColor = Configs.Generic.CHAT_BACKGROUND_COLOR.getIntegerValue();
        return (newColor & 0x00FFFFFF) | ((int) (((newColor >>> 24) / 255.0) * ((colorOrig >>> 24) / 255.0) / 0.5 * 255) << 24);
    }

    public static void copyTextFromSign(SignBlockEntity te, boolean front)
    {
        previousSignText = ((ISignTextAccess) te).tweakeroo$getText(front);
    }

    public static void applyPreviousTextToSign(SignBlockEntity te, @Nullable AbstractSignEditScreen guiLines, boolean front)
    {
        if (previousSignText != null)
        {
            te.setText(previousSignText, front);

            if (guiLines != null) {
                ((IGuiEditSign) guiLines).tweakeroo$applyText(previousSignText);
            }
        }
    }

    public static boolean commandNearbyPets(boolean sitDown)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        PlayerEntity player = mc.player;

        if (world != null && player != null)
        {
            UUID uuid = player.getUuid();
            double centerX = player.getX();
            double centerY = player.getY();
            double centerZ = player.getZ();
            double range = 6.0;
            Box box = new Box(centerX - range, centerY - range, centerZ - range,
                              centerX + range, centerY + range, centerZ + range);
            Predicate<Entity> filter = (e) -> isTameableOwnedBy(e, uuid);

            for (Entity entity : world.getOtherEntities(null, box, filter))
            {
                if (((TameableEntity) entity).isInSittingPose() != sitDown)
                {
                    rightClickEntity(entity, mc, player);
                }
            }
        }

        return true;
    }

    public static boolean isTameableOwnedBy(Entity entity, UUID ownerUuid)
    {
        return ((entity instanceof TameableEntity) &&
               ownerUuid.equals(((TameableEntity) entity).getOwnerUuid())) &&
               ((TameableEntity) entity).isTamed();
    }

    public static void rightClickEntity(Entity entity, MinecraftClient mc, PlayerEntity player)
    {
        Hand hand = Hand.MAIN_HAND;
        ActionResult actionResult = mc.interactionManager.interactEntityAtLocation(player, entity, new EntityHitResult(entity), hand);

        if (actionResult.isAccepted() == false)
        {
            actionResult = mc.interactionManager.interactEntity(player, entity, hand);
        }

        if (actionResult instanceof ActionResult.Success success)
        {
            if (success.swingSource() == ActionResult.SwingSource.CLIENT)
            {
                player.swingHand(hand);
            }
        }
    }

    public static double getLastRealPitch()
    {
        return lastRealPitch;
    }

    public static double getLastRealYaw()
    {
        return lastRealYaw;
    }

    public static void setEntityRotations(Entity entity, float yaw, float pitch)
    {
        entity.setYaw(yaw);
        entity.setPitch(pitch);
        entity.prevYaw = yaw;
        entity.prevPitch = pitch;

        if (entity instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) entity;
            living.headYaw = yaw;
            living.prevHeadYaw = yaw;
        }
    }

    public static float getSnappedPitch(double realPitch)
    {
        if (Configs.Generic.SNAP_AIM_MODE.getOptionListValue() != SnapAimMode.YAW)
        {
            if (lastRealPitch != realPitch)
            {
                lastRealPitch = realPitch;
                RenderUtils.notifyRotationChanged();
            }

            if (FeatureToggle.TWEAK_SNAP_AIM_LOCK.getBooleanValue())
            {
                return (float) Configs.Internal.SNAP_AIM_LAST_PITCH.getDoubleValue();
            }

            double step = Configs.Generic.SNAP_AIM_PITCH_STEP.getDoubleValue();
            int limit = Configs.Generic.SNAP_AIM_PITCH_OVERSHOOT.getBooleanValue() ? 180 : 90;
            double snappedPitch;

            //realPitch = MathHelper.clamp(realPitch, -limit, limit);

            if (realPitch < 0)
            {
                snappedPitch = -calculateSnappedAngle(-realPitch, step);
            }
            else
            {
                snappedPitch = calculateSnappedAngle(realPitch, step);
            }

            double offset = Math.abs(MathHelper.wrapDegrees((float) (snappedPitch - realPitch)));
            if (GuiBase.isCtrlDown()) System.out.printf("real: %.2f, snapped: %.2f, offset: %.2f\n", realPitch, snappedPitch, offset);

            if (Configs.Generic.SNAP_AIM_ONLY_CLOSE_TO_ANGLE.getBooleanValue() == false ||
                offset <= Configs.Generic.SNAP_AIM_THRESHOLD_PITCH.getDoubleValue())
            {
                snappedPitch = MathHelper.clamp(MathHelper.wrapDegrees(snappedPitch), -limit, limit);

                if (Configs.Internal.SNAP_AIM_LAST_PITCH.getDoubleValue() != snappedPitch)
                {
                    String g = GuiBase.TXT_GREEN;
                    String r = GuiBase.TXT_RST;
                    String str = String.format("%s%s%s (step %s%s%s)", g, String.valueOf(MathHelper.wrapDegrees(snappedPitch)), r, g, String.valueOf(step), r);

                    InfoUtils.printActionbarMessage("tweakeroo.message.snapped_to_pitch", str);

                    Configs.Internal.SNAP_AIM_LAST_PITCH.setDoubleValue(snappedPitch);
                }

                return MathHelper.wrapDegrees((float) snappedPitch);
            }
        }

        // This causes the snap message to also get shown when re-snapping to the same snap angle, when using the threshold
        Configs.Internal.SNAP_AIM_LAST_PITCH.setDoubleValue(realPitch);

        return (float) realPitch;
    }

    public static float getSnappedYaw(double realYaw)
    {
        if (Configs.Generic.SNAP_AIM_MODE.getOptionListValue() != SnapAimMode.PITCH)
        {
            if (lastRealYaw != realYaw)
            {
                lastRealYaw = realYaw;
                RenderUtils.notifyRotationChanged();
            }

            if (FeatureToggle.TWEAK_SNAP_AIM_LOCK.getBooleanValue())
            {
                return (float) Configs.Internal.SNAP_AIM_LAST_YAW.getDoubleValue();
            }

            double step = Configs.Generic.SNAP_AIM_YAW_STEP.getDoubleValue();
            double snappedYaw = calculateSnappedAngle(realYaw, step);

            if (Configs.Generic.SNAP_AIM_ONLY_CLOSE_TO_ANGLE.getBooleanValue() == false ||
                Math.abs(MathHelper.wrapDegrees((float) (snappedYaw - realYaw))) <= Configs.Generic.SNAP_AIM_THRESHOLD_YAW.getDoubleValue())
            {
                if (Configs.Internal.SNAP_AIM_LAST_YAW.getDoubleValue() != snappedYaw)
                {
                    String g = GuiBase.TXT_GREEN;
                    String r = GuiBase.TXT_RST;
                    String str = String.format("%s%s%s (step %s%s%s)", g, String.valueOf(MathHelper.wrapDegrees(snappedYaw)), r, g, String.valueOf(step), r);

                    InfoUtils.printActionbarMessage("tweakeroo.message.snapped_to_yaw", str);

                    Configs.Internal.SNAP_AIM_LAST_YAW.setDoubleValue(snappedYaw);
                }

                return MathHelper.wrapDegrees((float) snappedYaw);
            }
        }

        // This causes the snap message to also get shown when re-snapping to the same snap angle, when using the threshold
        Configs.Internal.SNAP_AIM_LAST_YAW.setDoubleValue(realYaw);

        return (float) realYaw;
    }

    public static double calculateSnappedAngle(double realRotation, double step)
    {
        double offsetRealRotation = MathHelper.floorMod(realRotation, 360.0D) + (step / 2.0);
        return MathHelper.floorMod(((int) (offsetRealRotation / step)) * step, 360.0D);
    }

    public static boolean writeAllMapsAsImages()
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null)
        {
            return true;
        }

        Map<MapIdComponent, MapState> data = ((IMixinClientWorld) mc.world).tweakeroo_getMapStates();
        String worldName = StringUtils.getWorldOrServerName();

        if (worldName == null)
        {
            worldName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis()));
        }

        File dir = FileUtils.getConfigDirectory().toPath().resolve(Reference.MOD_ID).resolve("map_images").resolve(worldName).toFile();

        if (dir.exists() == false && dir.mkdirs() == false)
        {
            InfoUtils.showGuiOrInGameMessage(Message.MessageType.ERROR, "Failed to create directory: " + dir.getAbsolutePath());
            return true;
        }

        int count = 0;

        for (Map.Entry<MapIdComponent, MapState> entry : data.entrySet())
        {
            File file = new File(dir, entry.getKey().asString() + ".png");
            writeMapAsImage(file, entry.getValue());
            ++count;
        }

        InfoUtils.showGuiOrInGameMessage(Message.MessageType.INFO, String.format("Wrote %d maps to image files", count));

        return true;
    }

    private static void writeMapAsImage(File fileOut, MapState state)
    {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < 128; ++y)
        {
            for (int x = 0; x < 128; ++x)
            {
                int index = x + y * 128;
                int color = MapColor.getRenderColor(state.colors[index]);
                // Swap the color channels from ABGR to ARGB
                int outputColor = (color & 0xFF00FF00) | (color & 0xFF0000) >> 16 | (color & 0xFF) << 16;

                image.setRGB(x, y, outputColor);
            }
        }

        try
        {
            ImageIO.write(image, "png", fileOut);
        }
        catch (Exception e)
        {
            InfoUtils.showGuiOrInGameMessage(Message.MessageType.ERROR, "Failed to write image to file: " + fileOut.getAbsolutePath());
        }
    }

    public static boolean isShulkerBox(ItemStack stack)
    {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    public static boolean hasCustomMaxStackSize(ItemStack stack)
    {
        int defaultStackSize = stack.getDefaultComponents().getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
        int currentStackSize = stack.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
        return defaultStackSize != currentStackSize;
    }

    public static class PostKeyAction
    {
        private int lastIntValue;
        private double lastDoubleValue;
        private long lastActive;
        private boolean active = false;

        public PostKeyAction(int lastIntValue)
        {
            this.lastIntValue = lastIntValue;
            this.lastDoubleValue = -1;
            this.lastActive = Util.getMeasuringTimeNano();
            this.active = true;
        }

        public PostKeyAction(double lastDoubleValue)
        {
            this.lastDoubleValue = lastDoubleValue;
            this.lastIntValue = -1;
            this.lastActive = Util.getMeasuringTimeNano();
            this.active = true;
        }

        public boolean isActive()
        {
            return this.active;
        }

        public int getLastIntValue()
        {
            return this.lastIntValue;
        }

        public double getLastDoubleValue()
        {
            return this.lastDoubleValue;
        }

        public long getLastActive()
        {
            return this.lastActive;
        }

        public void setActionHandled()
        {
            this.lastIntValue = -1;
            this.lastDoubleValue = -1;
            this.lastActive = Util.getMeasuringTimeNano();
            this.active = false;
        }
    }
}
