package fi.dy.masa.tweakeroo.renderer;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.config.HudAlignment;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.render.InventoryOverlayType;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.util.SnapAimMode;
import fi.dy.masa.tweakeroo.util.SnapAimUtils;

public class RenderUtils
{
    private static long lastRotationChangeTime;

    public static void renderHotbarSwapOverlay(GuiContext ctx)
    {
        Player player = ctx.mc().player;

        if (player != null && ctx.mc().gui.screen() == null)
        {
            final int scaledWidth = GuiUtils.getScaledWindowWidth();
            final int scaledHeight = GuiUtils.getScaledWindowHeight();
            final int offX = Configs.Generic.HOTBAR_SWAP_OVERLAY_OFFSET_X.getIntegerValue();
            final int offY = Configs.Generic.HOTBAR_SWAP_OVERLAY_OFFSET_Y.getIntegerValue();
            int startX = offX;
            int startY = offY;

            HudAlignment align = (HudAlignment) Configs.Generic.HOTBAR_SWAP_OVERLAY_ALIGNMENT.getOptionListValue();

            switch (align)
            {
                case TOP_RIGHT:
                    startX = (int) scaledWidth - offX - 9 * 18;
                    break;
                case BOTTOM_LEFT:
                    startY = (int) scaledHeight - offY - 3 * 18;
                    break;
                case BOTTOM_RIGHT:
                    startX = (int) scaledWidth - offX - 9 * 18;
                    startY = (int) scaledHeight - offY - 3 * 18;
                    break;
                case CENTER:
                    startX = (int) scaledWidth / 2 - offX - 9 * 18 / 2;
                    startY = (int) scaledHeight / 2 - offY - 3 * 18 / 2;
                    break;
                default:
            }

            int x = startX;
            int y = startY;
            Font textRenderer = ctx.fontRenderer();
            Matrix4f modelViewMatrix = new Matrix4f();

            modelViewMatrix.set(RenderSystem.getModelViewMatrixCopy());
            fi.dy.masa.malilib.render.RenderUtils.drawTexturedRect(ctx, AbstractContainerScreen.INVENTORY_LOCATION, x - 1, y - 1, 7, 83, 9 * 18, 3 * 18);

	        ctx.drawString(textRenderer, "1", x - 10, y +  4, 0xFFFFFF);
	        ctx.drawString(textRenderer, "2", x - 10, y + 22, 0xFFFFFF);
	        ctx.drawString(textRenderer, "3", x - 10, y + 40, 0xFFFFFF);

            for (int row = 1; row <= 3; row++)
            {
                for (int column = 0; column < 9; column++)
                {
                    ItemStack stack = player.getInventory().getItem(row * 9 + column);

                    if (stack.isEmpty() == false)
                    {
                        InventoryOverlay.renderStackAt(ctx, stack, x, y, 1);
                    }

                    x += 18;
                }

                y += 18;
                x = startX;
            }

            RenderSystem.getModelViewMatrixCopy().set(modelViewMatrix);
        }
    }

    public static void renderPlayerInventoryOverlay(GuiContext ctx)
    {
        if (ctx.mc().player == null)
        {
            return;
        }

        Container inv = ctx.mc().player.getInventory();

        int x = GuiUtils.getScaledWindowWidth() / 2 - 176 / 2;
        int y = GuiUtils.getScaledWindowHeight() / 2 + 10;
        int slotOffsetX = 8;
        int slotOffsetY = 8;
	    InventoryOverlayType type = InventoryOverlayType.GENERIC;

        InventoryOverlay.renderInventoryBackground(ctx, type, x, y, 9, 27);
        InventoryOverlay.renderInventoryStacks(ctx, type, inv, x + slotOffsetX, y + slotOffsetY, 9, 9, 27);
    }

    public static void renderHotbarScrollOverlay(GuiContext ctx)
    {
        if (ctx.mc().player == null)
        {
            return;
        }

        Container inv = ctx.mc().player.getInventory();

        final int xCenter = GuiUtils.getScaledWindowWidth() / 2;
        final int yCenter = GuiUtils.getScaledWindowHeight() / 2;
        final int x = xCenter - 176 / 2;
        final int y = yCenter + 6;
        InventoryOverlayType type = InventoryOverlayType.GENERIC;

        InventoryOverlay.renderInventoryBackground(ctx, type, x, y     , 9, 27);
        InventoryOverlay.renderInventoryBackground(ctx, type, x, y + 70, 9,  9);

        // Main inventory
        InventoryOverlay.renderInventoryStacks(ctx, type, inv, x + 8, y +  8, 9, 9, 27);
        // Hotbar
        InventoryOverlay.renderInventoryStacks(ctx, type, inv, x + 8, y + 78, 9, 0,  9);

        int currentRow = Configs.Internal.HOTBAR_SCROLL_CURRENT_ROW.getIntegerValue();
        fi.dy.masa.malilib.render.RenderUtils.drawOutline(ctx, x + 5, y + currentRow * 18 + 5, 9 * 18 + 4, 22, 2, 0xFFFF2020);
    }

