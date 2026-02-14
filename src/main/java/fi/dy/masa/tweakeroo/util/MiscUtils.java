package fi.dy.masa.tweakeroo.util;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.malilib.util.position.PositionUtils;
import fi.dy.masa.malilib.util.time.TickUtils;
import fi.dy.masa.malilib.util.time.TimeFormat;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.mixin.block.IMixinCommandBlockExecutor;
import fi.dy.masa.tweakeroo.mixin.item.IMixinAxeItem;
import fi.dy.masa.tweakeroo.mixin.item.IMixinShovelItem;
import fi.dy.masa.tweakeroo.mixin.option.IMixinSimpleOption;
import fi.dy.masa.tweakeroo.mixin.screen.IMixinCustomizeFlatLevelScreen;
import fi.dy.masa.tweakeroo.mixin.world.IMixinClientWorld;
import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;

public class MiscUtils
{
    // name;blocks;biome;options;iconitem
    public static final Pattern PATTERN_WORLD_PRESET = Pattern.compile("^(?<name>[a-zA-Z0-9_/&*#!=()\\[\\]{} -]+);(?<blocks>[a-z0-9_:.*,-]+);(?<biome>[a-z0-9_:.-]+);(?<options>[a-z0-9_, ()=]*);(?<icon>[a-z0-9_:.-]+)$");

    private static SignText previousSignText;
    private static String previousChatText = "";
    private static final Date DATE = new Date();
    private static double mouseSensitivity = -1.0F;
    private static boolean zoomActive;
    private static boolean spyglassZoomActive;

//    private static boolean periodicAttackActive;
//    private static boolean periodicUseActive;
//    private static boolean periodicHoldAttackActive;
//    private static boolean periodicHoldUseActive;

    private static PostKeyAction lastZoomValue;
    private static PostKeyAction lastSpyglassValue;
//    private static PostKeyAction lastPeriodicAttackValue;
//    private static PostKeyAction lastPeriodicUseValue;
//    private static PostKeyAction lastPeriodicHoldAttackValue;
//    private static PostKeyAction lastPeriodicHoldUseValue;

    public static final float DEFAULT_TICK_RATE = 20.0F;
    public static final float MIN_TICK_RATE = 1.0F;
    public static final float MAX_TICK_RATE = 10000.0F;
    private static float realTickRate = -1.0f;

    public static void handlePlayerDeceleration()
    {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

		if (player != null)
		{
			ClientInput input = player.input;

			//if (input.jumping || input.sneaking ||
			if (input.keyPresses.jump() || input.keyPresses.shift() ||
				player.zza != 0 || player.xxa != 0 || player.getAbilities().flying == false)
			{
				return;
			}

			double factor = Configs.Generic.FLY_DECELERATION_FACTOR.getDoubleValue();
			player.setDeltaMovement(player.getDeltaMovement().scale(factor));
		}
    }

