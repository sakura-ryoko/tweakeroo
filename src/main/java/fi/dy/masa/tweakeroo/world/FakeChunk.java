package fi.dy.masa.tweakeroo.world;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Copied From Tweak Fork by Andrew54757
 */
public class FakeChunk extends LevelChunk
{
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private final int bottomY;
    private final int topY;
    private boolean isEmpty = true;

    public FakeChunk(FakeWorld world, ChunkPos pos)
    {
        super(world, pos);
        this.bottomY = world.getMinY();
        this.topY = world.getMaxY();
    }

    @Override
    public @Nonnull BlockState getBlockState(BlockPos pos)
    {
        int x = pos.getX() & 0xF;
        int y = pos.getY();
        int z = pos.getZ() & 0xF;
        int cy = this.getSectionIndex(y);
        y &= 0xF;

        LevelChunkSection[] sections = this.getSections();

        if (cy >= 0 && cy < sections.length)
        {
            LevelChunkSection chunkSection = sections[cy];

            if (!chunkSection.hasOnlyAir())
            {
                return chunkSection.getBlockState(x, y, z);
            }
        }

        return AIR;
    }

    @Override
    public BlockState setBlockState(@Nonnull BlockPos pos, @Nonnull BlockState state, int flags)
    {
        BlockState stateOld = this.getBlockState(pos);
        int y = pos.getY();

        if (stateOld == state || y >= this.topY || y < this.bottomY)
        {
            return null;
        }
        else
        {
            int x = pos.getX() & 15;
            int z = pos.getZ() & 15;
            int cy = this.getSectionIndex(y);

            Block blockNew = state.getBlock();
            Block blockOld = stateOld.getBlock();
            LevelChunkSection section = this.getSections()[cy];

            if (section.hasOnlyAir() && state.isAir())
            {
                return null;
            }

            y &= 0xF;

            if (state.isAir() == false)
            {
                this.isEmpty = false;
            }

            section.setBlockState(x, y, z, state);

            if (blockOld != blockNew)
            {
                this.getLevel().removeBlockEntity(pos);
            }

            if (section.getBlockState(x, y, z).getBlock() != blockNew)
            {
                return null;
            }
            else
            {
                // if (state.hasBlockEntity() && blockNew instanceof BlockEntityProvider)
                // {
                //     BlockEntity te = this.getBlockEntity(pos, WorldChunk.CreationType.CHECK);

                //     if (te == null)
                //     {
                //         te = ((BlockEntityProvider) blockNew).createBlockEntity(pos, state);

                //         if (te != null)
                //         {
                //             this.getWorld().getWorldChunk(pos).setBlockEntity(te);
                //         }
                //     }
                // }

                this.isUnsaved();
                return stateOld;
            }
        }
    }


    @Override
    public boolean isEmpty()
    {
        return this.isEmpty;
    }
}
