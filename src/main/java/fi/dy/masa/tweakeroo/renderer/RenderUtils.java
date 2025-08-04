package fi.dy.masa.tweakeroo.renderer;

import java.util.HashSet;
import java.util.Set;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.*;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import fi.dy.masa.malilib.config.HudAlignment;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.util.Color4f;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.game.BlockUtils;
import fi.dy.masa.malilib.util.nbt.NbtBlockUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.util.SnapAimMode;
import fi.dy.masa.tweakeroo.util.SnapAimUtils;

public class RenderUtils
{
    private static long lastRotationChangeTime;

    public static void renderHotbarSwapOverlay(MinecraftClient mc, DrawContext drawContext)
    {
        PlayerEntity player = mc.player;

        if (player != null && mc.currentScreen == null)
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
            TextRenderer textRenderer = mc.textRenderer;

            Matrix4f modelViewMatrix = new Matrix4f();
            modelViewMatrix.set(RenderSystem.getModelViewMatrix());
            fi.dy.masa.malilib.render.RenderUtils.color(1f, 1f, 1f, 1f);
            fi.dy.masa.malilib.render.RenderUtils.bindTexture(HandledScreen.BACKGROUND_TEXTURE);
            fi.dy.masa.malilib.render.RenderUtils.drawTexturedRect(x - 1, y - 1, 7, 83, 9 * 18, 3 * 18);

            drawContext.drawTextWithShadow(textRenderer, "1", x - 10, y +  4, 0xFFFFFF);
            drawContext.drawTextWithShadow(textRenderer, "2", x - 10, y + 22, 0xFFFFFF);
            drawContext.drawTextWithShadow(textRenderer, "3", x - 10, y + 40, 0xFFFFFF);

            for (int row = 1; row <= 3; row++)
            {
                for (int column = 0; column < 9; column++)
                {
                    ItemStack stack = player.getInventory().getStack(row * 9 + column);

                    if (stack.isEmpty() == false)
                    {
                        InventoryOverlay.renderStackAt(stack, x, y, 1, mc, drawContext);
                    }

                    x += 18;
                }

                y += 18;
                x = startX;
            }

            RenderSystem.getModelViewMatrix().set(modelViewMatrix);
        }
    }

    // OG Method
    public static void renderInventoryOverlay(InventoryOverlay.Context context, DrawContext drawContext)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        LivingEntity entityLivingBase = null;
        BlockEntity be = null;
        Inventory inv = null;
        NbtCompound nbt = new NbtCompound();

        if (context.be() != null)
        {
            be = context.be();
        }
        else if (context.entity() != null)
        {
            if (context.entity() instanceof LivingEntity)
            {
                entityLivingBase = context.entity();
            }
        }
        if (context.inv() != null)
        {
            inv = context.inv();
        }
        if (context.nbt() != null)
        {
            nbt.copyFrom(context.nbt());
        }

        //Tweakeroo.logger.error("render: ctx-type [{}], inv [{}], raw Nbt [{}]", context.type().toString(), inv != null ? inv.size() : "null", nbt.isEmpty() ? "empty" : nbt.toString());

        final boolean isWolf = (entityLivingBase instanceof WolfEntity);
        final int xCenter = GuiUtils.getScaledWindowWidth() / 2;
        final int yCenter = GuiUtils.getScaledWindowHeight() / 2;
        int x = xCenter - 52 / 2;
        int y = yCenter - 92;

        if (inv != null && inv.size() > 0)
        {
            final boolean isHorse = (entityLivingBase instanceof AbstractHorseEntity);
            final int totalSlots = isHorse ? inv.size() - 1 : inv.size();
            final int firstSlot = isHorse ? 1 : 0;

            InventoryOverlay.InventoryRenderType type = (entityLivingBase instanceof VillagerEntity) ? InventoryOverlay.InventoryRenderType.VILLAGER : InventoryOverlay.getBestInventoryType(inv, nbt, context);
            final InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type, totalSlots);
            final int rows = (int) Math.ceil((double) totalSlots / props.slotsPerRow);
            Set<Integer> lockedSlots = new HashSet<>();
            int xInv = xCenter - (props.width / 2);
            int yInv = yCenter - props.height - 6;

            if (rows > 6)
            {
                yInv -= (rows - 6) * 18;
                y -= (rows - 6) * 18;
            }

            if (entityLivingBase != null)
            {
                x = xCenter - 55;
                xInv = xCenter + 2;
                yInv = Math.min(yInv, yCenter - 92);
            }

            if (be != null && type == InventoryOverlay.InventoryRenderType.CRAFTER)
            {
                if (be instanceof CrafterBlockEntity cbe)
                {
                    lockedSlots = BlockUtils.getDisabledSlots(cbe);
                }
                else if (context.nbt() != null)
                {
                    lockedSlots = NbtBlockUtils.getDisabledSlotsFromNbt(context.nbt());
                }
            }

            //Tweakeroo.logger.warn("renderInventoryOverlay: type [{}] // Nbt Type [{}] // inv.isEmpty({})", type.toString(), context.nbt() != null ? InventoryOverlay.getInventoryType(context.nbt()) : "INVALID", inv.isEmpty());

            if (context.be() != null && context.be().getCachedState().getBlock() instanceof ShulkerBoxBlock sbb)
            {
                fi.dy.masa.malilib.render.RenderUtils.setShulkerboxBackgroundTintColor(sbb, Configs.Generic.SHULKER_DISPLAY_BACKGROUND_COLOR.getBooleanValue());
            }

            if (isHorse)
            {
                Inventory horseInv = new SimpleInventory(2);
                ItemStack horseArmor = (((AbstractHorseEntity) entityLivingBase).getBodyArmor());
                horseInv.setStack(0, horseArmor != null && !horseArmor.isEmpty() ? horseArmor : ItemStack.EMPTY);
                horseInv.setStack(1, inv.getStack(0));

                InventoryOverlay.renderInventoryBackground(type, xInv, yInv, 1, 2, mc);
                InventoryOverlay.renderInventoryBackgroundSlots(type, horseInv, xInv + props.slotOffsetX, yInv + props.slotOffsetY, drawContext);
                InventoryOverlay.renderInventoryStacks(type, horseInv, xInv + props.slotOffsetX, yInv + props.slotOffsetY, 1, 0, 2, mc, drawContext);
                xInv += 32 + 4;
            }

            if (totalSlots > 0)
            {
                InventoryOverlay.renderInventoryBackground(type, xInv, yInv, props.slotsPerRow, totalSlots, mc);
                // TODO 1.21.4+
                /*
                if (type == InventoryOverlay.InventoryRenderType.BREWING_STAND)
                {
                    InventoryOverlay.renderBrewerBackgroundSlots(inv, xInv, yInv, drawContext);
                }
                 */
                InventoryOverlay.renderInventoryStacks(type, inv, xInv + props.slotOffsetX, yInv + props.slotOffsetY, props.slotsPerRow, firstSlot, totalSlots, lockedSlots, mc, drawContext);
            }
        }

        if (isWolf)
        {
            InventoryOverlay.InventoryRenderType type = InventoryOverlay.InventoryRenderType.HORSE;
            final InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type, 2);
            final int rows = (int) Math.ceil((double) 2 / props.slotsPerRow);
            int xInv;
            int yInv = yCenter - props.height - 6;

            if (rows > 6)
            {
                yInv -= (rows - 6) * 18;
                y -= (rows - 6) * 18;
            }

            x = xCenter - 55;
            xInv = xCenter + 2;
            yInv = Math.min(yInv, yCenter - 92);

            Inventory wolfInv = new SimpleInventory(2);
            ItemStack wolfArmor = ((WolfEntity) entityLivingBase).getBodyArmor();
            wolfInv.setStack(0, wolfArmor != null && !wolfArmor.isEmpty() ? wolfArmor : ItemStack.EMPTY);
            InventoryOverlay.renderInventoryBackground(type, xInv, yInv, 1, 2, mc);
            InventoryOverlay.renderWolfArmorBackgroundSlots(wolfInv, xInv + props.slotOffsetX, yInv + props.slotOffsetY, drawContext);
            InventoryOverlay.renderInventoryStacks(type, wolfInv, xInv + props.slotOffsetX, yInv + props.slotOffsetY, 1, 0, 2, mc, drawContext);
        }

        if (entityLivingBase != null)
        {
            InventoryOverlay.renderEquipmentOverlayBackground(x, y, entityLivingBase, drawContext);
            InventoryOverlay.renderEquipmentStacks(entityLivingBase, x, y, mc, drawContext);
        }
    }

    public static void renderPlayerInventoryOverlay(MinecraftClient mc, DrawContext drawContext)
    {
        if (mc.player == null)
        {
            return;
        }

        Inventory inv = mc.player.getInventory();

        int x = GuiUtils.getScaledWindowWidth() / 2 - 176 / 2;
        int y = GuiUtils.getScaledWindowHeight() / 2 + 10;
        int slotOffsetX = 8;
        int slotOffsetY = 8;
        InventoryOverlay.InventoryRenderType type = InventoryOverlay.InventoryRenderType.GENERIC;

        fi.dy.masa.malilib.render.RenderUtils.color(1f, 1f, 1f, 1f);

        InventoryOverlay.renderInventoryBackground(type, x, y, 9, 27, mc);
        InventoryOverlay.renderInventoryStacks(type, inv, x + slotOffsetX, y + slotOffsetY, 9, 9, 27, mc, drawContext);
    }

    public static void renderHotbarScrollOverlay(MinecraftClient mc, DrawContext drawContext)
    {
        if (mc.player == null)
        {
            return;
        }

        Inventory inv = mc.player.getInventory();

        final int xCenter = GuiUtils.getScaledWindowWidth() / 2;
        final int yCenter = GuiUtils.getScaledWindowHeight() / 2;
        final int x = xCenter - 176 / 2;
        final int y = yCenter + 6;
        InventoryOverlay.InventoryRenderType type = InventoryOverlay.InventoryRenderType.GENERIC;

        fi.dy.masa.malilib.render.RenderUtils.color(1f, 1f, 1f, 1f);

        InventoryOverlay.renderInventoryBackground(type, x, y     , 9, 27, mc);
        InventoryOverlay.renderInventoryBackground(type, x, y + 70, 9,  9, mc);

        // Main inventory
        InventoryOverlay.renderInventoryStacks(type, inv, x + 8, y +  8, 9, 9, 27, mc, drawContext);
        // Hotbar
        InventoryOverlay.renderInventoryStacks(type, inv, x + 8, y + 78, 9, 0,  9, mc, drawContext);

        int currentRow = Configs.Internal.HOTBAR_SCROLL_CURRENT_ROW.getIntegerValue();
        fi.dy.masa.malilib.render.RenderUtils.drawOutline(x + 5, y + currentRow * 18 + 5, 9 * 18 + 4, 22, 2, 0xFFFF2020);
    }

    public static float getLavaFogDistance(Entity entity, float originalFog)
    {
        if (entity instanceof LivingEntity living)
        {
            ItemStack head = living.getEquippedStack(EquipmentSlot.HEAD);

            if (head.isEmpty() == false)
            {
                ItemEnchantmentsComponent enchants = head.getEnchantments();
                float fog = (originalFog > 1.0f) ? 3.3f : 1.3f;
                int resp = 0;
                int aqua = 0;

                if (enchants.equals(ItemEnchantmentsComponent.DEFAULT) == false)
                {
                    Set<RegistryEntry<Enchantment>> enchantList = enchants.getEnchantments();

                    for (RegistryEntry<Enchantment> entry : enchantList)
                    {
                        if (entry.matchesKey(Enchantments.AQUA_AFFINITY))
                        {
                            aqua = enchants.getLevel(entry);
                        }
                        if (entry.matchesKey(Enchantments.RESPIRATION))
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

                //Tweakeroo.logger.info("getLavaFogDistance: aqua {} resp {} orig: {} adjusted {}", aqua, resp, originalFog, fog);

                return Math.max(fog, originalFog);
            }
        }

        return originalFog;
    }

    public static void renderDirectionsCursor(DrawContext drawContext)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        Camera camera = mc.gameRenderer.getCamera();
        float width = (float) drawContext.getScaledWindowWidth() / 2;
        float height = (float) drawContext.getScaledWindowHeight() / 2;
        float pitch = camera.getPitch();
        float yaw = camera.getYaw();

        RenderSystem.enableBlend();
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul(drawContext.getMatrices().peek().getPositionMatrix());
        matrix4fStack.translate(width, height, 0.0F);
        matrix4fStack.rotateX(fi.dy.masa.malilib.render.RenderUtils.matrix4fRotateFix(-pitch));
        matrix4fStack.rotateY(fi.dy.masa.malilib.render.RenderUtils.matrix4fRotateFix(yaw));
        matrix4fStack.scale(-1.0F, -1.0F, -1.0F);
        RenderSystem.renderCrosshair(10);
        matrix4fStack.popMatrix();
        //RenderSystem.applyModelViewMatrix();
        RenderSystem.disableBlend();
    }

    public static void notifyRotationChanged()
    {
        lastRotationChangeTime = System.currentTimeMillis();
    }

    public static void renderSnapAimAngleIndicator(DrawContext drawContext)
    {
        long current = System.currentTimeMillis();

        if (current - lastRotationChangeTime < 750)
        {
            MinecraftClient mc = MinecraftClient.getInstance();
            final int xCenter = GuiUtils.getScaledWindowWidth() / 2;
            final int yCenter = GuiUtils.getScaledWindowHeight() / 2;
            SnapAimMode mode = (SnapAimMode) Configs.Generic.SNAP_AIM_MODE.getOptionListValue();

            if (mode != SnapAimMode.PITCH)
            {
                renderSnapAimAngleIndicatorYaw(xCenter, yCenter, 80, 12, mc, drawContext);
            }

            if (mode != SnapAimMode.YAW)
            {
                renderSnapAimAngleIndicatorPitch(xCenter, yCenter, 12, 50, mc, drawContext);
            }
        }
    }

    private static void renderSnapAimAngleIndicatorYaw(int xCenter, int yCenter, int width, int height, MinecraftClient mc, DrawContext drawContext)
    {
        double step = Configs.Generic.SNAP_AIM_YAW_STEP.getDoubleValue();
        double realYaw = MathHelper.floorMod(SnapAimUtils.getLastRealYaw(), 360.0D);
        double snappedYaw = SnapAimUtils.calculateSnappedAngle(realYaw, step);
        double startYaw = snappedYaw - (step / 2.0);
        final int x = xCenter - width / 2;
        final int y = yCenter + 10;
        int lineX = x + (int) ((MathHelper.wrapDegrees(realYaw - startYaw)) / step * width);
        TextRenderer textRenderer = mc.textRenderer;

        fi.dy.masa.malilib.render.RenderUtils.color(1f, 1f, 1f, 1f);

        int bgColor = Configs.Generic.SNAP_AIM_INDICATOR_COLOR.getIntegerValue();

        // Draw the main box
        fi.dy.masa.malilib.render.RenderUtils.drawOutlinedBox(x, y, width, height, bgColor, 0xFFFFFFFF);

        String str = MathHelper.wrapDegrees(snappedYaw) + "°";
        drawContext.drawText(textRenderer, str, xCenter - textRenderer.getWidth(str) / 2, y + height + 2, 0xFFFFFFFF, false);

        str = "<  " + MathHelper.wrapDegrees(snappedYaw - step) + "°";
        drawContext.drawText(textRenderer, str, x - textRenderer.getWidth(str), y + height + 2, 0xFFFFFFFF, false);

        str = MathHelper.wrapDegrees(snappedYaw + step) + "°  >";
        drawContext.drawText(textRenderer, str, x + width, y + height + 2, 0xFFFFFFFF, false);

        if (Configs.Generic.SNAP_AIM_ONLY_CLOSE_TO_ANGLE.getBooleanValue())
        {
            double threshold = Configs.Generic.SNAP_AIM_THRESHOLD_YAW.getDoubleValue();
            int snapThreshOffset = (int) (width * threshold / step);

            // Draw the middle region
            fi.dy.masa.malilib.render.RenderUtils.drawRect(xCenter - snapThreshOffset, y, snapThreshOffset * 2, height, 0x6050FF50);

            if (threshold < (step / 2.0))
            {
                fi.dy.masa.malilib.render.RenderUtils.drawRect(xCenter - snapThreshOffset, y, 2, height, 0xFF20FF20);
                fi.dy.masa.malilib.render.RenderUtils.drawRect(xCenter + snapThreshOffset, y, 2, height, 0xFF20FF20);
            }
        }

        // Draw the current angle indicator
        fi.dy.masa.malilib.render.RenderUtils.drawRect(lineX, y, 2, height, 0xFFFFFFFF);
    }

    private static void renderSnapAimAngleIndicatorPitch(int xCenter, int yCenter, int width, int height,
            MinecraftClient mc, DrawContext drawContext)
    {
        double step = Configs.Generic.SNAP_AIM_PITCH_STEP.getDoubleValue();
        int limit = Configs.Generic.SNAP_AIM_PITCH_OVERSHOOT.getBooleanValue() ? 180 : 90;
        //double realPitch = MathHelper.clamp(MathHelper.wrapDegrees(MiscUtils.getLastRealPitch()), -limit, limit);
        double realPitch = MathHelper.wrapDegrees(SnapAimUtils.getLastRealPitch());
        double snappedPitch;

        if (realPitch < 0)
        {
            snappedPitch = -SnapAimUtils.calculateSnappedAngle(-realPitch, step);
        }
        else
        {
            snappedPitch = SnapAimUtils.calculateSnappedAngle(realPitch, step);
        }

        snappedPitch = MathHelper.clamp(MathHelper.wrapDegrees(snappedPitch), -limit, limit);

        int x = xCenter - width / 2;
        int y = yCenter - height - 10;

        renderPitchIndicator(x, y, width, height, realPitch, snappedPitch, step, true, mc, drawContext);
    }

    public static void renderPitchLockIndicator(MinecraftClient mc, DrawContext drawContext)
    {
        if (mc.player == null)
        {
            return;
        }

        final int xCenter = GuiUtils.getScaledWindowWidth() / 2;
        final int yCenter = GuiUtils.getScaledWindowHeight() / 2;
        int width = 12;
        int height = 50;
        int x = xCenter - width / 2;
        int y = yCenter - height - 10;
        double currentPitch = mc.player.getPitch();
        double centerPitch = 0;
        double indicatorRange = 180;

        renderPitchIndicator(x, y, width, height, currentPitch, centerPitch, indicatorRange, false, mc, drawContext);
    }

    private static void renderPitchIndicator(int x, int y, int width, int height,
            double currentPitch, double centerPitch, double indicatorRange, boolean isSnapRange,
            MinecraftClient mc, DrawContext drawContext)
    {
        double startPitch = centerPitch - (indicatorRange / 2.0);
        double printedRange = isSnapRange ? indicatorRange : indicatorRange / 2;
        int lineY = y + (int) ((MathHelper.wrapDegrees(currentPitch) - startPitch) / indicatorRange * height);
        double angleUp = centerPitch - printedRange;
        double angleDown = centerPitch + printedRange;

        fi.dy.masa.malilib.render.RenderUtils.color(1f, 1f, 1f, 1f);
        TextRenderer textRenderer = mc.textRenderer;

        if (isSnapRange)
        {
            String strUp   = String.format("%6.1f° ^", MathHelper.wrapDegrees(angleUp));
            String strDown = String.format("%6.1f° v", MathHelper.wrapDegrees(angleDown));
            drawContext.drawText(textRenderer, strUp, x + width + 4, y - 4, 0xFFFFFFFF, false);
            drawContext.drawText(textRenderer, strDown, x + width + 4, y + height - 4, 0xFFFFFFFF, false);

            String str = String.format("%6.1f°", MathHelper.wrapDegrees(isSnapRange ? centerPitch : currentPitch));
            drawContext.drawText(textRenderer, str, x + width + 4, y + height / 2 - 4, 0xFFFFFFFF, false);
        }
        else
        {
            String str = String.format("%4.1f°", MathHelper.wrapDegrees(isSnapRange ? centerPitch : currentPitch));
            drawContext.drawText(textRenderer, str, x + width + 4, lineY - 4, 0xFFFFFFFF, false);
        }

        int bgColor = Configs.Generic.SNAP_AIM_INDICATOR_COLOR.getIntegerValue();
        // Draw the main box
        fi.dy.masa.malilib.render.RenderUtils.drawOutlinedBox(x, y, width, height, bgColor, 0xFFFFFFFF);

        int yCenter = y + height / 2 - 1;

        if (isSnapRange && Configs.Generic.SNAP_AIM_ONLY_CLOSE_TO_ANGLE.getBooleanValue())
        {
            double step = Configs.Generic.SNAP_AIM_YAW_STEP.getDoubleValue();
            double threshold = Configs.Generic.SNAP_AIM_THRESHOLD_PITCH.getDoubleValue();
            int snapThreshOffset = (int) ((double) height * threshold / indicatorRange);

            fi.dy.masa.malilib.render.RenderUtils.drawRect(x, yCenter - snapThreshOffset, width, snapThreshOffset * 2, 0x6050FF50);

            if (threshold < (step / 2.0))
            {
                fi.dy.masa.malilib.render.RenderUtils.drawRect(x, yCenter - snapThreshOffset, width, 2, 0xFF20FF20);
                fi.dy.masa.malilib.render.RenderUtils.drawRect(x, yCenter + snapThreshOffset, width, 2, 0xFF20FF20);
            }
        }
        else if (isSnapRange == false)
        {
            fi.dy.masa.malilib.render.RenderUtils.drawRect(x + 1, yCenter - 1, width - 2, 2, 0xFFC0C0C0);
        }

        // Draw the current angle indicator
        fi.dy.masa.malilib.render.RenderUtils.drawRect(x, lineY - 1, width, 2, 0xFFFFFFFF);
    }

    public static void renderBlockOutline(BlockPos pos, float expand, float lineWidth, Color4f color, MinecraftClient mc)
    {
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        BuiltBuffer meshData;

        drawBlockBoundingBoxOutlinesBatchedLines(pos, color, expand, buffer, mc);

        try
        {
            meshData = buffer.end();
            BufferRenderer.drawWithGlobalProgram(meshData);
            meshData.close();
        }
        catch (Exception ignored) { }
    }

    public static void drawBlockBoundingBoxOutlinesBatchedLines(BlockPos pos, Color4f color,
                                                                double expand, BufferBuilder buffer, MinecraftClient mc)
    {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        final double dx = cameraPos.x;
        final double dy = cameraPos.y;
        final double dz = cameraPos.z;

        float minX = (float) (pos.getX() - dx - expand);
        float minY = (float) (pos.getY() - dy - expand);
        float minZ = (float) (pos.getZ() - dz - expand);
        float maxX = (float) (pos.getX() - dx + expand + 1);
        float maxY = (float) (pos.getY() - dy + expand + 1);
        float maxZ = (float) (pos.getZ() - dz + expand + 1);

        fi.dy.masa.malilib.render.RenderUtils.drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
    }

    public static void drawConnectingLineBatchedLines(BlockPos pos1, BlockPos pos2, boolean center,
                                                      Color4f color, BufferBuilder buffer, MinecraftClient mc)
    {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        final double dx = cameraPos.x;
        final double dy = cameraPos.y;
        final double dz = cameraPos.z;

        float x1 = (float) (pos1.getX() - dx);
        float y1 = (float) (pos1.getY() - dy);
        float z1 = (float) (pos1.getZ() - dz);
        float x2 = (float) (pos2.getX() - dx);
        float y2 = (float) (pos2.getY() - dy);
        float z2 = (float) (pos2.getZ() - dz);

        if (center)
        {
            x1 += 0.5F;
            y1 += 0.5F;
            z1 += 0.5F;
            x2 += 0.5F;
            y2 += 0.5F;
            z2 += 0.5F;
        }

        buffer.vertex(x1, y1, z1).color(color.r, color.g, color.b, color.a);
        buffer.vertex(x2, y2, z2).color(color.r, color.g, color.b, color.a);
    }

    public static void renderBlockOutlineOverlapping(BlockPos pos, float expand, float lineWidth,
                                                     Color4f color1, Color4f color2, Color4f color3, Matrix4f matrix4f, MinecraftClient mc)
    {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        final double dx = cameraPos.x;
        final double dy = cameraPos.y;
        final double dz = cameraPos.z;

        final float minX = (float) (pos.getX() - dx - expand);
        final float minY = (float) (pos.getY() - dy - expand);
        final float minZ = (float) (pos.getZ() - dz - expand);
        final float maxX = (float) (pos.getX() - dx + expand + 1);
        final float maxY = (float) (pos.getY() - dy + expand + 1);
        final float maxZ = (float) (pos.getZ() - dz + expand + 1);

        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        BuiltBuffer meshData;

        // Min corner
        buffer.vertex(minX, minY, minZ).color(color1.r, color1.g, color1.b, color1.a);
        buffer.vertex(maxX, minY, minZ).color(color1.r, color1.g, color1.b, color1.a);

        buffer.vertex(minX, minY, minZ).color(color1.r, color1.g, color1.b, color1.a);
        buffer.vertex(minX, maxY, minZ).color(color1.r, color1.g, color1.b, color1.a);

        buffer.vertex(minX, minY, minZ).color(color1.r, color1.g, color1.b, color1.a);
        buffer.vertex(minX, minY, maxZ).color(color1.r, color1.g, color1.b, color1.a);

        // Max corner
        buffer.vertex(minX, maxY, maxZ).color(color2.r, color2.g, color2.b, color2.a);
        buffer.vertex(maxX, maxY, maxZ).color(color2.r, color2.g, color2.b, color2.a);

        buffer.vertex(maxX, minY, maxZ).color(color2.r, color2.g, color2.b, color2.a);
        buffer.vertex(maxX, maxY, maxZ).color(color2.r, color2.g, color2.b, color2.a);

        buffer.vertex(maxX, maxY, minZ).color(color2.r, color2.g, color2.b, color2.a);
        buffer.vertex(maxX, maxY, maxZ).color(color2.r, color2.g, color2.b, color2.a);

        // The rest of the edges
        buffer.vertex(minX, maxY, minZ).color(color3.r, color3.g, color3.b, color3.a);
        buffer.vertex(maxX, maxY, minZ).color(color3.r, color3.g, color3.b, color3.a);

        buffer.vertex(minX, minY, maxZ).color(color3.r, color3.g, color3.b, color3.a);
        buffer.vertex(maxX, minY, maxZ).color(color3.r, color3.g, color3.b, color3.a);

        buffer.vertex(maxX, minY, minZ).color(color3.r, color3.g, color3.b, color3.a);
        buffer.vertex(maxX, maxY, minZ).color(color3.r, color3.g, color3.b, color3.a);

        buffer.vertex(minX, minY, maxZ).color(color3.r, color3.g, color3.b, color3.a);
        buffer.vertex(minX, maxY, maxZ).color(color3.r, color3.g, color3.b, color3.a);

        buffer.vertex(maxX, minY, minZ).color(color3.r, color3.g, color3.b, color3.a);
        buffer.vertex(maxX, minY, maxZ).color(color3.r, color3.g, color3.b, color3.a);

        buffer.vertex(minX, maxY, minZ).color(color3.r, color3.g, color3.b, color3.a);
        buffer.vertex(minX, maxY, maxZ).color(color3.r, color3.g, color3.b, color3.a);

        try
        {
            meshData = buffer.end();
            BufferRenderer.drawWithGlobalProgram(meshData);
            meshData.close();
        }
        catch (Exception ignored) { }
    }

    public static void renderAreaOutline(BlockPos pos1, BlockPos pos2, float lineWidth,
                                         Color4f colorX, Color4f colorY, Color4f colorZ, MinecraftClient mc)
    {
        RenderSystem.lineWidth(lineWidth);

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        final double dx = cameraPos.x;
        final double dy = cameraPos.y;
        final double dz = cameraPos.z;

        double minX = Math.min(pos1.getX(), pos2.getX()) - dx;
        double minY = Math.min(pos1.getY(), pos2.getY()) - dy;
        double minZ = Math.min(pos1.getZ(), pos2.getZ()) - dz;
        double maxX = Math.max(pos1.getX(), pos2.getX()) - dx + 1;
        double maxY = Math.max(pos1.getY(), pos2.getY()) - dy + 1;
        double maxZ = Math.max(pos1.getZ(), pos2.getZ()) - dz + 1;

        drawBoundingBoxEdges((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, colorX, colorY, colorZ);
    }

    private static void drawBoundingBoxEdges(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Color4f colorX, Color4f colorY, Color4f colorZ)
    {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        BuiltBuffer meshData;

        drawBoundingBoxLinesX(buffer, minX, minY, minZ, maxX, maxY, maxZ, colorX);
        drawBoundingBoxLinesY(buffer, minX, minY, minZ, maxX, maxY, maxZ, colorY);
        drawBoundingBoxLinesZ(buffer, minX, minY, minZ, maxX, maxY, maxZ, colorZ);

        try
        {
            meshData = buffer.end();
            BufferRenderer.drawWithGlobalProgram(meshData);
            meshData.close();
        }
        catch (Exception ignored) { }
    }

    private static void drawBoundingBoxLinesX(BufferBuilder buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Color4f color)
    {
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a);

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a);

        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a);

        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
    }

    private static void drawBoundingBoxLinesY(BufferBuilder buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Color4f color)
    {
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a);

        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a);

        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a);

        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
    }

    private static void drawBoundingBoxLinesZ(BufferBuilder buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Color4f color)
    {
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a);

        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a);

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a);

        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a);
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a);
    }

    public static void renderAreaSides(BlockPos pos1, BlockPos pos2, Color4f color, Matrix4f matrix4f, MinecraftClient mc)
    {
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        BuiltBuffer meshData;

        renderAreaSidesBatched(pos1, pos2, color, 0.002, buffer, mc);

        try
        {
            meshData = buffer.end();
            BufferRenderer.drawWithGlobalProgram(meshData);
            meshData.close();
        }
        catch (Exception ignored) { }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void renderAreaSidesBatched(BlockPos pos1, BlockPos pos2, Color4f color,
                                              double expand, BufferBuilder buffer, MinecraftClient mc)
    {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        final double dx = cameraPos.x;
        final double dy = cameraPos.y;
        final double dz = cameraPos.z;
        double minX = Math.min(pos1.getX(), pos2.getX()) - dx - expand;
        double minY = Math.min(pos1.getY(), pos2.getY()) - dy - expand;
        double minZ = Math.min(pos1.getZ(), pos2.getZ()) - dz - expand;
        double maxX = Math.max(pos1.getX(), pos2.getX()) + 1 - dx + expand;
        double maxY = Math.max(pos1.getY(), pos2.getY()) + 1 - dy + expand;
        double maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1 - dz + expand;

        fi.dy.masa.malilib.render.RenderUtils.drawBoxAllSidesBatchedQuads((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ, color, buffer);
    }

    public static void renderAreaOutlineNoCorners(BlockPos pos1, BlockPos pos2,
                                                  float lineWidth, Color4f colorX, Color4f colorY, Color4f colorZ, MinecraftClient mc)
    {
        final int xMin = Math.min(pos1.getX(), pos2.getX());
        final int yMin = Math.min(pos1.getY(), pos2.getY());
        final int zMin = Math.min(pos1.getZ(), pos2.getZ());
        final int xMax = Math.max(pos1.getX(), pos2.getX());
        final int yMax = Math.max(pos1.getY(), pos2.getY());
        final int zMax = Math.max(pos1.getZ(), pos2.getZ());

        final double expand = 0.001;
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        final double dx = cameraPos.x;
        final double dy = cameraPos.y;
        final double dz = cameraPos.z;

        final float dxMin = (float) (-dx - expand);
        final float dyMin = (float) (-dy - expand);
        final float dzMin = (float) (-dz - expand);
        final float dxMax = (float) (-dx + expand);
        final float dyMax = (float) (-dy + expand);
        final float dzMax = (float) (-dz + expand);

        final float minX = xMin + dxMin;
        final float minY = yMin + dyMin;
        final float minZ = zMin + dzMin;
        final float maxX = xMax + dxMax;
        final float maxY = yMax + dyMax;
        final float maxZ = zMax + dzMax;

        int start, end;

        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        BuiltBuffer meshData;

        // Edges along the X-axis
        start = (pos1.getX() == xMin && pos1.getY() == yMin && pos1.getZ() == zMin) || (pos2.getX() == xMin && pos2.getY() == yMin && pos2.getZ() == zMin) ? xMin + 1 : xMin;
        end   = (pos1.getX() == xMax && pos1.getY() == yMin && pos1.getZ() == zMin) || (pos2.getX() == xMax && pos2.getY() == yMin && pos2.getZ() == zMin) ? xMax : xMax + 1;

        if (end > start)
        {
            buffer.vertex(start + dxMin, minY, minZ).color(colorX.r, colorX.g, colorX.b, colorX.a);
            buffer.vertex(end   + dxMax, minY, minZ).color(colorX.r, colorX.g, colorX.b, colorX.a);
        }

        start = (pos1.getX() == xMin && pos1.getY() == yMax && pos1.getZ() == zMin) || (pos2.getX() == xMin && pos2.getY() == yMax && pos2.getZ() == zMin) ? xMin + 1 : xMin;
        end   = (pos1.getX() == xMax && pos1.getY() == yMax && pos1.getZ() == zMin) || (pos2.getX() == xMax && pos2.getY() == yMax && pos2.getZ() == zMin) ? xMax : xMax + 1;

        if (end > start)
        {
            buffer.vertex(start + dxMin, maxY + 1, minZ).color(colorX.r, colorX.g, colorX.b, colorX.a);
            buffer.vertex(end   + dxMax, maxY + 1, minZ).color(colorX.r, colorX.g, colorX.b, colorX.a);
        }

        start = (pos1.getX() == xMin && pos1.getY() == yMin && pos1.getZ() == zMax) || (pos2.getX() == xMin && pos2.getY() == yMin && pos2.getZ() == zMax) ? xMin + 1 : xMin;
        end   = (pos1.getX() == xMax && pos1.getY() == yMin && pos1.getZ() == zMax) || (pos2.getX() == xMax && pos2.getY() == yMin && pos2.getZ() == zMax) ? xMax : xMax + 1;

        if (end > start)
        {
            buffer.vertex(start + dxMin, minY, maxZ + 1).color(colorX.r, colorX.g, colorX.b, colorX.a);
            buffer.vertex(end   + dxMax, minY, maxZ + 1).color(colorX.r, colorX.g, colorX.b, colorX.a);
        }

        start = (pos1.getX() == xMin && pos1.getY() == yMax && pos1.getZ() == zMax) || (pos2.getX() == xMin && pos2.getY() == yMax && pos2.getZ() == zMax) ? xMin + 1 : xMin;
        end   = (pos1.getX() == xMax && pos1.getY() == yMax && pos1.getZ() == zMax) || (pos2.getX() == xMax && pos2.getY() == yMax && pos2.getZ() == zMax) ? xMax : xMax + 1;

        if (end > start)
        {
            buffer.vertex(start + dxMin, maxY + 1, maxZ + 1).color(colorX.r, colorX.g, colorX.b, colorX.a);
            buffer.vertex(end   + dxMax, maxY + 1, maxZ + 1).color(colorX.r, colorX.g, colorX.b, colorX.a);
        }

        // Edges along the Y-axis
        start = (pos1.getX() == xMin && pos1.getY() == yMin && pos1.getZ() == zMin) || (pos2.getX() == xMin && pos2.getY() == yMin && pos2.getZ() == zMin) ? yMin + 1 : yMin;
        end   = (pos1.getX() == xMin && pos1.getY() == yMax && pos1.getZ() == zMin) || (pos2.getX() == xMin && pos2.getY() == yMax && pos2.getZ() == zMin) ? yMax : yMax + 1;

        if (end > start)
        {
            buffer.vertex(minX, start + dyMin, minZ).color(colorY.r, colorY.g, colorY.b, colorY.a);
            buffer.vertex(minX, end   + dyMax, minZ).color(colorY.r, colorY.g, colorY.b, colorY.a);
        }

        start = (pos1.getX() == xMax && pos1.getY() == yMin && pos1.getZ() == zMin) || (pos2.getX() == xMax && pos2.getY() == yMin && pos2.getZ() == zMin) ? yMin + 1 : yMin;
        end   = (pos1.getX() == xMax && pos1.getY() == yMax && pos1.getZ() == zMin) || (pos2.getX() == xMax && pos2.getY() == yMax && pos2.getZ() == zMin) ? yMax : yMax + 1;

        if (end > start)
        {
            buffer.vertex(maxX + 1, start + dyMin, minZ).color(colorY.r, colorY.g, colorY.b, colorY.a);
            buffer.vertex(maxX + 1, end   + dyMax, minZ).color(colorY.r, colorY.g, colorY.b, colorY.a);
        }

        start = (pos1.getX() == xMin && pos1.getY() == yMin && pos1.getZ() == zMax) || (pos2.getX() == xMin && pos2.getY() == yMin && pos2.getZ() == zMax) ? yMin + 1 : yMin;
        end   = (pos1.getX() == xMin && pos1.getY() == yMax && pos1.getZ() == zMax) || (pos2.getX() == xMin && pos2.getY() == yMax && pos2.getZ() == zMax) ? yMax : yMax + 1;

        if (end > start)
        {
            buffer.vertex(minX, start + dyMin, maxZ + 1).color(colorY.r, colorY.g, colorY.b, colorY.a);
            buffer.vertex(minX, end   + dyMax, maxZ + 1).color(colorY.r, colorY.g, colorY.b, colorY.a);
        }

        start = (pos1.getX() == xMax && pos1.getY() == yMin && pos1.getZ() == zMax) || (pos2.getX() == xMax && pos2.getY() == yMin && pos2.getZ() == zMax) ? yMin + 1 : yMin;
        end   = (pos1.getX() == xMax && pos1.getY() == yMax && pos1.getZ() == zMax) || (pos2.getX() == xMax && pos2.getY() == yMax && pos2.getZ() == zMax) ? yMax : yMax + 1;

        if (end > start)
        {
            buffer.vertex(maxX + 1, start + dyMin, maxZ + 1).color(colorY.r, colorY.g, colorY.b, colorY.a);
            buffer.vertex(maxX + 1, end   + dyMax, maxZ + 1).color(colorY.r, colorY.g, colorY.b, colorY.a);
        }

        // Edges along the Z-axis
        start = (pos1.getX() == xMin && pos1.getY() == yMin && pos1.getZ() == zMin) || (pos2.getX() == xMin && pos2.getY() == yMin && pos2.getZ() == zMin) ? zMin + 1 : zMin;
        end   = (pos1.getX() == xMin && pos1.getY() == yMin && pos1.getZ() == zMax) || (pos2.getX() == xMin && pos2.getY() == yMin && pos2.getZ() == zMax) ? zMax : zMax + 1;

        if (end > start)
        {
            buffer.vertex(minX, minY, start + dzMin).color(colorZ.r, colorZ.g, colorZ.b, colorZ.a);
            buffer.vertex(minX, minY, end   + dzMax).color(colorZ.r, colorZ.g, colorZ.b, colorZ.a);
        }

        start = (pos1.getX() == xMax && pos1.getY() == yMin && pos1.getZ() == zMin) || (pos2.getX() == xMax && pos2.getY() == yMin && pos2.getZ() == zMin) ? zMin + 1 : zMin;
        end   = (pos1.getX() == xMax && pos1.getY() == yMin && pos1.getZ() == zMax) || (pos2.getX() == xMax && pos2.getY() == yMin && pos2.getZ() == zMax) ? zMax : zMax + 1;

        if (end > start)
        {
            buffer.vertex(maxX + 1, minY, start + dzMin).color(colorZ.r, colorZ.g, colorZ.b, colorZ.a);
            buffer.vertex(maxX + 1, minY, end   + dzMax).color(colorZ.r, colorZ.g, colorZ.b, colorZ.a);
        }

        start = (pos1.getX() == xMin && pos1.getY() == yMax && pos1.getZ() == zMin) || (pos2.getX() == xMin && pos2.getY() == yMax && pos2.getZ() == zMin) ? zMin + 1 : zMin;
        end   = (pos1.getX() == xMin && pos1.getY() == yMax && pos1.getZ() == zMax) || (pos2.getX() == xMin && pos2.getY() == yMax && pos2.getZ() == zMax) ? zMax : zMax + 1;

        if (end > start)
        {
            buffer.vertex(minX, maxY + 1, start + dzMin).color(colorZ.r, colorZ.g, colorZ.b, colorZ.a);
            buffer.vertex(minX, maxY + 1, end   + dzMax).color(colorZ.r, colorZ.g, colorZ.b, colorZ.a);
        }

        start = (pos1.getX() == xMax && pos1.getY() == yMax && pos1.getZ() == zMin) || (pos2.getX() == xMax && pos2.getY() == yMax && pos2.getZ() == zMin) ? zMin + 1 : zMin;
        end   = (pos1.getX() == xMax && pos1.getY() == yMax && pos1.getZ() == zMax) || (pos2.getX() == xMax && pos2.getY() == yMax && pos2.getZ() == zMax) ? zMax : zMax + 1;

        if (end > start)
        {
            buffer.vertex(maxX + 1, maxY + 1, start + dzMin).color(colorZ.r, colorZ.g, colorZ.b, colorZ.a);
            buffer.vertex(maxX + 1, maxY + 1, end   + dzMax).color(colorZ.r, colorZ.g, colorZ.b, colorZ.a);
        }

        try
        {
            meshData = buffer.end();
            BufferRenderer.drawWithGlobalProgram(meshData);
            meshData.close();
        }
        catch (Exception ignored) { }
    }
}
