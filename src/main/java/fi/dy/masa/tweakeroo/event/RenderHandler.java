package fi.dy.masa.tweakeroo.event;

import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.ActiveMode;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import fi.dy.masa.malilib.util.data.tag.CompoundData;
import fi.dy.masa.malilib.util.data.tag.ListData;
import fi.dy.masa.malilib.util.nbt.NbtInventory;
import fi.dy.masa.malilib.util.nbt.NbtKeys;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.data.EntityDataManager;
import fi.dy.masa.tweakeroo.renderer.InventoryOverlayHandler;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

public class RenderHandler implements IRenderer
{
    private static final RenderHandler INSTANCE = new RenderHandler();
    private final Minecraft mc;
    private Pair<Entity, CompoundData> lastEnderItems;

    public RenderHandler()
    {
        this.mc = Minecraft.getInstance();
        this.lastEnderItems = null;
    }

    public static RenderHandler getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onRenderGameOverlayPostAdvanced(GuiContext ctx, float partialTicks, ProfilerFiller profiler)
    {
        if (FeatureToggle.TWEAK_HOTBAR_SWAP.getBooleanValue() &&
            Hotkeys.HOTBAR_SWAP_BASE.getKeybind().isKeybindHeld())
        {
            RenderUtils.renderHotbarSwapOverlay(ctx);
        }
        else if (FeatureToggle.TWEAK_HOTBAR_SCROLL.getBooleanValue() &&
                 Hotkeys.HOTBAR_SCROLL.getKeybind().isKeybindHeld())
        {
            RenderUtils.renderHotbarScrollOverlay(ctx);
        }

        if (FeatureToggle.TWEAK_INVENTORY_PREVIEW.getBooleanValue() &&
            Hotkeys.INVENTORY_PREVIEW.getKeybind().isKeybindHeld())
        {
            /*
            InventoryOverlay.Context context = RayTraceUtils.getTargetInventory(mc);

            if (context != null)
            {
                RenderUtils.renderInventoryOverlay(context, drawContext);
            }
             */

            InventoryOverlayHandler.getInstance().getRenderContext(ctx, profiler);
        }

        if (FeatureToggle.TWEAK_PLAYER_INVENTORY_PEEK.getBooleanValue() &&
            Hotkeys.PLAYER_INVENTORY_PEEK.getKeybind().isKeybindHeld())
        {
            RenderUtils.renderPlayerInventoryOverlay(ctx);
        }

        if (FeatureToggle.TWEAK_SNAP_AIM.getBooleanValue() &&
            Configs.Generic.SNAP_AIM_INDICATOR.getBooleanValue())
        {
            RenderUtils.renderSnapAimAngleIndicator(ctx);
        }

        if (FeatureToggle.TWEAK_ELYTRA_CAMERA.getBooleanValue())
        {
            ActiveMode mode = (ActiveMode) Configs.Generic.ELYTRA_CAMERA_INDICATOR.getOptionListValue();

            if (mode == ActiveMode.ALWAYS || (mode == ActiveMode.WITH_KEY && Hotkeys.ELYTRA_CAMERA.getKeybind().isKeybindHeld()))
            {
                RenderUtils.renderPitchLockIndicator(ctx);
            }
        }
    }

