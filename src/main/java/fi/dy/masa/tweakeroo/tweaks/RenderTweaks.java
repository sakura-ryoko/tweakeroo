package fi.dy.masa.tweakeroo.tweaks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

import fi.dy.masa.malilib.render.RenderUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.data.Color4f;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.mixin.block.IMixinPistonBlock;
import fi.dy.masa.tweakeroo.world.FakeChunk;
import fi.dy.masa.tweakeroo.world.FakeWorld;

/**
 * Copied From Tweak Fork by Andrew54757
 */
public class RenderTweaks
{
    private static final ConcurrentHashMap<Long, ListMapEntry> SELECTIVE_BLACKLIST = new ConcurrentHashMap<Long, ListMapEntry>();
    private static final ConcurrentHashMap<Long, ListMapEntry> SELECTIVE_WHITELIST = new ConcurrentHashMap<Long, ListMapEntry>();
    private static final ConcurrentHashMap<Long, ListMapEntry> CACHED_LIST = new ConcurrentHashMap<Long, ListMapEntry>();

    public static final int PASSTHROUGH = 1024;

    private static final Color4f colorPos1 = new Color4f(1f, 0.0625f, 0.0625f);
    private static final Color4f colorPos2 = new Color4f(0.0625f, 0.0625f, 1f);
    private static final Color4f sideColor = Color4f.fromColor(0x30FFFFFF);
    private static final Color4f colorOverlapping = new Color4f(1f, 0.0625f, 1f);
    private static final Color4f colorX = new Color4f(1f, 0.25f, 0.25f);
    private static final Color4f colorY = new Color4f(0.25f, 1f, 0.25f);
    private static final Color4f colorZ = new Color4f(0.25f, 0.25f, 1f);
    private static final Color4f colorLooking = new Color4f(1.0f, 1.0f, 1.0f, 0.6f);
    private static final Color4f colorWhitelist = new Color4f(0.1f, 0.7f, 0.1f, 0.25f);
    private static final Color4f colorBlacklist = new Color4f(0.7f, 0.1f, 0.1f, 0.25f);
    private static Color4f colorSearch = new Color4f(0.9f, 0f, 0.7f, 0.25f);

    public static Selection AREA_SELECTION = new Selection();
    public static BlockPos posLookingAt = null;
    //public static Framebuffer endframebuffer = new SimpleFramebuffer(1, 1, true);

    public static long LAST_CHECK = 0;

    private static UsageRestriction.ListType previousType = (UsageRestriction.ListType) Configs.Lists.SELECTIVE_BLOCKS_LIST_TYPE.getOptionListValue();
    private static boolean previousSelectiveToggle = FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDERING.getBooleanValue();

    private static DynamicRegistryManager.Immutable dynamicRegistryManager;
    private static FakeWorld fakeWorld = null;

    public static void setDynamicRegistryManager(@Nullable DynamicRegistryManager.Immutable immutable)
    {
        if (immutable == null)
        {
            return;
        }

        dynamicRegistryManager = immutable;
    }

    public static DynamicRegistryManager.Immutable getDynamicRegistryManager()
    {
        return dynamicRegistryManager;
    }

    public static void resetWorld(int loadDistance)
    {
        fakeWorld = new FakeWorld(dynamicRegistryManager, loadDistance);
    }

    public static FakeWorld getFakeWorld()
    {
        return fakeWorld;
    }

