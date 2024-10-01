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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.Camera;
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
import net.minecraft.util.math.MathHelper;

import fi.dy.masa.malilib.config.HudAlignment;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.util.BlockUtils;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.util.MiscUtils;
import fi.dy.masa.tweakeroo.util.SnapAimMode;

public class RenderUtils
{
    private static long lastRotationChangeTime;

    public static void renderHotbarSwapOverlay(MinecraftClient mc, DrawContext drawContext)
    {
        if (mc.player == null)
        {
            return;
        }
        PlayerEntity player = mc.player;

        if (mc.currentScreen == null)
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

    public static void renderInventoryOverlay(InventoryOverlay.Context context, DrawContext drawContext)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        /*
        World world = WorldUtils.getBestWorld(mc);
        Entity cameraEntity = EntityUtils.getCameraEntity();

        if (mc.player == null || world == null || cameraEntity == null)
        {
            return;
        }

        if (cameraEntity == mc.player && world instanceof ServerWorld)
        {
            // We need to get the player from the server world (if available, ie. in single player),
            // so that the player itself won't be included in the ray trace
            Entity serverPlayer = world.getPlayerByUuid(mc.player.getUuid());

            if (serverPlayer != null)
            {
                cameraEntity = serverPlayer;
            }
        }

        HitResult trace = RayTraceUtils.getRayTraceFromEntity(world, cameraEntity, false);

        ShulkerBoxBlock shulkerBoxBlock = null;
        CrafterBlock crafterBlock = null;
        LivingEntity entityLivingBase = null;
        InventoryOverlay.Context context = null;
        NbtCompound nbt = new NbtCompound();
        BlockEntity be = null;
        Inventory inv = null;
        BlockPos pos = null;

        if (trace.getType() == HitResult.Type.BLOCK)
        {
            pos = ((BlockHitResult) trace).getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block blockTmp = state.getBlock();
            Pair<BlockEntity, NbtCompound> blockPair = null;

            if (blockTmp instanceof ShulkerBoxBlock)
            {
                shulkerBoxBlock = (ShulkerBoxBlock) blockTmp;
            }
            else if (blockTmp instanceof CrafterBlock)
            {
                crafterBlock = (CrafterBlock) blockTmp;
            }

            inv = InventoryUtils.getInventory(world, pos);

            if (blockTmp instanceof BlockEntityProvider)
            {
                if (world instanceof ServerWorld)
                {
                    be = world.getWorldChunk(pos).getBlockEntity(pos);

                    if (be != null)
                    {
                        nbt = be.createNbtWithIdentifyingData(world.getRegistryManager());
                    }
                }
                else if (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue())
                {
                    blockPair = ServerDataSyncer.getInstance().requestBlockEntity(world, pos);
                    inv = ServerDataSyncer.getInstance().getBlockInventory(world, pos, true);
                }

                if (!nbt.isEmpty())
                {
                    Inventory inv2 = InventoryUtils.getNbtInventory(nbt, inv != null ? inv.size() : -1, world.getRegistryManager());

                    if (inv == null)
                    {
                        inv = inv2;
                    }

                    context = new InventoryOverlay.Context(InventoryOverlay.getBestInventoryType(inv, nbt), inv, be, null, nbt);
                }
                else if (blockPair != null && !blockPair.getRight().isEmpty())
                {
                    be = blockPair.getLeft();

                    Inventory inv2 = InventoryUtils.getNbtInventory(blockPair.getRight(), inv != null ? inv.size() : -1, world.getRegistryManager());

                    if (inv == null)
                    {
                        inv = inv2;
                    }

                    context = new InventoryOverlay.Context(InventoryOverlay.getBestInventoryType(inv, blockPair.getRight()), inv, be, null, blockPair.getRight());
                }
            }

            if (inv != null && !nbt.isEmpty())
            {
                context = new InventoryOverlay.Context(InventoryOverlay.getBestInventoryType(inv, nbt), inv, be, null, nbt);
            }
        }
        else if (trace.getType() == HitResult.Type.ENTITY)
        {
            Entity entity = ((EntityHitResult) trace).getEntity();

            if (world instanceof ServerWorld)
            {
                entity.saveSelfNbt(nbt);
            }
            else if (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue())
            {
                Pair<Entity, NbtCompound> entityPair = ServerDataSyncer.getInstance().requestEntity(entity.getId());

                if (entityPair != null && entityPair.getLeft() != null)
                {
                    entity = entityPair.getLeft();
                }
            }

            if (entity instanceof LivingEntity)
            {
                entityLivingBase = (LivingEntity) entity;
            }

            switch (entity)
            {
                case Inventory inventory ->
                        inv = inventory;
                case VillagerEntity villagerEntity ->
                        inv = villagerEntity.getInventory();
                case AbstractHorseEntity abstractHorseEntity ->
                        inv = ((IMixinAbstractHorseEntity) abstractHorseEntity).tweakeroo_getHorseInventory();
                default -> {}
            }

            if (!nbt.isEmpty())
            {
                Inventory inv2 = InventoryUtils.getNbtInventory(nbt, inv != null ? inv.size() : -1, world.getRegistryManager());

                if (inv == null)
                {
                    inv = inv2;
                }
            }

            context = new InventoryOverlay.Context(InventoryOverlay.getInventoryType(nbt), inv, null, entityLivingBase, nbt);
        }
         */

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
                    lockedSlots = BlockUtils.getDisabledSlotsFromNbt(context.nbt());
                }
            }

            //Tweakeroo.logger.warn("renderInventoryOverlay: type [{}] // Nbt Type [{}]", type.toString(), context.nbt() != null ? InventoryOverlay.getInventoryType(context.nbt()) : "INVALID");

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
                InventoryOverlay.renderInventoryStacks(type, horseInv, xInv + props.slotOffsetX, yInv + props.slotOffsetY, 1, 0, 2, mc, drawContext);
                xInv += 32 + 4;
            }

            if (totalSlots > 0)
            {
                InventoryOverlay.renderInventoryBackground(type, xInv, yInv, props.slotsPerRow, totalSlots, mc);
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
        double realYaw = MathHelper.floorMod(MiscUtils.getLastRealYaw(), 360.0D);
        double snappedYaw = MiscUtils.calculateSnappedAngle(realYaw, step);
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
        double realPitch = MathHelper.wrapDegrees(MiscUtils.getLastRealPitch());
        double snappedPitch;

        if (realPitch < 0)
        {
            snappedPitch = -MiscUtils.calculateSnappedAngle(-realPitch, step);
        }
        else
        {
            snappedPitch = MiscUtils.calculateSnappedAngle(realPitch, step);
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
}
