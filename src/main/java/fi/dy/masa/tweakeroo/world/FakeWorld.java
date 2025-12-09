package fi.dy.masa.tweakeroo.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;

import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

/**
 * Copied From Tweak Fork by Andrew54757
 */
public class FakeWorld extends Level
{
    private static final ResourceKey<Level> REGISTRY_KEY = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "selective_world"));
    private static final ClientLevel.ClientLevelData LEVEL_INFO = new ClientLevel.ClientLevelData(Difficulty.PEACEFUL, false, true);
    private static final Holder<DimensionType> DIMENSION_TYPE = RenderTweaks.getDynamicRegistryManager().getOrThrow(BuiltinDimensionTypes.OVERWORLD);

    private final Minecraft mc;
    private final FakeChunkManager chunkManager;
    private final Supplier<ProfilerFiller> profiler;
    private RegistryAccess registryManager;

    public FakeWorld(
            RegistryAccess registryManager,
            WritableLevelData properties,
            Holder<DimensionType> dimension,
            Supplier<ProfilerFiller> profiler,
            int loadDistance
    )
    {
        //MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates
        super(properties, REGISTRY_KEY, registryManager, dimension, true, true, 0L, 0);
        this.mc = Minecraft.getInstance();
        this.registryManager = registryManager;
        this.chunkManager = new FakeChunkManager(this, loadDistance);
        this.profiler = profiler;
    }

    public FakeWorld(RegistryAccess registryManager, int loadDistance)
    {
        this(registryManager, LEVEL_INFO, DIMENSION_TYPE, Profiler::get, loadDistance);
    }

    public ProfilerFiller getProfiler()
    {
        return this.profiler.get();
    }

    public FakeChunkManager getChunkProvider()
    {
        return this.chunkManager;
    }

    @Override
    public @Nonnull FakeChunkManager getChunkSource()
    {
        return this.chunkManager;
    }

    @Override
    public void levelEvent(@Nullable Entity source, int eventId, @Nonnull BlockPos pos, int data)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void gameEvent(@Nonnull Holder<GameEvent> event, @Nonnull Vec3 emitterPos, @Nonnull Context emitter)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public @Nonnull LevelChunk getChunkAt(BlockPos pos)
    {
        return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Override
    public @Nonnull FakeChunk getChunk(int chunkX, int chunkZ)
    {
        return this.chunkManager.getChunkForLighting(chunkX, chunkZ);
    }

    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, @Nonnull ChunkStatus status, boolean required)
    {
        return this.getChunk(chunkX, chunkZ);
    }

    @Override
    public boolean setBlock(BlockPos pos, @Nonnull BlockState newState, int flags)
    {
        if (pos.getY() < this.getMinY() || pos.getY() >= this.getMaxY())
        {
            return false;
        }
        else
        {
            return this.getChunk(pos.getX() >> 4, pos.getZ() >> 4).setBlockState(pos, newState, -1) != null;
        }
    }


    public List<FakeChunk> getChunksWithinBox(AABB box)
    {
        final int minX = Mth.floor(box.minX / 16.0);
        final int minZ = Mth.floor(box.minZ / 16.0);
        final int maxX = Mth.floor(box.maxX / 16.0);
        final int maxZ = Mth.floor(box.maxZ / 16.0);

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
    public int getMinY()
    {
        return this.mc.level != null ? this.mc.level.getMinY() : -64;
    }

    @Override
    public int getHeight()
    {
        return this.mc.level != null ? this.mc.level.getHeight() : 384;
    }

    // The following HeightLimitView overrides are to work around an incompatibility with Lithium 0.7.4+

    @Override
    public int getMaxY()
    {
        return this.getMinY() + this.getHeight();
    }

    @Override
    public int getMinSectionY()
    {
        return this.getMinY() >> 4;
    }

    @Override
    public int getMaxSectionY()
    {
        return this.getMaxY() >> 4;
    }

    @Override
    public int getSectionsCount()
    {
        return this.getMaxSectionY() - this.getMinSectionY();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos)
    {
        return this.isOutsideBuildHeight(pos.getY());
    }

    @Override
    public boolean isOutsideBuildHeight(int y)
    {
        return (y < this.getMinY()) || (y >= this.getMaxY());
    }

    @Override
    public int getSectionIndex(int y)
    {
        return (y >> 4) - (this.getMinY() >> 4);
    }

    @Override
    public int getSectionIndexFromSectionY(int coord)
    {
        return coord - (this.getMinY() >> 4);
    }

    @Override
    public int getSectionYFromSectionIndex(int index)
    {
        return index + (this.getMinY() >> 4);
    }

    @Override
    public @Nonnull String gatherChunkSourceStats()
    {
        return "Chunks[FAKE] W: " + this.getChunkSource().gatherStats();
    }

	@Override
	public void setRespawnData(@Nonnull LevelData.RespawnData spawnPoint)
	{
		// NO-OP
	}

    @Override
    public @Nonnull LevelData.RespawnData getRespawnData()
    {
        return new LevelData.RespawnData(new GlobalPos(Level.OVERWORLD, BlockPos.ZERO), 0.0f, 0.0f);
    }

    @Override
    public @Nonnull RegistryAccess registryAccess()
    {
        if (this.registryManager == null)
        {
            this.registryManager = RenderTweaks.getDynamicRegistryManager();
        }

        return this.registryManager;
    }

	@Override
	public EnvironmentAttributeSystem environmentAttributes()
	{
		return null;
	}

	@Override
    public PotionBrewing potionBrewing()
    {
        return null;
    }

    @Override
    public FuelValues fuelValues()
    {
        return null;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks()
    {
        return null;
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks()
    {
        return null;
    }

    @Override
    public @Nonnull List<? extends Player> players()
    {
        return List.of();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int var1, int var2, int var3)
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
    public @Nonnull FeatureFlagSet enabledFeatures()
    {
        // TODO Auto-generated method stub
        return FeatureFlagSet.of();
    }

    @Override
    public float getShade(@Nonnull Direction var1, boolean var2)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void sendBlockUpdated(@Nonnull BlockPos var1, @Nonnull BlockState var2, @Nonnull BlockState var3, int var4)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSeededSound(@Nullable Entity source, double x, double y, double z, @Nonnull Holder<SoundEvent> sound, @Nonnull SoundSource category, float volume, float pitch, long seed)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void playSeededSound(@Nullable Entity source, @Nonnull Entity entity, @Nonnull Holder<SoundEvent> sound, @Nonnull SoundSource category, float volume, float pitch, long seed)
    {
        // TODO Auto-generated method stub
    }

	@Override
	public void explode(@Nullable Entity entity, @Nullable DamageSource damageSource,
	                    @Nullable ExplosionDamageCalculator behavior,
	                    double x, double y, double z, float power, boolean createFire,
	                    @Nonnull ExplosionInteraction explosionSourceType,
	                    @Nonnull ParticleOptions smallParticle, @Nonnull ParticleOptions largeParticle,
	                    @Nonnull WeightedList<ExplosionParticleInfo> blockParticles,
	                    @Nonnull Holder<SoundEvent> soundEvent)
	{
		// TODO Auto-generated method stub
	}

    @Override
    public Entity getEntity(int var1)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nonnull Collection<EnderDragonPart> dragonParts()
    {
        return List.of();
    }

    @Override
    public TickRateManager tickRateManager()
    {
        return null;
    }

    @Override
    public @Nullable MapItemSavedData getMapData(@Nonnull MapId id)
    {
        return null;
    }

    @Override
    public void destroyBlockProgress(int var1, @Nonnull BlockPos var2, int var3)
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
    public RecipeAccess recipeAccess()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities()
    {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public @Nonnull WorldBorder getWorldBorder()
	{
		return new WorldBorder();
	}
}