    public static float calculateLiquidFogDistance(Entity entity, float originalFog, boolean water)
    {
        if (entity instanceof LivingEntity living)
        {
            ItemStack head = living.getItemBySlot(EquipmentSlot.HEAD);

            if (head.isEmpty() == false)
            {
                ItemEnchantments enchants = head.getEnchantments();
                float lavaFog = (originalFog > 1.0f) ? 3.3f : 1.3f;
                float fog = water ? (originalFog / 4) : lavaFog;
                int resp = 0;
                int aqua = 0;

                if (enchants.equals(ItemEnchantments.EMPTY) == false)
                {
                    Set<Holder<Enchantment>> enchantList = enchants.keySet();

                    for (Holder<Enchantment> entry : enchantList)
                    {
                        if (entry.is(Enchantments.AQUA_AFFINITY))
                        {
                            aqua = enchants.getLevel(entry);
                        }
                        if (entry.is(Enchantments.RESPIRATION))
                        {
                            resp = enchants.getLevel(entry);
                        }
                    }
                }

                if (aqua > 0)
                {
                    fog *= 1.6f;
                }

                if (resp > 0)
                {
                    fog *= (float) resp * 1.6f;
                }

                // Custom add the adjustment with some fancy math for water so it's not too
                // overpowered, and not too underpowered either.
                // Gets around +40-50f with max enchants, and around +10-14f with one enchant.
                if (water)
                {
                    fog = Math.max((fog / 2.8f), (originalFog / 4.4f)) + originalFog;
                }

//                Tweakeroo.LOGGER.info("calculateLiquidFogDistance: aqua [{}] resp [{}] orig: [{}] -> adjusted [{}]", aqua, resp, originalFog, fog);
                return Math.max(fog, originalFog);
            }
        }

        return originalFog;
    }

    public static void notifyRotationChanged()
    {
        lastRotationChangeTime = System.currentTimeMillis();
    }

    public static void renderSnapAimAngleIndicator(GuiContext ctx)
    {
        long current = System.currentTimeMillis();

        if (current - lastRotationChangeTime < 750)
        {
            Minecraft mc = Minecraft.getInstance();
            final int xCenter = GuiUtils.getScaledWindowWidth() / 2;
            final int yCenter = GuiUtils.getScaledWindowHeight() / 2;
            SnapAimMode mode = (SnapAimMode) Configs.Generic.SNAP_AIM_MODE.getOptionListValue();

            if (mode != SnapAimMode.PITCH)
            {
                renderSnapAimAngleIndicatorYaw(ctx, xCenter, yCenter, 80, 12);
            }

            if (mode != SnapAimMode.YAW)
            {
                renderSnapAimAngleIndicatorPitch(ctx, xCenter, yCenter, 12, 50);
            }
        }
    }

