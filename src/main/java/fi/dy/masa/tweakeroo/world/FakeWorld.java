package fi.dy.masa.tweakeroo.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.map.MapState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickManager;

import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

/**
 * Copied From Tweak Fork by Andrew54757
 */
public class FakeWorld extends World
{
    private static final RegistryKey<World> REGISTRY_KEY = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(Reference.MOD_ID, "selective_world"));
    private static final ClientWorld.Properties LEVEL_INFO = new ClientWorld.Properties(Difficulty.PEACEFUL, false, true);
    private static final RegistryEntry<DimensionType> DIMENSION_TYPE = RenderTweaks.getDynamicRegistryManager().getOptionalEntry(DimensionTypes.OVERWORLD).orElseThrow();

    private final MinecraftClient mc;
    private final FakeChunkManager chunkManager;
    private final Supplier<Profiler> profiler;
    private DynamicRegistryManager registryManager;

    public FakeWorld(
            DynamicRegistryManager registryManager,
            MutableWorldProperties properties,
            RegistryEntry<DimensionType> dimension,
            Supplier<Profiler> profiler,
            int loadDistance
    )
    {
        //MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates
        super(properties, REGISTRY_KEY, registryManager, dimension, true, true, 0L, 0);
        this.mc = MinecraftClient.getInstance();
        this.registryManager = registryManager;
        this.chunkManager = new FakeChunkManager(this, loadDistance);
        this.profiler = profiler;
    }

    public FakeWorld(DynamicRegistryManager registryManager, int loadDistance)
    {
        this(registryManager, LEVEL_INFO, DIMENSION_TYPE, Profilers::get, loadDistance);
    }

    public Profiler getProfiler()
    {
        return this.profiler.get();
    }

    public FakeChunkManager getChunkProvider()
    {
        return this.chunkManager;
    }

    @Override
    public FakeChunkManager getChunkManager()
    {
        return this.chunkManager;
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void emitGameEvent(RegistryEntry<GameEvent> event, Vec3d emitterPos, Emitter emitter)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public WorldChunk getWorldChunk(BlockPos pos)
    {
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Override
    public FakeChunk getChunk(int chunkX, int chunkZ)
    {
        return this.chunkManager.getChunk(chunkX, chunkZ);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status, boolean required)
    {
        return this.getChunk(chunkX, chunkZ);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags)
    {
        if (pos.getY() < this.getBottomY() || pos.getY() >= this.getTopYInclusive())
        {
            return false;
        }
        else
        {
            return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4).setBlockState(pos, newState, false) != null;
        }
    }


    public List<FakeChunk> getChunksWithinBox(Box box)
    {
        final int minX = MathHelper.floor(box.minX / 16.0);
        final int minZ = MathHelper.floor(box.minZ / 16.0);
        final int maxX = MathHelper.floor(box.maxX / 16.0);
        final int maxZ = MathHelper.floor(box.maxZ / 16.0);

        List<FakeChunk> chunks = new ArrayList<>();

        for (int cx = minX; cx <= maxX; ++cx)
        {
            for (int cz = minZ; cz <= maxZ; ++cz)
            {
                FakeChunk chunk = this.chunkManager.getChunkIfExists(cx, cz);

                if (chunk != null)
                {
                    chunks.add(chunk);
                }
            }
        }

        return chunks;
    }

    @Override
    public int getBottomY()
    {
        return this.mc.world != null ? this.mc.world.getBottomY() : -64;
    }

    @Override
    public int getHeight()
    {
        return this.mc.world != null ? this.mc.world.getHeight() : 384;
    }

    // The following HeightLimitView overrides are to work around an incompatibility with Lithium 0.7.4+

    @Override
    public int getTopYInclusive()
    {
        return this.getBottomY() + this.getHeight();
    }

    @Override
    public int getBottomSectionCoord()
    {
        return this.getBottomY() >> 4;
    }

    @Override
    public int getTopSectionCoord()
    {
        return this.getTopYInclusive() >> 4;
    }

    @Override
    public int countVerticalSections()
    {
        return this.getTopSectionCoord() - this.getBottomSectionCoord();
    }

    @Override
    public boolean isOutOfHeightLimit(BlockPos pos)
    {
        return this.isOutOfHeightLimit(pos.getY());
    }

    @Override
    public boolean isOutOfHeightLimit(int y)
    {
        return (y < this.getBottomY()) || (y >= this.getTopYInclusive());
    }

    @Override
    public int getSectionIndex(int y)
    {
        return (y >> 4) - (this.getBottomY() >> 4);
    }

    @Override
    public int sectionCoordToIndex(int coord)
    {
        return coord - (this.getBottomY() >> 4);
    }

    @Override
    public int sectionIndexToCoord(int index)
    {
        return index + (this.getBottomY() >> 4);
    }

    @Override
    public String asString()
    {
        return "Chunks[FAKE] W: " + this.getChunkManager().getDebugString();
    }

    @Override
    public DynamicRegistryManager getRegistryManager()
    {
        if (this.registryManager == null)
        {
            this.registryManager = RenderTweaks.getDynamicRegistryManager();
        }

        return this.registryManager;
    }

    @Override
    public BrewingRecipeRegistry getBrewingRecipeRegistry()
    {
        return null;
    }

    @Override
    public FuelRegistry getFuelRegistry()
    {
        return null;
    }

    @Override
    public QueryableTickScheduler<Block> getBlockTickScheduler()
    {
        return null;
    }

    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler()
    {
        return null;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RegistryEntry<Biome> getGeneratorStoredBiome(int var1, int var2, int var3)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSeaLevel()
    {
        return 0;
    }

    @Override
    public FeatureSet getEnabledFeatures()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float getBrightness(Direction var1, boolean var2)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void updateListeners(BlockPos var1, BlockState var2, BlockState var3, int var4)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSound(@Nullable PlayerEntity source, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity source, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void createExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, ExplosionSourceType explosionSourceType, ParticleEffect smallParticle, ParticleEffect largeParticle, RegistryEntry<SoundEvent> soundEvent)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public Entity getEntityById(int var1)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TickManager getTickManager()
    {
        return null;
    }

    @Override
    public @Nullable MapState getMapState(MapIdComponent id)
    {
        return null;
    }

    @Override
    public void putMapState(MapIdComponent id, MapState state)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public MapIdComponent increaseAndGetMapId()
    {
        return null;
    }

    @Override
    public void setBlockBreakingInfo(int var1, BlockPos var2, int var3)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public Scoreboard getScoreboard()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected EntityLookup<Entity> getEntityLookup()
    {
        // TODO Auto-generated method stub
        return null;
    }
}