    public static void onTick()
    {
        // Dumb rendundancy due to replaymod
        MinecraftClient mc = MinecraftClient.getInstance();
        if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue())
        {
            if (mc.options.attackKey.isPressed())
            {
                select(false);
            }

            if (mc.options.useKey.isPressed())
            {
                select(true);
            }
        }

    }

    public static void render(Matrix4f posMatrix, Matrix4f projMatrix, Profiler profiler)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        float expand = 0.001f;
        float lineWidthBlockBox = 2f;

        if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue()
            || FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDER_OUTLINE.getBooleanValue())
        {

            if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue())
            {
                updateLookingAt();
            }

            profiler.push(Reference.MOD_ID+"_render_tweaks");
            Matrix4fStack globalStack = RenderSystem.getModelViewStack();

            globalStack.pushMatrix();
            fi.dy.masa.malilib.render.RenderUtils.color(1f, 1f, 1f, 1f);
            fi.dy.masa.malilib.render.RenderUtils.blend(true);
            fi.dy.masa.malilib.render.RenderUtils.depthTest(false);
            fi.dy.masa.malilib.render.RenderUtils.polygonOffset(true);
            fi.dy.masa.malilib.render.RenderUtils.polygonOffset(-1.2f, -0.2f);

            if (FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDER_OUTLINE.getBooleanValue())
            {
                renderLists(posMatrix, projMatrix, profiler);
            }
            if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue())
            {
                if (posLookingAt != null)
                {
                    RenderUtils.renderBlockOutline(posLookingAt, expand, lineWidthBlockBox, colorLooking);
                }

                renderSelection(posMatrix, projMatrix, profiler, AREA_SELECTION);
            }

            fi.dy.masa.malilib.render.RenderUtils.polygonOffset(0f, 0f);
            fi.dy.masa.malilib.render.RenderUtils.polygonOffset(false);
            globalStack.popMatrix();
            fi.dy.masa.malilib.render.RenderUtils.depthMask(true);
            profiler.pop();
        }
    }

    private static void renderLists(Matrix4f posMatrix, Matrix4f projMatrix, Profiler profiler)
    {
        float expand = 0.001f;
        float lineWidthBlockBox = 2f;
        MinecraftClient mc = MinecraftClient.getInstance();

        profiler.push("lists");
        for (ListMapEntry entry : SELECTIVE_BLACKLIST.values())
        {
            RenderUtils.renderBlockOutline(entry.currentPosition, expand, lineWidthBlockBox, colorBlacklist);
        }
        for (ListMapEntry entry : SELECTIVE_WHITELIST.values())
        {
            RenderUtils.renderBlockOutline(entry.currentPosition, expand, lineWidthBlockBox, colorWhitelist);
        }
        profiler.pop();
    }

    public static void updateLookingAt()
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.crosshairTarget != null && mc.crosshairTarget instanceof BlockHitResult)
        {
            posLookingAt = ((BlockHitResult) mc.crosshairTarget).getBlockPos();

            // use offset
            if (Hotkeys.AREA_SELECTION_OFFSET.getKeybind().isKeybindHeld())
            {
                posLookingAt = posLookingAt.offset(((BlockHitResult) mc.crosshairTarget).getSide());
            }
        }
        else
        {
            posLookingAt = null;
        }
    }

    public static void select(boolean pos2)
    {
        if (posLookingAt == null)
        {
            return;
        }
        if (pos2)
        {
            AREA_SELECTION.pos2 = posLookingAt;
        }
        else
        {
            AREA_SELECTION.pos1 = posLookingAt;
        }
    }

    public static boolean isInSelection(BlockPos pos)
    {
        int minX = Math.min(AREA_SELECTION.pos1.getX(), AREA_SELECTION.pos2.getX());
        int minY = Math.min(AREA_SELECTION.pos1.getY(), AREA_SELECTION.pos2.getY());
        int minZ = Math.min(AREA_SELECTION.pos1.getZ(), AREA_SELECTION.pos2.getZ());
        int maxX = Math.max(AREA_SELECTION.pos1.getX(), AREA_SELECTION.pos2.getX());
        int maxY = Math.max(AREA_SELECTION.pos1.getY(), AREA_SELECTION.pos2.getY());
        int maxZ = Math.max(AREA_SELECTION.pos1.getZ(), AREA_SELECTION.pos2.getZ());

        return !(pos.getX() < minX || pos.getX() > maxX || pos.getY() < minY || pos.getY() > maxY || pos.getZ() < minZ
                || pos.getZ() > maxZ);
    }

    public static void addSelectionToList()
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null)
        {
            return;
        }
        if (AREA_SELECTION.pos1 == null || AREA_SELECTION.pos2 == null)
        {
            InfoUtils.printActionbarMessage("Please set an area first");
            return;
        }
        UsageRestriction.ListType type = (UsageRestriction.ListType) Configs.Lists.SELECTIVE_BLOCKS_LIST_TYPE.getOptionListValue();

        if (type == UsageRestriction.ListType.NONE)
        {
            InfoUtils.printActionbarMessage("No list selected");
            return;
        }

        Iterator<BlockPos> iterator = BlockPos.iterate(AREA_SELECTION.pos1, AREA_SELECTION.pos2).iterator();
        int count = 0;
        ConcurrentHashMap<Long, ListMapEntry> list = (type == UsageRestriction.ListType.WHITELIST) ? SELECTIVE_WHITELIST
                                                                                                   : SELECTIVE_BLACKLIST;

        while (iterator.hasNext())
        {
            BlockPos pos = iterator.next().toImmutable();

            if (Configs.Generic.AREA_SELECTION_USE_ALL.getBooleanValue() || !mc.world.getBlockState(pos).isAir())
            {
                if (!list.containsKey(pos.asLong()))
                {
                    list.put(pos.asLong(), new ListMapEntry(pos));
                    count++;
                }
            }
        }
        rebuildStrings();
        InfoUtils.printActionbarMessage("Added " + count + " blocks");
    }

    public static void removeSelectionFromList()
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null)
        {
            return;
        }
        if (AREA_SELECTION.pos1 == null || AREA_SELECTION.pos2 == null)
        {
            InfoUtils.printActionbarMessage("Please set an area first");
            return;
        }
        UsageRestriction.ListType type = (UsageRestriction.ListType) Configs.Lists.SELECTIVE_BLOCKS_LIST_TYPE.getOptionListValue();

        if (type == UsageRestriction.ListType.NONE)
        {
            InfoUtils.printActionbarMessage("No list selected");
            return;
        }

        Iterator<BlockPos> iterator = BlockPos.iterate(AREA_SELECTION.pos1, AREA_SELECTION.pos2).iterator();
        int count = 0;
        ConcurrentHashMap<Long, ListMapEntry> list = (type == UsageRestriction.ListType.WHITELIST) ? SELECTIVE_WHITELIST
                                                                                                   : SELECTIVE_BLACKLIST;

        while (iterator.hasNext())
        {
            BlockPos pos = iterator.next();
            if (list.containsKey(pos.asLong()))
            {
                list.remove(pos.asLong());
                count++;
            }

        }
        rebuildStrings();
        InfoUtils.printActionbarMessage("Removed " + count + " blocks");
    }

    // From litematica
    public static void renderSelection(Matrix4f posMatrix, Matrix4f projMatrix, Profiler profiler, Selection selection)
    {

        BlockPos pos1 = selection.pos1;
        BlockPos pos2 = selection.pos2;
        if (pos1 == null && pos2 == null)
        {
            return;
        }
        float expand = 0.001f;
        float lineWidthBlockBox = 2f;
        float lineWidthArea = 1.5f;

        MinecraftClient mc = MinecraftClient.getInstance();

        profiler.push("selection");

        if (pos1 != null && pos2 != null)
        {
            if (pos1.equals(pos2) == false)
            {
                RenderUtils.renderAreaOutlineNoCorners(pos1, pos2, lineWidthArea, colorX, colorY, colorZ);

                RenderUtils.renderAreaSides(pos1, pos2, sideColor, posMatrix);

                RenderUtils.renderBlockOutline(pos1, expand, lineWidthBlockBox, colorPos1);
                RenderUtils.renderBlockOutline(pos2, expand, lineWidthBlockBox, colorPos2);
            }
            else
            {
                RenderUtils.renderBlockOutlineOverlapping(pos1, expand, lineWidthBlockBox, colorPos1, colorPos2,
                                                          colorOverlapping, posMatrix);
            }
        }
        else
        {
            if (pos1 != null)
            {
                RenderUtils.renderBlockOutline(pos1, expand, lineWidthBlockBox, colorPos1);
            }

            if (pos2 != null)
            {
                RenderUtils.renderBlockOutline(pos2, expand, lineWidthBlockBox, colorPos2);
            }
        }

        profiler.pop();
    }

    public static void onPistonEvent(BlockState state, World world, BlockPos pos, int type, int data)
    {
        if (!Configs.Generic.SELECTIVE_BLOCKS_TRACK_PISTONS.getBooleanValue()
            || (!FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDERING.getBooleanValue()
            && !FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDER_OUTLINE.getBooleanValue())
            || (SELECTIVE_WHITELIST.size() == 0 && SELECTIVE_BLACKLIST.size() == 0))
        {
            return;
        }

        if (type == 2)
        {
            return;
        }

        Direction pushDirection = Direction.byIndex(data & 7);

        PistonHandler pistonHandler = new PistonHandler(world, pos, pushDirection, type == 0);

        BlockState state2 = null;
        BlockEntity entity = null;
        BlockEntity entity2 = null;

        if (type != 0 && !((IMixinPistonBlock) state.getBlock()).getSticky())
        {
            return; // non sticky pistons do nothing
        }

        if (type != 0)
        {

            state2 = world.getBlockState(pos.offset(pushDirection)); // piston head
            entity = world.getBlockEntity(pos);
            entity2 = world.getBlockEntity(pos.offset(pushDirection));
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
            world.setBlockState(pos.offset(pushDirection), Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
        }
        boolean moveSuccess = pistonHandler.calculatePush();

        if (type != 0)
        {
            world.setBlockState(pos, state, Block.FORCE_STATE);
            world.setBlockState(pos.offset(pushDirection), state2, Block.FORCE_STATE);
            if (entity != null)
            {
                world.addBlockEntity(entity);
            }
            if (entity2 != null)
            {
                world.addBlockEntity(entity2);
            }
        }

        boolean attatchedWhitelist = SELECTIVE_WHITELIST
                .containsKey(pos.offset(pushDirection, (type == 0) ? 1 : 2).asLong());
        boolean attatchedBlacklist = SELECTIVE_BLACKLIST
                .containsKey(pos.offset(pushDirection, (type == 0) ? 1 : 2).asLong());

        boolean whitelisted = SELECTIVE_WHITELIST.containsKey(pos.asLong());
        boolean blacklisted = SELECTIVE_BLACKLIST.containsKey(pos.asLong());

        if (type == 0)
        { // extending
            if (whitelisted)
            {
                if (attatchedWhitelist)
                {
                    SELECTIVE_WHITELIST.get(pos.offset(pushDirection).asLong()).preserve = true;
                }
                else
                {
                    SELECTIVE_WHITELIST.put(pos.offset(pushDirection, 1).asLong(),
                                            new ListMapEntry(pos.offset(pushDirection, 1)));
                }
            }
            if (blacklisted)
            {
                if (attatchedBlacklist)
                {
                    SELECTIVE_BLACKLIST.get(pos.offset(pushDirection).asLong()).preserve = true;
                }
                else
                {
                    SELECTIVE_BLACKLIST.put(pos.offset(pushDirection, 1).asLong(),
                                            new ListMapEntry(pos.offset(pushDirection, 1)));
                }
            }
        }
        if (moveSuccess)
        {
            // List<BlockPos> brokenBlocks = pistonHandler.getBrokenBlocks();
            List<BlockPos> movedBlocks = pistonHandler.getMovedBlocks();

            ArrayList<ListMapEntry> toMoveWhitelist = new ArrayList<ListMapEntry>();
            ArrayList<ListMapEntry> toMoveBlacklist = new ArrayList<ListMapEntry>();

            ArrayList<ListMapEntry> toAddWhitelist = new ArrayList<ListMapEntry>();
            ArrayList<ListMapEntry> toAddBlacklist = new ArrayList<ListMapEntry>();

            for (BlockPos p : movedBlocks)
            {
                long key = p.asLong();
                if (SELECTIVE_WHITELIST.containsKey(key))
                {
                    ListMapEntry entry = SELECTIVE_WHITELIST.get(key);
                    toMoveWhitelist.add(entry);

                    SELECTIVE_WHITELIST.remove(key);
                    if (entry.preserve)
                    {
                        entry.preserve = false;
                        toAddWhitelist.add(new ListMapEntry(p));
                    }
                }

                if (SELECTIVE_BLACKLIST.containsKey(key))
                {
                    ListMapEntry entry = SELECTIVE_BLACKLIST.get(key);
                    toMoveBlacklist.add(entry);

                    SELECTIVE_BLACKLIST.remove(key);
                    if (entry.preserve)
                    {
                        entry.preserve = false;
                        toAddBlacklist.add(new ListMapEntry(p));
                    }
                }
            }

            for (ListMapEntry p : toMoveWhitelist)
            {
                p.currentPosition = p.currentPosition.offset(pushDirection, (type == 0) ? 1 : -1);
                if (SELECTIVE_WHITELIST.containsKey(p.currentPosition.asLong()))
                {
                    p.preserve = true;
                }
                SELECTIVE_WHITELIST.put(p.currentPosition.asLong(), p);
            }

            for (ListMapEntry p : toMoveBlacklist)
            {
                p.currentPosition = p.currentPosition.offset(pushDirection, (type == 0) ? 1 : -1);
                if (SELECTIVE_BLACKLIST.containsKey(p.currentPosition.asLong()))
                {
                    p.preserve = true;
                }
                SELECTIVE_BLACKLIST.put(p.currentPosition.asLong(), p);
            }

            for (ListMapEntry p : toAddWhitelist)
            {
                SELECTIVE_WHITELIST.put(p.currentPosition.asLong(), p);
            }

            for (ListMapEntry p : toAddBlacklist)
            {
                SELECTIVE_BLACKLIST.put(p.currentPosition.asLong(), p);
            }
        }
        reloadSelective();
    }

    public static boolean isPositionValidForRendering(BlockPos pos)
    {
        return isPositionValidForRendering(pos.asLong());
    }

    public static boolean isPositionValidForRendering(long key)
    {
        if (!FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDERING.getBooleanValue())
        {
            return true;
        }

        switch ((UsageRestriction.ListType) Configs.Lists.SELECTIVE_BLOCKS_LIST_TYPE.getOptionListValue())
        {
            case NONE:
                return true;
            case WHITELIST:
                return SELECTIVE_WHITELIST.containsKey(key);
            case BLACKLIST:
                return !SELECTIVE_BLACKLIST.containsKey(key);
        }

        return false;
    }

    public static void rebuildLists()
    {
        SELECTIVE_BLACKLIST.clear();
        SELECTIVE_WHITELIST.clear();
        putMapFromString(SELECTIVE_BLACKLIST, Configs.Lists.SELECTIVE_BLOCKS_BLACKLIST.getStringValue());
        putMapFromString(SELECTIVE_WHITELIST, Configs.Lists.SELECTIVE_BLOCKS_WHITELIST.getStringValue());

        reloadSelective();
    }

    public static void updateSelectiveAtPos(BlockPos pos)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null)
        {
            return;
        }
        BlockState state = mc.world.getBlockState(pos);

        if (RenderTweaks.isPositionValidForRendering(pos))
        {
            if (state.isAir())
            {
                BlockState originalState = fakeWorld.getBlockState(pos);

                if (!originalState.isAir())
                {
                    BlockEntity be = fakeWorld.getBlockEntity(pos);
                    fakeWorld.setBlockState(pos, Blocks.AIR.getDefaultState());
                    mc.world.setBlockState(pos, originalState,
                                           Block.NOTIFY_ALL | Block.FORCE_STATE | PASSTHROUGH);
                    if (be != null)
                    {
                        mc.world.addBlockEntity(be);
                    }
                }
            }
        }
        else
        {
            if (!state.isAir())
            {
                BlockEntity be = mc.world.getBlockEntity(pos);
                mc.world.setBlockState(pos, Blocks.AIR.getDefaultState(),
                                       Block.NOTIFY_ALL | Block.FORCE_STATE | PASSTHROUGH);
                setFakeBlockState(mc.world, pos, state, be);
            }
        }
    }

    public static void reloadSelective()
    {
        MinecraftClient.getInstance().execute(RenderTweaks::reloadSelectiveInternal);
    }

    public static void reloadSelectiveInternal()
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        UsageRestriction.ListType listtype = (UsageRestriction.ListType) Configs.Lists.SELECTIVE_BLOCKS_LIST_TYPE.getOptionListValue();
        boolean toggle = FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDERING.getBooleanValue();
        if (mc.world == null)
        {
            CACHED_LIST.clear();
            if (listtype != UsageRestriction.ListType.NONE)
            {
                ConcurrentHashMap<Long, ListMapEntry> list = (listtype == UsageRestriction.ListType.WHITELIST) ? SELECTIVE_WHITELIST
                                                                                                               : SELECTIVE_BLACKLIST;
                Iterator<ListMapEntry> iterator = list.values().iterator();
                while (iterator.hasNext())
                {
                    ListMapEntry entry = iterator.next();
                    CACHED_LIST.put(entry.currentPosition.asLong(), entry);
                }
            }

            previousSelectiveToggle = toggle;
            previousType = listtype;
            return;
        }
        if (listtype != previousType || toggle != previousSelectiveToggle)
        {
            ChunkPos center = fakeWorld.getChunkManager().getChunkMapCenter();
            int radius = fakeWorld.getChunkManager().getRadius();

            BlockPos.Mutable pos = new BlockPos.Mutable();

            for (int cx = center.x - radius; cx <= center.x + radius; cx++)
            {
                for (int cz = center.z - radius; cz <= center.z + radius; cz++)
                {

                    WorldChunk chunk = (WorldChunk) mc.world.getChunkManager().getChunk(cx, cz, ChunkStatus.FULL,
                                                                                        false);
                    FakeChunk fakeChunk = fakeWorld.getChunkManager().getChunkIfExists(cx, cz);
                    if (chunk != null && fakeChunk != null)
                    {
                        ChunkPos cpos = chunk.getPos();
                        ChunkSection[] sections = chunk.getSectionArray();
                        ChunkSection[] fakeSections = fakeChunk.getSectionArray();
                        for (int i = 0; i < sections.length; i++)
                        {
                            ChunkSection section = sections[i];
                            if (!section.isEmpty() || !fakeSections[i].isEmpty())
                            {
                                for (int x = 0; x < 16; x++)
                                {
                                    for (int y = 0; y < 16; y++)
                                    {
                                        for (int z = 0; z < 16; z++)
                                        {
                                            pos.set(x + cpos.getStartX(), y + fakeWorld.sectionIndexToCoord(i),
                                                    z + cpos.getStartZ());
                                            updateSelectiveAtPos(pos);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            CACHED_LIST.clear();
            if (listtype != UsageRestriction.ListType.NONE)
            {
                ConcurrentHashMap<Long, ListMapEntry> list = (listtype == UsageRestriction.ListType.WHITELIST) ? SELECTIVE_WHITELIST
                                                                                                               : SELECTIVE_BLACKLIST;
                Iterator<ListMapEntry> iterator = list.values().iterator();
                while (iterator.hasNext())
                {
                    ListMapEntry entry = iterator.next();
                    CACHED_LIST.put(entry.currentPosition.asLong(), entry);
                }
            }
        }
        else if (listtype != UsageRestriction.ListType.NONE)
        {
            ConcurrentHashMap<Long, ListMapEntry> list = (listtype == UsageRestriction.ListType.WHITELIST) ? SELECTIVE_WHITELIST
                                                                                                           : SELECTIVE_BLACKLIST;
            Iterator<ListMapEntry> iterator = CACHED_LIST.values().iterator();
            while (iterator.hasNext())
            {
                ListMapEntry entry = iterator.next();
                if (!list.containsKey(entry.currentPosition.asLong()))
                {
                    updateSelectiveAtPos(entry.currentPosition);
                    iterator.remove();
                }
            }

            iterator = list.values().iterator();
            while (iterator.hasNext())
            {
                ListMapEntry entry = iterator.next();
                if (!CACHED_LIST.containsKey(entry.currentPosition.asLong()))
                {
                    updateSelectiveAtPos(entry.currentPosition);
                    CACHED_LIST.put(entry.currentPosition.asLong(), entry);
                }
            }
        }

        previousSelectiveToggle = toggle;
        previousType = listtype;
    }

    public static void onLightUpdateEvent(int chunkX, int chunkZ, CallbackInfo ci)
    {
        if (true || !FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDERING.getBooleanValue())
        {
            return;
        }

        UsageRestriction.ListType listtype = (UsageRestriction.ListType) Configs.Lists.SELECTIVE_BLOCKS_LIST_TYPE.getOptionListValue();

        if (listtype == UsageRestriction.ListType.NONE)
        {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        boolean found = false;
        if (mc != null && mc.world != null && mc.world.getLightingProvider() != null)
        {

            ConcurrentHashMap<Long, ListMapEntry> list = (listtype == UsageRestriction.ListType.WHITELIST) ? SELECTIVE_WHITELIST
                                                                                                           : SELECTIVE_BLACKLIST;

            int minX = chunkX * 16 - 1;
            int minZ = chunkZ * 16 - 1;
            int maxX = chunkX * 16 + 16;
            int maxZ = chunkZ * 16 + 16;

            for (ListMapEntry entry : list.values())
            {

                int x = entry.currentPosition.getX();
                int z = entry.currentPosition.getZ();
                if (x >= minX && z >= minZ && x <= maxX && z <= maxZ)
                {
                    found = true;
                    break;
                }
            }
        }
        if (found)
        {
            ci.cancel();
        }
    }

    public static void rebuildStrings()
    {
        String whitelist = getStringFromMap(SELECTIVE_WHITELIST);
        String blacklist = getStringFromMap(SELECTIVE_BLACKLIST);

        Configs.Lists.SELECTIVE_BLOCKS_WHITELIST.setValueFromString(whitelist);
        Configs.Lists.SELECTIVE_BLOCKS_BLACKLIST.setValueFromString(blacklist);
    }

    public static void putMapFromString(ConcurrentHashMap<Long, ListMapEntry> map, String str)
    {

        String[] parts = str.split("\\|");

        for (String part : parts)
        {
            String[] nums = part.split(",");

            if (nums.length < 3)
            {
                continue;
            }

            try
            {
                int x = Integer.parseInt(nums[0]);
                int y = Integer.parseInt(nums[1]);
                int z = Integer.parseInt(nums[2]);
                // System.out.println(x + "," + y + "," + z);
                BlockPos pos = new BlockPos(x, y, z);
                map.put(pos.asLong(), new ListMapEntry(pos, true));
            }
            catch (NumberFormatException e)
            {

                Tweakeroo.LOGGER.warn("Error while parsing int: {}", e.toString());
            }
        }
    }

    public static String getStringFromMap(ConcurrentHashMap<Long, ListMapEntry> map)
    {

        Iterator<ListMapEntry> iterator = map.values().iterator();
        ArrayList<String> entries = new ArrayList<String>();

        while (iterator.hasNext())
        {
            ListMapEntry entry = iterator.next();
            entries.add(entry.originalPosition.getX() + "," + entry.originalPosition.getY() + ","
                                + entry.originalPosition.getZ());
        }
        return String.join("|", entries);
    }

    public static Color4f getColorSearch()
    {
        return colorSearch;
    }

    public static void setColorSearch(Color4f colorSearch)
    {
        RenderTweaks.colorSearch = colorSearch;
    }

    public static class ListMapEntry
    {
        public final BlockPos originalPosition;
        public BlockPos currentPosition;
        public boolean preserve = false;

        ListMapEntry(BlockPos pos)
        {
            originalPosition = pos;
            currentPosition = pos;
        }

        ListMapEntry(BlockPos pos, boolean preserve)
        {
            this(pos);
            this.preserve = preserve;
        }
    }

    public static class Selection
    {
        public BlockPos pos1 = null;
        public BlockPos pos2 = null;
    }

    public static boolean onOpenScreen(Text name, ScreenHandlerType<?> screenHandlerType, int syncId)
    {
        LAST_CHECK = System.currentTimeMillis();
        return true;
    }

    public static void loadFakeChunk(int x, int z)
    {
        fakeWorld.getChunkManager().loadChunk(x, z);
    }

    public static void setFakeBlockState(World realWorld, BlockPos pos, BlockState state, BlockEntity be)
    {
        fakeWorld.setBlockState(pos, state, 0);
        if (be != null)
        {
            fakeWorld.addBlockEntity(be);
            be.setWorld(realWorld);
        }
    }

    public static void unloadFakeChunk(int x, int z)
    {
        fakeWorld.getChunkManager().unloadChunk(x, z);
    }
}
