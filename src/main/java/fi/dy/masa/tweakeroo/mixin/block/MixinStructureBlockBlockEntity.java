package fi.dy.masa.tweakeroo.mixin.block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = StructureBlockEntity.class, priority = 999)
public abstract class MixinStructureBlockBlockEntity extends BlockEntity
{
    private MixinStructureBlockBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState)
    {
        super(blockEntityType, blockPos, blockState);
    }

    @ModifyConstant(method = "loadAdditional",
                    slice = @Slice(from = @At(value = "FIELD",
                                              target = "Lnet/minecraft/world/level/block/entity/StructureBlockEntity;metaData:Ljava/lang/String;"),
                                   to = @At(value = "FIELD",
                                            target = "Lnet/minecraft/world/level/block/entity/StructureBlockEntity;structureSize:Lnet/minecraft/core/Vec3i;")),
                    constant = { @Constant(intValue = -48), @Constant(intValue = 48) }, require = 0)
    private int overrideMaxSize(int original)
    {
        if (FeatureToggle.TWEAK_STRUCTURE_BLOCK_LIMIT.getBooleanValue())
        {
            int overridden = Configs.Generic.STRUCTURE_BLOCK_MAX_SIZE.getIntegerValue();
            return original == -48 ? -overridden : overridden;
        }

        return original;
    }

    @Inject(method = "getRelatedCorners", at = @At("HEAD"), cancellable = true)
    private void overrideCornerBlockScan(BlockPos start, BlockPos end, CallbackInfoReturnable<Stream<BlockPos>> cir)
    {
        if (FeatureToggle.TWEAK_STRUCTURE_BLOCK_LIMIT.getBooleanValue())
        {
            BlockPos pos = this.getBlockPos();
            Level world = this.getLevel();
            String name = ((StructureBlockEntity) (Object) this).getStructureName();
            int maxSize = Configs.Generic.STRUCTURE_BLOCK_MAX_SIZE.getIntegerValue();
            int maxOffset = 48;

            // Expand by the maximum position/offset and a bit of margin
            final int minX = pos.getX() - maxSize - maxOffset - 2;
            final int minZ = pos.getZ() - maxSize - maxOffset - 2;
            final int maxX = pos.getX() + maxSize + maxOffset + 2;
            final int maxZ = pos.getZ() + maxSize + maxOffset + 2;

            final int minY = Math.max(world.getMinY() , pos.getY() - maxSize - maxOffset - 2);
            final int maxY = Math.min(world.getMaxY(), pos.getY() + maxSize + maxOffset + 2);
            List<BlockPos> positions = new ArrayList<>();

            for (int cz = minZ >> 4; cz <= (maxZ >> 4); ++cz)
            {
                for (int cx = minX >> 4; cx <= (maxX >> 4); ++cx)
                {
                    LevelChunk chunk = world.getChunk(cx, cz);

                    if (chunk == null)
                    {
                        continue;
                    }

                    Collection<BlockEntity> list = chunk.getBlockEntities().values();

                    for (BlockEntity te : list)
                    {
                        if (te instanceof StructureBlockEntity)
                        {
                            StructureBlockEntity tes = (StructureBlockEntity) te;
                            BlockPos p = te.getBlockPos();

                            if (tes.getMode() == StructureMode.CORNER &&
                                Objects.equals(tes.getStructureName(), name) &&
                                p.getX() >= minX && p.getX() <= maxX &&
                                p.getY() >= minY && p.getY() <= maxY &&
                                p.getZ() >= minZ && p.getZ() <= maxZ)
                            {
                                positions.add(p);
                            }
                        }
                    }
                }
            }

            cir.setReturnValue(positions.stream());
        }
    }
}