    private static void renderSnapAimAngleIndicatorYaw(GuiContext ctx, int xCenter, int yCenter, int width, int height)
    {
        double step = Configs.Generic.SNAP_AIM_YAW_STEP.getDoubleValue();
        double realYaw = Mth.positiveModulo(SnapAimUtils.getLastRealYaw(), 360.0D);
        double snappedYaw = SnapAimUtils.calculateSnappedAngle(realYaw, step);
        double startYaw = snappedYaw - (step / 2.0);
        final int x = xCenter - width / 2;
        final int y = yCenter + 10;
        int lineX = x + (int) ((Mth.wrapDegrees(realYaw - startYaw)) / step * width);
        Font textRenderer = ctx.fontRenderer();
        int bgColor = Configs.Generic.SNAP_AIM_INDICATOR_COLOR.getIntegerValue();

        // Draw the main box
        fi.dy.masa.malilib.render.RenderUtils.drawOutlinedBox(ctx, x, y, width, height, bgColor, 0xFFFFFFFF);

        String str = Mth.wrapDegrees(snappedYaw) + "°";
	    ctx.drawString(textRenderer, str, xCenter - textRenderer.width(str) / 2, y + height + 2, 0xFFFFFFFF, false);

        str = "<  " + Mth.wrapDegrees(snappedYaw - step) + "°";
	    ctx.drawString(textRenderer, str, x - textRenderer.width(str), y + height + 2, 0xFFFFFFFF, false);

        str = Mth.wrapDegrees(snappedYaw + step) + "°  >";
	    ctx.drawString(textRenderer, str, x + width, y + height + 2, 0xFFFFFFFF, false);

        if (Configs.Generic.SNAP_AIM_ONLY_CLOSE_TO_ANGLE.getBooleanValue())
        {
            double threshold = Configs.Generic.SNAP_AIM_THRESHOLD_YAW.getDoubleValue();
            int snapThreshOffset = (int) (width * threshold / step);

            // Draw the middle region
            fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, xCenter - snapThreshOffset, y, snapThreshOffset * 2, height, 0x6050FF50);

            if (threshold < (step / 2.0))
            {
                fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, xCenter - snapThreshOffset, y, 2, height, 0xFF20FF20);
                fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, xCenter + snapThreshOffset, y, 2, height, 0xFF20FF20);
            }
        }

        // Draw the current angle indicator
        fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, lineX, y, 2, height, 0xFFFFFFFF);
    }

    private static void renderSnapAimAngleIndicatorPitch(GuiContext ctx, int xCenter, int yCenter, int width, int height)
    {
        double step = Configs.Generic.SNAP_AIM_PITCH_STEP.getDoubleValue();
        int limit = Configs.Generic.SNAP_AIM_PITCH_OVERSHOOT.getBooleanValue() ? 180 : 90;
        //double realPitch = MathHelper.clamp(MathHelper.wrapDegrees(MiscUtils.getLastRealPitch()), -limit, limit);
        double realPitch = Mth.wrapDegrees(SnapAimUtils.getLastRealPitch());
        double snappedPitch;

        if (realPitch < 0)
        {
            snappedPitch = -SnapAimUtils.calculateSnappedAngle(-realPitch, step);
        }
        else
        {
            snappedPitch = SnapAimUtils.calculateSnappedAngle(realPitch, step);
        }

        snappedPitch = Mth.clamp(Mth.wrapDegrees(snappedPitch), -limit, limit);

        int x = xCenter - width / 2;
        int y = yCenter - height - 10;

        renderPitchIndicator(ctx, x, y, width, height, realPitch, snappedPitch, step, true);
    }

    public static void renderPitchLockIndicator(GuiContext ctx)
    {
        if (ctx.mc().player == null)
        {
            return;
        }

        final int xCenter = GuiUtils.getScaledWindowWidth() / 2;
        final int yCenter = GuiUtils.getScaledWindowHeight() / 2;
        int width = 12;
        int height = 50;
        int x = xCenter - width / 2;
        int y = yCenter - height - 10;
        double currentPitch = ctx.mc().player.getXRot();
        double centerPitch = 0;
        double indicatorRange = 180;

        renderPitchIndicator(ctx, x, y, width, height, currentPitch, centerPitch, indicatorRange, false);
    }

    private static void renderPitchIndicator(GuiContext ctx, int x, int y, int width, int height,
                                             double currentPitch, double centerPitch,
                                             double indicatorRange, boolean isSnapRange)
    {
        double startPitch = centerPitch - (indicatorRange / 2.0);
        double printedRange = isSnapRange ? indicatorRange : indicatorRange / 2;
        int lineY = y + (int) ((Mth.wrapDegrees(currentPitch) - startPitch) / indicatorRange * height);
        double angleUp = centerPitch - printedRange;
        double angleDown = centerPitch + printedRange;

        Font textRenderer = ctx.fontRenderer();

        if (isSnapRange)
        {
            String strUp   = String.format("%6.1f° ^", Mth.wrapDegrees(angleUp));
            String strDown = String.format("%6.1f° v", Mth.wrapDegrees(angleDown));
	        ctx.drawString(textRenderer, strUp, x + width + 4, y - 4, 0xFFFFFFFF, false);
	        ctx.drawString(textRenderer, strDown, x + width + 4, y + height - 4, 0xFFFFFFFF, false);

            String str = String.format("%6.1f°", Mth.wrapDegrees(isSnapRange ? centerPitch : currentPitch));
	        ctx.drawString(textRenderer, str, x + width + 4, y + height / 2 - 4, 0xFFFFFFFF, false);
        }
        else
        {
            String str = String.format("%4.1f°", Mth.wrapDegrees(isSnapRange ? centerPitch : currentPitch));
	        ctx.drawString(textRenderer, str, x + width + 4, lineY - 4, 0xFFFFFFFF, false);
        }

        int bgColor = Configs.Generic.SNAP_AIM_INDICATOR_COLOR.getIntegerValue();
        // Draw the main box
        fi.dy.masa.malilib.render.RenderUtils.drawOutlinedBox(ctx, x, y, width, height, bgColor, 0xFFFFFFFF);

        int yCenter = y + height / 2 - 1;

        if (isSnapRange && Configs.Generic.SNAP_AIM_ONLY_CLOSE_TO_ANGLE.getBooleanValue())
        {
            double step = Configs.Generic.SNAP_AIM_YAW_STEP.getDoubleValue();
            double threshold = Configs.Generic.SNAP_AIM_THRESHOLD_PITCH.getDoubleValue();
            int snapThreshOffset = (int) ((double) height * threshold / indicatorRange);

            fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, x, yCenter - snapThreshOffset, width, snapThreshOffset * 2, 0x6050FF50);

            if (threshold < (step / 2.0))
            {
                fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, x, yCenter - snapThreshOffset, width, 2, 0xFF20FF20);
                fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, x, yCenter + snapThreshOffset, width, 2, 0xFF20FF20);
            }
        }
        else if (isSnapRange == false)
        {
            fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, x + 1, yCenter - 1, width - 2, 2, 0xFFC0C0C0);
        }

        // Draw the current angle indicator
        fi.dy.masa.malilib.render.RenderUtils.drawRect(ctx, x, lineY - 1, width, 2, 0xFFFFFFFF);
    }
}
