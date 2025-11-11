package fi.dy.masa.tweakeroo.world;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

/**
 * Copied From Tweak Fork by Andrew54757
 */
public class FakeChunkManager extends ChunkManager
{
    private final FakeWorld world;
    private final FakeChunk blankChunk;
    private FakeChunkMap chunks;

    public FakeChunkManager(FakeWorld world, int loadDistance)
    {
        super();
        this.world = world;
        this.blankChunk = new FakeChunk(world, new ChunkPos(0, 0));

        chunks = new FakeChunkMap(getChunkMapRadius(loadDistance));
    }

    @Override
    public @Nonnull FakeWorld getWorld()
    {
        return this.world;
    }

    public void loadChunk(int chunkX, int chunkZ)
    {
        FakeChunk chunk = new FakeChunk(this.world, new ChunkPos(chunkX, chunkZ));
        this.chunks.set(chunkX, chunkZ, chunk);
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ)
    {
        return this.chunks.get(chunkX, chunkZ) != null;
    }

    public @Nonnull String getDebugString()
    {
        return "Fake Chunk Cache: " + this.getLoadedChunkCount();
    }

    public int getLoadedChunkCount()
    {
        return 1;
    }

    @Override
    public WorldChunk getChunk(int chunkX, int chunkZ, @Nonnull ChunkStatus status, boolean fallbackToEmpty)
    {
        FakeChunk chunk = this.getChunk(chunkX, chunkZ);
        return chunk == null && fallbackToEmpty ? this.blankChunk : chunk;
    }

    @Override
    public FakeChunk getChunk(int chunkX, int chunkZ)
    {
        FakeChunk chunk = this.chunks.get(chunkX, chunkZ);
        return chunk == null ? this.blankChunk : chunk;
    }

    @Nullable
    public FakeChunk getChunkIfExists(int chunkX, int chunkZ)
    {
        return this.chunks.get(chunkX, chunkZ);
    }

    public void unloadChunk(int chunkX, int chunkZ)
    {
        this.chunks.set(chunkX, chunkZ, null);
    }

    @Override
    public @Nonnull LightingProvider getLightingProvider()
    {
        return null;
    }

    public void setChunkMapCenter(int x, int z)
    {
        this.chunks.centerChunkX = x;
        this.chunks.centerChunkZ = z;
    }


    public ChunkPos getChunkMapCenter()
    {
        return new ChunkPos(this.chunks.centerChunkX, this.chunks.centerChunkZ);
    }

    public int getRadius()
    {
        return this.chunks.radius;
    }

    public void updateLoadDistance(int loadDistance)
    {
        int i = this.chunks.radius;
        int j = getChunkMapRadius(loadDistance);
        if (i != j)
        {
            FakeChunkMap newChunkMap = new FakeChunkMap(j);
            newChunkMap.centerChunkX = this.chunks.centerChunkX;
            newChunkMap.centerChunkZ = this.chunks.centerChunkZ;
            for (int k = 0; k < this.chunks.chunks.length(); ++k)
            {
                FakeChunk chunk = this.chunks.chunks.get(k);
                if (chunk != null)
                {
                    ChunkPos chunkPos = chunk.getPos();
                    newChunkMap.set(chunkPos.x, chunkPos.z, chunk);
                }
            }
            this.chunks = newChunkMap;
        }

    }

    private static int getChunkMapRadius(int loadDistance)
    {
        return Math.max(2, loadDistance) + 3;
    }

    private static final class FakeChunkMap
    {
        final AtomicReferenceArray<FakeChunk> chunks;
        final int radius;
        private final int diameter;
        volatile int centerChunkX;
        volatile int centerChunkZ;

        FakeChunkMap(int radius)
        {
            this.radius = radius;
            this.diameter = radius * 2 + 1;
            this.chunks = new AtomicReferenceArray<>(this.diameter * this.diameter);
        }

        int getIndex(int chunkX, int chunkZ)
        {
            return Math.floorMod(chunkZ, this.diameter) * this.diameter + Math.floorMod(chunkX, this.diameter);
        }

        private void set(int x, int z, @Nullable FakeChunk chunk)
        {
            if (isInRadius(x, z))
            {
                this.chunks.set(getIndex(x, z), chunk);
            }
        }

        private FakeChunk get(int x, int z)
        {
            if (!isInRadius(x, z))
            {
                return null;
            }
            FakeChunk chunk = this.chunks.get(getIndex(x, z));
            if (chunk == null)
            {
                return null;
            }

            if (!positionEquals(chunk, x, z))
            {
                return null;
            }
            return chunk;
        }

        boolean isInRadius(int chunkX, int chunkZ)
        {
            return Math.abs(chunkX - this.centerChunkX) <= this.radius
                    && Math.abs(chunkZ - this.centerChunkZ) <= this.radius;
        }

        private static boolean positionEquals(@Nullable WorldChunk chunk, int x, int z)
        {
            if (chunk == null)
            {
                return false;
            }
            else
            {
                ChunkPos chunkPos = chunk.getPos();
                return chunkPos.x == x && chunkPos.z == z;
            }
        }

    }

    @Override
    public void tick(@Nonnull BooleanSupplier var1, boolean var2)
    {
        // NOOP

    }
}