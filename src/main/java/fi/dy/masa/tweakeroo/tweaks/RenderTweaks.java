package fi.dy.masa.tweakeroo.tweaks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;
import fi.dy.masa.malilib.render.RenderUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import com.mojang.blaze3d.systems.RenderSystem;
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
    private static final ConcurrentHashMap<Long, ListMapEntry> SELECTIVE_BLACKLIST = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, ListMapEntry> SELECTIVE_WHITELIST = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, ListMapEntry> CACHED_LIST = new ConcurrentHashMap<>();

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

    private static RegistryAccess.Frozen dynamicRegistryManager;
    private static FakeWorld fakeWorld = null;

    public static void setDynamicRegistryManager(@Nullable RegistryAccess.Frozen immutable)
    {
        if (immutable == null)
        {
            return;
        }

        dynamicRegistryManager = immutable;
    }

    public static RegistryAccess.Frozen getDynamicRegistryManager()
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
        Minecraft mc = Minecraft.getInstance();
        if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue())
        {
            if (mc.options.keyAttack.isDown())
            {
                select(false);
            }

            if (mc.options.keyUse.isDown())
            {
                select(true);
            }
        }

    }

    public static void render(Matrix4f posMatrix, Matrix4f projMatrix, ProfilerFiller profiler)
    {
        Minecraft mc = Minecraft.getInstance();
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

            if (FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDER_OUTLINE.getBooleanValue())
            {
                renderLists(posMatrix, projMatrix, profiler);
            }
            if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue())
            {
                if (posLookingAt != null)
                {
                    RenderUtils.renderBlockOutline(posLookingAt, expand, lineWidthBlockBox, colorLooking, false);
                }

                renderSelection(posMatrix, projMatrix, profiler, AREA_SELECTION);
            }

            globalStack.popMatrix();
            profiler.pop();
        }
    }

    private static void renderLists(Matrix4f posMatrix, Matrix4f projMatrix, ProfilerFiller profiler)
    {
        float expand = 0.001f;
        float lineWidthBlockBox = 2f;
        Minecraft mc = Minecraft.getInstance();

        profiler.push("lists");
        for (ListMapEntry entry : SELECTIVE_BLACKLIST.values())
        {
            RenderUtils.renderBlockOutline(entry.currentPosition, expand, lineWidthBlockBox, colorBlacklist, false);
        }
        for (ListMapEntry entry : SELECTIVE_WHITELIST.values())
        {
            RenderUtils.renderBlockOutline(entry.currentPosition, expand, lineWidthBlockBox, colorWhitelist, false);
        }
        profiler.pop();
    }

    public static void updateLookingAt()
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.hitResult != null && mc.hitResult instanceof BlockHitResult)
        {
            posLookingAt = ((BlockHitResult) mc.hitResult).getBlockPos();

            // use offset
            if (Hotkeys.AREA_SELECTION_OFFSET.getKeybind().isKeybindHeld())
            {
                posLookingAt = posLookingAt.relative(((BlockHitResult) mc.hitResult).getDirection());
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
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null)
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

        Iterator<BlockPos> iterator = BlockPos.betweenClosed(AREA_SELECTION.pos1, AREA_SELECTION.pos2).iterator();
        int count = 0;
        ConcurrentHashMap<Long, ListMapEntry> list = (type == UsageRestriction.ListType.WHITELIST) ? SELECTIVE_WHITELIST
                                                                                                   : SELECTIVE_BLACKLIST;

        while (iterator.hasNext())
        {
            BlockPos pos = iterator.next().immutable();

            if (Configs.Generic.AREA_SELECTION_USE_ALL.getBooleanValue() || !mc.level.getBlockState(pos).isAir())
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
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null)
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

        Iterator<BlockPos> iterator = BlockPos.betweenClosed(AREA_SELECTION.pos1, AREA_SELECTION.pos2).iterator();
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
    public static void renderSelection(Matrix4f posMatrix, Matrix4f projMatrix, ProfilerFiller profiler, Selection selection)
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

        Minecraft mc = Minecraft.getInstance();

        profiler.push("selection");

        if (pos1 != null && pos2 != null)
        {
            if (pos1.equals(pos2) == false)
            {
                RenderUtils.renderAreaOutlineNoCorners(pos1, pos2, lineWidthArea, colorX, colorY, colorZ);
                RenderUtils.renderAreaSides(pos1, pos2, sideColor, posMatrix, false);
                RenderUtils.renderBlockOutline(pos1, expand, lineWidthBlockBox, colorPos1, false);
                RenderUtils.renderBlockOutline(pos2, expand, lineWidthBlockBox, colorPos2, false);
            }
            else
            {
                RenderUtils.renderBlockOutlineOverlapping(pos1, expand, lineWidthBlockBox, colorPos1, colorPos2,
                                                          colorOverlapping, posMatrix, false);
            }
        }
        else
        {
            if (pos1 != null)
            {
                RenderUtils.renderBlockOutline(pos1, expand, lineWidthBlockBox, colorPos1, false);
            }

            if (pos2 != null)
            {
                RenderUtils.renderBlockOutline(pos2, expand, lineWidthBlockBox, colorPos2, false);
            }
        }

        profiler.pop();
    }

    public static void onPistonEvent(BlockState state, Level world, BlockPos pos, int type, int data)
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

        Direction pushDirection = Direction.from3DDataValue(data & 7);

        PistonStructureResolver pistonHandler = new PistonStructureResolver(world, pos, pushDirection, type == 0);

        BlockState state2 = null;
        BlockEntity entity = null;
        BlockEntity entity2 = null;

        if (type != 0 && !((IMixinPistonBlock) state.getBlock()).getSticky())
        {
            return; // non sticky pistons do nothing
        }

        if (type != 0)
        {

            state2 = world.getBlockState(pos.relative(pushDirection)); // piston head
            entity = world.getBlockEntity(pos);
            entity2 = world.getBlockEntity(pos.relative(pushDirection));
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
            world.setBlock(pos.relative(pushDirection), Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
        }
        boolean moveSuccess = pistonHandler.resolve();

        if (type != 0)
        {
            world.setBlock(pos, state, Block.UPDATE_KNOWN_SHAPE);
            world.setBlock(pos.relative(pushDirection), state2, Block.UPDATE_KNOWN_SHAPE);
            if (entity != null)
            {
                world.setBlockEntity(entity);
            }
            if (entity2 != null)
            {
                world.setBlockEntity(entity2);
            }
        }

        boolean attatchedWhitelist = SELECTIVE_WHITELIST
                .containsKey(pos.relative(pushDirection, (type == 0) ? 1 : 2).asLong());
        boolean attatchedBlacklist = SELECTIVE_BLACKLIST
                .containsKey(pos.relative(pushDirection, (type == 0) ? 1 : 2).asLong());

        boolean whitelisted = SELECTIVE_WHITELIST.containsKey(pos.asLong());
        boolean blacklisted = SELECTIVE_BLACKLIST.containsKey(pos.asLong());

        if (type == 0)
        { // extending
            if (whitelisted)
            {
                if (attatchedWhitelist)
                {
                    SELECTIVE_WHITELIST.get(pos.relative(pushDirection).asLong()).preserve = true;
                }
                else
                {
                    SELECTIVE_WHITELIST.put(pos.relative(pushDirection, 1).asLong(),
                                            new ListMapEntry(pos.relative(pushDirection, 1)));
                }
            }
            if (blacklisted)
            {
                if (attatchedBlacklist)
                {
                    SELECTIVE_BLACKLIST.get(pos.relative(pushDirection).asLong()).preserve = true;
                }
                else
                {
                    SELECTIVE_BLACKLIST.put(pos.relative(pushDirection, 1).asLong(),
                                            new ListMapEntry(pos.relative(pushDirection, 1)));
                }
            }
        }
        if (moveSuccess)
        {
            // List<BlockPos> brokenBlocks = pistonHandler.getBrokenBlocks();
            List<BlockPos> movedBlocks = pistonHandler.getToPush();

            ArrayList<ListMapEntry> toMoveWhitelist = new ArrayList<>();
            ArrayList<ListMapEntry> toMoveBlacklist = new ArrayList<>();

            ArrayList<ListMapEntry> toAddWhitelist = new ArrayList<>();
            ArrayList<ListMapEntry> toAddBlacklist = new ArrayList<>();

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
                p.currentPosition = p.currentPosition.relative(pushDirection, (type == 0) ? 1 : -1);
                if (SELECTIVE_WHITELIST.containsKey(p.currentPosition.asLong()))
                {
                    p.preserve = true;
                }
                SELECTIVE_WHITELIST.put(p.currentPosition.asLong(), p);
            }

            for (ListMapEntry p : toMoveBlacklist)
            {
                p.currentPosition = p.currentPosition.relative(pushDirection, (type == 0) ? 1 : -1);
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

	    return switch ((UsageRestriction.ListType) Configs.Lists.SELECTIVE_BLOCKS_LIST_TYPE.getOptionListValue())
	    {
		    case NONE -> true;
		    case WHITELIST -> SELECTIVE_WHITELIST.containsKey(key);
		    case BLACKLIST -> !SELECTIVE_BLACKLIST.containsKey(key);
	    };
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
        {
            return;
        }
        BlockState state = mc.level.getBlockState(pos);

        if (RenderTweaks.isPositionValidForRendering(pos))
        {
            if (state.isAir())
            {
                BlockState originalState = fakeWorld.getBlockState(pos);

                if (!originalState.isAir())
                {
                    BlockEntity be = fakeWorld.getBlockEntity(pos);
                    fakeWorld.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    mc.level.setBlock(pos, originalState,
                                           Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE | PASSTHROUGH);
                    if (be != null)
                    {
                        mc.level.setBlockEntity(be);
                    }
                }
            }
        }
        else
        {
            if (!state.isAir())
            {
                BlockEntity be = mc.level.getBlockEntity(pos);
                mc.level.setBlock(pos, Blocks.AIR.defaultBlockState(),
                                       Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE | PASSTHROUGH);
                setFakeBlockState(mc.level, pos, state, be);
            }
        }
    }

    public static void reloadSelective()
    {
        Minecraft.getInstance().execute(RenderTweaks::reloadSelectiveInternal);
    }

    public static void reloadSelectiveInternal()
    {
        Minecraft mc = Minecraft.getInstance();
        UsageRestriction.ListType listtype = (UsageRestriction.ListType) Configs.Lists.SELECTIVE_BLOCKS_LIST_TYPE.getOptionListValue();
        boolean toggle = FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDERING.getBooleanValue();
        if (mc.level == null)
        {
            CACHED_LIST.clear();
            if (listtype != UsageRestriction.ListType.NONE)
            {
                ConcurrentHashMap<Long, ListMapEntry> list = (listtype == UsageRestriction.ListType.WHITELIST) ? SELECTIVE_WHITELIST
                                                                                                               : SELECTIVE_BLACKLIST;
	            for (ListMapEntry entry : list.values())
	            {
		            CACHED_LIST.put(entry.currentPosition.asLong(), entry);
	            }
            }

            previousSelectiveToggle = toggle;
            previousType = listtype;
            return;
        }
        if (listtype != previousType || toggle != previousSelectiveToggle)
        {
            ChunkPos center = fakeWorld.getChunkSource().getChunkMapCenter();
            int radius = fakeWorld.getChunkSource().getRadius();

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for (int cx = center.x - radius; cx <= center.x + radius; cx++)
            {
                for (int cz = center.z - radius; cz <= center.z + radius; cz++)
                {

                    LevelChunk chunk = (LevelChunk) mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL,
                                                                                        false);
                    FakeChunk fakeChunk = fakeWorld.getChunkSource().getChunkIfExists(cx, cz);
                    if (chunk != null && fakeChunk != null)
                    {
                        ChunkPos cpos = chunk.getPos();
                        LevelChunkSection[] sections = chunk.getSections();
                        LevelChunkSection[] fakeSections = fakeChunk.getSections();
                        for (int i = 0; i < sections.length; i++)
                        {
                            LevelChunkSection section = sections[i];
                            if (!section.hasOnlyAir() || !fakeSections[i].hasOnlyAir())
                            {
                                for (int x = 0; x < 16; x++)
                                {
                                    for (int y = 0; y < 16; y++)
                                    {
                                        for (int z = 0; z < 16; z++)
                                        {
                                            pos.set(x + cpos.getMinBlockX(), y + fakeWorld.getSectionYFromSectionIndex(i),
                                                    z + cpos.getMinBlockZ());
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
	            for (ListMapEntry entry : list.values())
	            {
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

        Minecraft mc = Minecraft.getInstance();
        boolean found = false;
        if (mc != null && mc.level != null && mc.level.getLightEngine() != null)
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
        ArrayList<String> entries = new ArrayList<>();

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

    public static boolean onOpenScreen(Component name, MenuType<?> screenHandlerType, int syncId)
    {
        LAST_CHECK = System.currentTimeMillis();
        return true;
    }

    public static void loadFakeChunk(int x, int z)
    {
        fakeWorld.getChunkSource().loadChunk(x, z);
    }

    public static void setFakeBlockState(Level realWorld, BlockPos pos, BlockState state, BlockEntity be)
    {
        fakeWorld.setBlock(pos, state, 0);
        if (be != null)
        {
            fakeWorld.setBlockEntity(be);
            be.setLevel(realWorld);
        }
    }

    public static void unloadFakeChunk(int x, int z)
    {
        fakeWorld.getChunkSource().unloadChunk(x, z);
    }
}