    public static Vec3 calculatePlayerMotionWithDeceleration(Vec3 lastMotion,
                                                              double rampAmount,
                                                              double decelerationFactor)
    {
        Options options = Minecraft.getInstance().options;
        int forward = 0;
        int vertical = 0;
        int strafe = 0;

        if (options.keyUp.isDown())
        {
            forward += 1;
        }
        if (options.keyDown.isDown())
        {
            forward -= 1;
        }
        if (options.keyLeft.isDown())
        {
            strafe += 1;
        }
        if (options.keyRight.isDown())
        {
            strafe -= 1;
        }
        if (options.keyJump.isDown())
        {
            vertical += 1;
        }
        if (options.keyShift.isDown())
        {
            vertical -= 1;
        }

        double speed = (forward != 0 && strafe != 0) ? 1.2 : 1.0;
        double forwardRamped = getRampedMotion(lastMotion.x, forward, rampAmount, decelerationFactor) / speed;
        double verticalRamped = getRampedMotion(lastMotion.y, vertical, rampAmount, decelerationFactor);
        double strafeRamped = getRampedMotion(lastMotion.z, strafe, rampAmount, decelerationFactor) / speed;

        return new Vec3(forwardRamped, verticalRamped, strafeRamped);
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

            current = Mth.clamp(current + rampAmount, -1.0, 1.0);
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
            Minecraft.getInstance().levelRenderer.needsUpdate();

            zoomActive = false;
        }
    }

    public static boolean isSpyglassZoomActive()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        return FeatureToggle.TWEAK_SPYGLASS_USES_TWEAK_ZOOM.getBooleanValue() &&
               player != null && player.isScoping();
    }

    public static void checkSpyglassZoomStatus()
    {
        if (spyglassZoomActive && isSpyglassZoomActive() == false)
        {
            onSpyglassZoomDeactivated();
        }
    }

    public static void onSpyglassZoomActivated()
    {
        if (Configs.Generic.ZOOM_ADJUST_MOUSE_SENSITIVITY.getBooleanValue())
        {
            setMouseSensitivityForZoom();
            lastSpyglassValue = new PostKeyAction(Configs.Generic.ZOOM_FOV.getDoubleValue());
        }

        spyglassZoomActive = true;
    }

    public static void onSpyglassZoomDeactivated()
    {
        if (spyglassZoomActive)
        {
            resetMouseSensitivityForZoom();
            if (lastSpyglassValue != null && lastSpyglassValue.isActive())
            {
                if (lastSpyglassValue.getLastDoubleValue() != Configs.Generic.ZOOM_FOV.getDoubleValue() &&
                    Configs.Generic.ZOOM_RESET_FOV_ON_ACTIVATE.getBooleanValue())
                {
                    Configs.Generic.ZOOM_FOV.setDoubleValue(lastSpyglassValue.getLastDoubleValue());
                }

                lastSpyglassValue.setActionHandled();
            }

            // Refresh the rendered chunks when exiting zoom mode
            Minecraft.getInstance().levelRenderer.needsUpdate();

            spyglassZoomActive = false;
        }
    }

    public static void setMouseSensitivityForZoom()
    {
        Minecraft mc = Minecraft.getInstance();

        double fov = Configs.Generic.ZOOM_FOV.getDoubleValue();
        double origFov = mc.options.fov().get();

        if (fov < origFov)
        {
            // Only store it once
            if (mouseSensitivity <= 0.0 || mouseSensitivity > 1.0)
            {
                mouseSensitivity = mc.options.sensitivity().get();
            }

            double min = 0.04;
            double sens = min + (0.5 - min) * (1.0 - (origFov - fov) / origFov);
            mc.options.sensitivity().set(Math.min(mouseSensitivity, sens));
        }
    }

    public static void resetMouseSensitivityForZoom()
    {
        if (mouseSensitivity > 0.0)
        {
            Minecraft.getInstance().options.sensitivity().set(mouseSensitivity);
            mouseSensitivity = -1.0;
        }
    }