    @Override
    public void onRenderTooltipLast(GuiContext ctx, ItemStack stack, int x, int y)
    {
        Item item = stack.getItem();
        if (item instanceof MapItem)
        {
            if (FeatureToggle.TWEAK_MAP_PREVIEW.getBooleanValue() &&
                (Configs.Generic.MAP_PREVIEW_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown()))
            {
                fi.dy.masa.malilib.render.RenderUtils.renderMapPreview(ctx, stack, x, y, Configs.Generic.MAP_PREVIEW_SIZE.getIntegerValue(), false);
            }
        }
        else if (stack.getComponents().has(DataComponents.CONTAINER) && InventoryUtils.shulkerBoxHasItems(stack))
        {
            if (FeatureToggle.TWEAK_SHULKERBOX_DISPLAY.getBooleanValue() &&
                (Configs.Generic.SHULKER_DISPLAY_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown()))
            {
                fi.dy.masa.malilib.render.RenderUtils.renderShulkerBoxPreview(ctx, stack, x, y, Configs.Generic.SHULKER_DISPLAY_BACKGROUND_COLOR.getBooleanValue());
            }
        }
        else if (stack.is(Items.ENDER_CHEST) && Configs.Generic.SHULKER_DISPLAY_ENDER_CHEST.getBooleanValue())
        {
            if (FeatureToggle.TWEAK_SHULKERBOX_DISPLAY.getBooleanValue() &&
                (Configs.Generic.SHULKER_DISPLAY_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown()))
            {
                Level world = WorldUtils.getBestWorld(this.mc);
                if (world == null || this.mc.player == null)
                {
                    return;
                }

                Player player = world.getPlayerByUUID(this.mc.player.getUUID());

                if (player != null)
                {
                    Pair<Entity, CompoundData> pair = EntityDataManager.getInstance().requestEntity(world, player.getId());
                    PlayerEnderChestContainer inv;

                    if (pair != null && pair.getRight() != null && pair.getRight().containsLenient(NbtKeys.ENDER_ITEMS))
                    {
                        inv = InventoryUtils.getPlayerEnderItemsFromData(pair.getRight(), world.registryAccess());
                        this.lastEnderItems = pair;
                    }
                    else if (pair != null && pair.getLeft() instanceof Player pe && !pe.getEnderChestInventory().isEmpty())
                    {
                        inv = pe.getEnderChestInventory();
                    }
                    else if (this.lastEnderItems != null)
                    {
                        inv = InventoryUtils.getPlayerEnderItemsFromData(this.lastEnderItems.getRight(), world.registryAccess());
                    }
                    else
                    {
                        // Last Ditch effort
                        inv = player.getEnderChestInventory();
                    }

	                if (inv != null)
	                {
		                try (NbtInventory nbtInv = NbtInventory.fromInventory(inv))
		                {
                            CompoundData data = new CompoundData();
			                ListData list = nbtInv.toDataList(world.registryAccess());

                            data.put(NbtKeys.ENDER_ITEMS, list);
			                fi.dy.masa.malilib.render.RenderUtils.renderDataItemsPreview(ctx, stack, data, x, y, false);
		                }
		                catch (Exception ignored) { }
	                }
                }
            }
        }
        else if (stack.getComponents().has(DataComponents.BUNDLE_CONTENTS) && InventoryUtils.bundleHasItems(stack))
        {
            if (FeatureToggle.TWEAK_BUNDLE_DISPLAY.getBooleanValue() &&
                (Configs.Generic.BUNDLE_DISPLAY_REQUIRE_SHIFT.getBooleanValue() == false || GuiBase.isShiftDown()))
            {
                fi.dy.masa.malilib.render.RenderUtils.renderBundlePreview(ctx, stack, x, y, Configs.Generic.BUNDLE_DISPLAY_ROW_WIDTH.getIntegerValue(), Configs.Generic.BUNDLE_DISPLAY_BACKGROUND_COLOR.getBooleanValue());
            }
        }
    }

    @Override
    public void onRenderWorldLastAdvanced(RenderTarget fb, Matrix4f posMatrix, Matrix4f projMatrix, Frustum frustum, Camera camera, RenderBuffers buffers, ProfilerFiller profiler)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null)
        {
            RenderTweaks.render(posMatrix, projMatrix, profiler);
            this.renderOverlays(posMatrix, mc);
        }
    }

    private void renderOverlays(Matrix4f posMatrix, Minecraft mc)
    {
        Entity entity = mc.getCameraEntity();

        if (FeatureToggle.TWEAK_FLEXIBLE_BLOCK_PLACEMENT.getBooleanValue() &&
            entity != null &&
            mc.hitResult != null &&
            mc.hitResult.getType() == HitResult.Type.BLOCK &&
            (Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_ROTATION.getKeybind().isKeybindHeld() ||
             Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_OFFSET.getKeybind().isKeybindHeld() ||
             Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_ADJACENT.getKeybind().isKeybindHeld()))
        {
            BlockHitResult hitResult = (BlockHitResult) mc.hitResult;
            Color4f color = Configs.Generic.FLEXIBLE_PLACEMENT_OVERLAY_COLOR.getColor();

            fi.dy.masa.malilib.render.RenderUtils.renderBlockTargetingOverlay(
                    entity,
                    hitResult.getBlockPos(),
                    hitResult.getDirection(),
                    hitResult.getLocation(),
                    color, posMatrix);
        }
    }
}