//    public boolean isPeriodicAttackActive()
//    {
//        return periodicAttackActive;
//    }
//
//    public static void onPeriodicAttackActivated()
//    {
//        lastPeriodicAttackValue = new PostKeyAction(Configs.Generic.PERIODIC_ATTACK_INTERVAL.getIntegerValue());
//        periodicAttackActive = true;
//    }
//
//    public static void onPeriodicAttackDeactivated()
//    {
//        if (periodicAttackActive)
//        {
//            if (lastPeriodicAttackValue != null && lastPeriodicAttackValue.isActive())
//            {
//                if (lastPeriodicAttackValue.getLastIntValue() != Configs.Generic.PERIODIC_ATTACK_INTERVAL.getIntegerValue() &&
//                        Configs.Generic.PERIODIC_ATTACK_RESET_ON_ACTIVATE.getBooleanValue())
//                {
//                    Configs.Generic.PERIODIC_ATTACK_INTERVAL.setIntegerValue(lastPeriodicAttackValue.getLastIntValue());
//                }
//
//                lastPeriodicAttackValue.setActionHandled();
//            }
//
//            periodicAttackActive = false;
//        }
//    }
//
//    public boolean isPeriodicUseActive()
//    {
//        return periodicUseActive;
//    }
//
//    public static void onPeriodicUseActivated()
//    {
//        lastPeriodicUseValue = new PostKeyAction(Configs.Generic.PERIODIC_USE_INTERVAL.getIntegerValue());
//        periodicUseActive = true;
//    }
//
//    public static void onPeriodicUseDeactivated()
//    {
//        if (periodicUseActive)
//        {
//            if (lastPeriodicUseValue != null && lastPeriodicUseValue.isActive())
//            {
//                if (lastPeriodicUseValue.getLastIntValue() != Configs.Generic.PERIODIC_USE_INTERVAL.getIntegerValue() &&
//                        Configs.Generic.PERIODIC_USE_RESET_ON_ACTIVATE.getBooleanValue())
//                {
//                    Configs.Generic.PERIODIC_USE_INTERVAL.setIntegerValue(lastPeriodicUseValue.getLastIntValue());
//                }
//
//                lastPeriodicUseValue.setActionHandled();
//            }
//
//            periodicUseActive = false;
//        }
//    }
//
//    public boolean isPeriodicHoldAttackActive()
//    {
//        return periodicHoldAttackActive;
//    }
//
//    public static void onPeriodicHoldAttackActivated()
//    {
//        lastPeriodicHoldAttackValue = new PostKeyAction(Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.getIntegerValue());
//        periodicHoldAttackActive = true;
//    }
//
//    public static void onPeriodicHoldAttackDeactivated()
//    {
//        if (periodicHoldAttackActive)
//        {
//            if (lastPeriodicHoldAttackValue != null && lastPeriodicHoldAttackValue.isActive())
//            {
//                if (lastPeriodicHoldAttackValue.getLastIntValue() != Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.getIntegerValue() &&
//                        Configs.Generic.PERIODIC_HOLD_ATTACK_RESET_ON_ACTIVATE.getBooleanValue())
//                {
//                    Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.setIntegerValue(lastPeriodicHoldAttackValue.getLastIntValue());
//                }
//
//                lastPeriodicHoldAttackValue.setActionHandled();
//            }
//
//            periodicHoldAttackActive = false;
//        }
//    }
//
//    public boolean isPeriodicHoldUseActive()
//    {
//        return periodicHoldUseActive;
//    }
//
//    public static void onPeriodicHoldUseActivated()
//    {
//        lastPeriodicHoldUseValue = new PostKeyAction(Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.getIntegerValue());
//        periodicHoldUseActive = true;
//    }
//
//    public static void onPeriodicHoldUseDeactivated()
//    {
//        if (periodicHoldUseActive)
//        {
//            if (lastPeriodicHoldUseValue != null && lastPeriodicHoldUseValue.isActive())
//            {
//                if (lastPeriodicHoldUseValue.getLastIntValue() != Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.getIntegerValue() &&
//                        Configs.Generic.PERIODIC_HOLD_USE_RESET_ON_ACTIVATE.getBooleanValue())
//                {
//                    Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.setIntegerValue(lastPeriodicHoldUseValue.getLastIntValue());
//                }
//
//                lastPeriodicHoldUseValue.setActionHandled();
//            }
//
//            periodicHoldUseActive = false;
//        }
//    }

    public static boolean isStrippableLog(Level world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        return IMixinAxeItem.tweakeroo_getStrippedBlocks().containsKey(state.getBlock());
    }

    public static boolean isShovelPathConvertableBlock(Level world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        return IMixinShovelItem.tweakeroo_getPathStates().containsKey(state.getBlock());
    }

    public static boolean getUpdateExec(CommandBlockEntity te)
    {
        return ((IMixinCommandBlockExecutor) te.getCommandBlock()).getUpdateLastExecution();
    }

    public static void setUpdateExec(CommandBlockEntity te, boolean value)
    {
        ((IMixinCommandBlockExecutor) te.getCommandBlock()).setUpdateLastExecution(value);
    }

    public static void printDeathCoordinates(Minecraft mc)
    {
		if (mc.player == null) return;
        BlockPos pos = PositionUtils.getEntityBlockPos(mc.player);
        String dim = mc.player.level().dimension().identifier().toString();
        String str = StringUtils.translate("tweakeroo.message.death_coordinates",
                                           pos.getX(), pos.getY(), pos.getZ(), dim);
        MutableComponent message = Component.literal(str);
        Style style = message.getStyle();
        String coords = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        //style = style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, coords));
        //style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(coords)));
        style = style.withClickEvent(new ClickEvent.SuggestCommand(coords));
        style = style.withHoverEvent(new HoverEvent.ShowText(Component.literal(coords)));
        message.setStyle(style);
        mc.gui.getChat().addMessage(message);
        Tweakeroo.LOGGER.info(str);
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

            if (guiLines != null)
            {
                ((IGuiEditSign) guiLines).tweakeroo$applyText(previousSignText);
            }
        }
    }

    public static boolean commandNearbyPets(boolean sitDown)
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        Player player = mc.player;

        if (world != null && player != null)
        {
            UUID uuid = player.getUUID();
            double centerX = player.getX();
            double centerY = player.getY();
            double centerZ = player.getZ();
            double range = 6.0;
            AABB box = new AABB(centerX - range, centerY - range, centerZ - range,
                              centerX + range, centerY + range, centerZ + range);
            Predicate<Entity> filter = (e) -> isTameableOwnedBy(e, uuid);

            for (Entity entity : world.getEntities((Entity) null, box, filter))
            {
                if (((TamableAnimal) entity).isInSittingPose() != sitDown)
                {
                    rightClickEntity(entity, mc, player);
                }
            }
        }

        return true;
    }

    public static boolean isTameableOwnedBy(Entity entity, UUID ownerUuid)
    {
        /*
        return ((entity instanceof TameableEntity) &&
               ownerUuid.equals(((TameableEntity) entity).getOwnerUuid())) &&
               ((TameableEntity) entity).isTamed();
         */

        // todo new 'TamableEntityHolder<>` Generic type class is used here.
        if (entity instanceof TamableAnimal te)
        {
            LivingEntity owner = te.getOwner();

            return owner != null && owner.getUUID().equals(ownerUuid);
        }

        return false;
    }

    public static void rightClickEntity(Entity entity, Minecraft mc, Player player)
    {
		if (mc.gameMode == null) return;
        InteractionHand hand = InteractionHand.MAIN_HAND;
        InteractionResult actionResult = mc.gameMode.interactAt(player, entity, new EntityHitResult(entity), hand);

        if (actionResult.consumesAction() == false)
        {
            actionResult = mc.gameMode.interact(player, entity, hand);
        }

        if (actionResult instanceof InteractionResult.Success success)
        {
            if (success.swingSource() == InteractionResult.SwingSource.CLIENT)
            {
                player.swing(hand);
            }
        }
    }

    public static void setEntityRotations(Entity entity, float yaw, float pitch)
    {
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yRotO = yaw;
        entity.xRotO = pitch;

        if (entity instanceof LivingEntity living)
        {
	        living.yHeadRot = yaw;
            living.yHeadRotO = yaw;
        }
    }

    /**
     * Copied from Tweak Fork by Andrew54757
     */
    public static Vec3 getEyesPos(Player player)
    {
        return new Vec3(player.getX(), player.getY() + player.getEyeHeight(player.getPose()), player.getZ());
    }

    /**
     * Copied from Tweak Fork by Andrew54757
     */
    public static BlockPos getPlayerHeadPos(Player player)
    {
        return (player.getPose() == Pose.STANDING) ? player.blockPosition().relative(Direction.UP) : player.blockPosition();
    }

    /**
     * Copied from Tweak Fork by Andrew54757
     */
    public static boolean isInReach(BlockPos pos, Player player, double reach)
    {
        Vec3 playerpos = getEyesPos(player);
        double d = playerpos.x() - ((double) pos.getX() + 0.5D);
        double d1 = playerpos.y() - ((double) pos.getY() + 0.5D);
        double d2 = playerpos.z() - ((double) pos.getZ() + 0.5D);
        return d * d + d1 * d1 + d2 * d2 <= reach * reach;
    }

    public static boolean writeAllMapsAsImages()
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null)
        {
            return true;
        }

        Map<MapId, MapItemSavedData> data = ((IMixinClientWorld) mc.level).tweakeroo_getMapStates();
        String worldName = StringUtils.getWorldOrServerName();

        if (worldName == null)
        {
            //worldName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis()));
            worldName = TimeFormat.REGULAR.formatNow();
        }

        Path dir = FileUtils.getConfigDirectoryAsPath().resolve(Reference.MOD_ID).resolve("map_images").resolve(worldName);

        if (!Files.exists(dir))
        {
            FileUtils.createDirectoriesIfMissing(dir);
            //Tweakeroo.debugLog("writeAllMapsAsImages(): Creating directory '{}'.", dir.toAbsolutePath());
        }

        if (Files.isDirectory(dir))
        {
            int count = 0;

            for (Map.Entry<MapId, MapItemSavedData> entry : data.entrySet())
            {
                Path file = dir.resolve(entry.getKey().key() + ".png");
                writeMapAsImage(file, entry.getValue());
                ++count;
            }

            InfoUtils.showGuiOrInGameMessage(Message.MessageType.INFO, String.format("Wrote %d maps to image files", count));
        }
        else
        {
            InfoUtils.showGuiOrInGameMessage(Message.MessageType.ERROR, "Failed to create directory: " + dir.toAbsolutePath());
        }

        return true;
    }

    private static void writeMapAsImage(Path fileOut, MapItemSavedData state)
    {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < 128; ++y)
        {
            for (int x = 0; x < 128; ++x)
            {
                int index = x + y * 128;
                int color = MapColor.getColorFromPackedId(state.colors[index]);
                // Swap the color channels from ABGR to ARGB
                //int outputColor = (color & 0xFF00FF00) | (color & 0xFF0000) >> 16 | (color & 0xFF) << 16;

                image.setRGB(x, y, color);
            }
        }

        try
        {
            ImageIO.write(image, "png", fileOut.toFile());
        }
        catch (Exception e)
        {
            InfoUtils.showGuiOrInGameMessage(Message.MessageType.ERROR, "Failed to write image to file: " + fileOut.toAbsolutePath());
        }
    }

    public static void setRealTickRate()
    {
        setTickRate(TickUtils.getTickRate());
    }

    public static void setTickRate(float value)
    {
        realTickRate = Math.clamp(value, MIN_TICK_RATE, MAX_TICK_RATE);
    }

    public static float getRealTickRate()
    {
        if (realTickRate < MIN_TICK_RATE || realTickRate > MAX_TICK_RATE)
        {
            setRealTickRate();

            if (realTickRate < MIN_TICK_RATE || realTickRate > MAX_TICK_RATE)
            {
                realTickRate = DEFAULT_TICK_RATE;
            }
        }

        return realTickRate;
    }

    public static boolean isShulkerBox(ItemStack stack)
    {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    public static boolean hasCustomMaxStackSize(ItemStack stack)
    {
        int defaultStackSize = stack.getPrototype().getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
        int currentStackSize = stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
        return defaultStackSize != currentStackSize;
    }

    public static boolean registerPresetFromString(CreateFlatWorldScreen screen, String str)
    {
        Matcher matcher = MiscUtils.PATTERN_WORLD_PRESET.matcher(str);

        if (matcher.matches())
        {
            // TODO --> I added some code here, and added the IMixinCustomizeFlatLevelScreen
            WorldCreationContext generatorOptionsHolder = ((IMixinCustomizeFlatLevelScreen) screen).tweakeroo_getCreateWorldParent().getUiState().getSettings();
            RegistryAccess.Frozen registryManager = generatorOptionsHolder.worldgenLoadContext();
            FeatureFlagSet featureSet = generatorOptionsHolder.dataConfiguration().enabledFeatures();
            HolderGetter<Biome> biomeLookup = registryManager.lookupOrThrow(Registries.BIOME);
            HolderGetter<StructureSet> structureLookup = registryManager.lookupOrThrow(Registries.STRUCTURE_SET);
            HolderGetter<PlacedFeature> featuresLookup = registryManager.lookupOrThrow(Registries.PLACED_FEATURE);
            HolderGetter<Block> blockLookup = registryManager.lookupOrThrow(Registries.BLOCK).filterFeatures(featureSet);
            FlatLevelGeneratorSettings defaultConfig = FlatLevelGeneratorSettings.getDefault(biomeLookup, structureLookup, featuresLookup);
            FlatLevelGeneratorSettings currentConfig = screen.settings();
            Holder.Reference<Biome> referenceEntry = biomeLookup.getOrThrow(Biomes.PLAINS);
            Holder.Reference<Biome> biomeEntry = referenceEntry;

            String name = matcher.group("name");
            String blocksString = matcher.group("blocks");
            String biomeName = matcher.group("biome");
            // TODO add back the features
            String iconItemName = matcher.group("icon");

            try
            {
                Optional<ResourceKey<Biome>> optBiome = Optional.ofNullable(Identifier.tryParse(biomeName)).map((biomeId) ->
                                                                                                                        ResourceKey.create(Registries.BIOME, biomeId));

                biomeEntry = optBiome.flatMap(biomeLookup::get).orElse(referenceEntry);
            }
            catch (Exception ignore)
            {
            }

            if (biomeEntry == null)
            {
                Tweakeroo.LOGGER.error("Invalid biome while parsing flat world string: '{}'", biomeName);
                return false;
            }

            Item item = null;

            try
            {
                Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(Identifier.parse(iconItemName));
                if (opt.isPresent())
                {
                    item = opt.get().value();
                }
            }
            catch (Exception ignore)
            {
            }

            if (item == null)
            {
                Tweakeroo.LOGGER.error("Invalid item for icon while parsing flat world string: '{}'", iconItemName);
                return false;
            }

            List<FlatLayerInfo> layers = MiscTweaks.parseBlockString(blocksString);

            if (layers == null)
            {
                Tweakeroo.LOGGER.error("Failed to get the layers for the flat world preset");
                return false;
            }

            FlatLevelGeneratorSettings newConfig = defaultConfig.withBiomeAndLayers(layers, defaultConfig.structureOverrides(), biomeEntry);

            //new PresetsScreen.SuperflatPresetsListWidget.SuperflatPresetEntry(null);
            //addPreset(Text.translatable(name), item, biome, ImmutableSet.of(), false, false, layers);

            screen.setConfig(newConfig);

            return true;
        }
        else
        {
            Tweakeroo.LOGGER.error("Flat world preset string did not match the regex");
        }

        return false;
    }

	public static void toggleGammaOverrideWithMessage()
    {
        toggleGammaOverrideWithMessage(false);
    }

    public static void toggleGammaOverrideWithMessage(boolean disableCallback)
	{
		boolean orig = FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue();

		if (!orig)
		{
            // Do the same thing as the Key Callback, just without the Warning messages
            if (disableCallback)
            {
                Minecraft mc = Minecraft.getInstance();
                double gamma = Configs.Generic.GAMMA_OVERRIDE_VALUE.getDoubleValue();

                FeatureToggle.TWEAK_GAMMA_OVERRIDE.setEnabledNoCallback();
                Configs.Internal.GAMMA_VALUE_ORIGINAL.setDoubleValue(mc.options.gamma().get());

                @SuppressWarnings("unchecked")
                IMixinSimpleOption<Double> opt = (IMixinSimpleOption<Double>) (Object) mc.options.gamma();

                if (opt != null)
                {
                    opt.tweakeroo_setValueWithoutCheck(gamma);
                }
            }
            else
            {
                FeatureToggle.TWEAK_GAMMA_OVERRIDE.setBooleanValue(true);
            }

			InfoUtils.printBooleanConfigToggleMessage(FeatureToggle.TWEAK_GAMMA_OVERRIDE.getPrettyName(), true);
		}
		else
		{
			FeatureToggle.TWEAK_GAMMA_OVERRIDE.setBooleanValue(false);
			InfoUtils.printBooleanConfigToggleMessage(FeatureToggle.TWEAK_GAMMA_OVERRIDE.getPrettyName(), false);
		}
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
            this.lastActive = Util.getNanos();
            this.active = true;
        }

        public PostKeyAction(double lastDoubleValue)
        {
            this.lastDoubleValue = lastDoubleValue;
            this.lastIntValue = -1;
            this.lastActive = Util.getNanos();
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
            this.lastActive = Util.getNanos();
            this.active = false;
        }
    }
}
