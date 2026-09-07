package fi.dy.masa.tweakeroo.mixin.block;

import java.util.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.objectweb.asm.Opcodes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import fi.dy.masa.malilib.compat.ModIds;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = StructureBlockEntity.class, priority = 999)
@Restriction(conflict = @Condition(value = ModIds.carpet))
public abstract class MixinStructureBlockEntity_carpetBreaks extends BlockEntity
{
    private MixinStructureBlockEntity_carpetBreaks(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState)
    {
        super(blockEntityType, blockPos, blockState);
    }

    @ModifyConstant(method = "loadAdditional",
                   slice = @Slice(from = @At(value = "FIELD",
                                              target = "Lnet/minecraft/world/level/block/entity/StructureBlockEntity;metaData:Ljava/lang/String;",
                                              opcode = Opcodes.PUTFIELD),
                                   to = @At(value = "FIELD",
                                            target = "Lnet/minecraft/world/level/block/entity/StructureBlockEntity;structureSize:Lnet/minecraft/core/Vec3i;",
                                            opcode = Opcodes.PUTFIELD)),
                   constant = {@Constant(intValue = -StructureBlockEntity.MAX_SIZE_PER_AXIS),
                               @Constant(intValue = StructureBlockEntity.MAX_SIZE_PER_AXIS)},
                   require = 0
    )
    private int tweakeroo_overrideMaxSize(int original)
    {
        if (FeatureToggle.TWEAK_STRUCTURE_BLOCK_LIMIT.getBooleanValue())
        {
            int overridden = Configs.Generic.STRUCTURE_BLOCK_MAX_SIZE.getIntegerValue();
            return original == -StructureBlockEntity.MAX_SIZE_PER_AXIS ? -overridden : overridden;
        }

        return original;
    }

    @SuppressWarnings("deprecation")
    @WrapMethod(method = "getEnclosingBoundingBox")
    private static Optional<BoundingBox> tweakeroo_overrideCornerBlockScan(Level level,
                                                                           BlockPos selfPos,
                                                                           Identifier structureName,
                                                                           Operation<Optional<BoundingBox>> original)
    {
        if (FeatureToggle.TWEAK_STRUCTURE_BLOCK_LIMIT.getBooleanValue())
        {
            MutableInt cornerCount = new MutableInt();
            MutableObject<BoundingBox> result = new MutableObject<>();
            int maxSize = Configs.Generic.STRUCTURE_BLOCK_MAX_SIZE.getIntegerValue();
            int maxOffset = StructureBlockEntity.MAX_SIZE_PER_AXIS;

            // Expand by the maximum position/offset and a bit of margin
            final int minX = selfPos.getX() - maxSize - maxOffset - 2;
            final int minZ = selfPos.getZ() - maxSize - maxOffset - 2;
            final int maxX = selfPos.getX() + maxSize + maxOffset + 2;
            final int maxZ = selfPos.getZ() + maxSize + maxOffset + 2;

            final int minY = Math.max(level.getMinY(), selfPos.getY() - maxSize - maxOffset - 2);
            final int maxY = Math.min(level.getMaxY(), selfPos.getY() + maxSize + maxOffset + 2);

            BlockPos corner1 = new BlockPos(minX, minY, minZ);
            BlockPos corner2 = new BlockPos(maxX, maxY, maxZ);

            level.findBlocksIn(corner1, corner2)
                    .filterState(state -> state.is(Blocks.STRUCTURE_BLOCK))
                    .forEach(
                            (p, s) ->
                            {
                                level.getBlockEntity(p, BlockEntityTypes.STRUCTURE_BLOCK)
                                        .ifPresent(tes ->
                                                   {
                                                       if (tes.getMode() == StructureMode.CORNER &&
                                                           tes.getStructureName().equals(structureName.toString()))
                                                       {
                                                           cornerCount.increment();
                                                           BoundingBox box = result.get();

                                                           if (box == null)
                                                           {
                                                               result.setValue(new BoundingBox(p));
                                                           }
                                                           else
                                                           {
                                                               box.encapsulate(p);
                                                           }
                                                       }
                                                   }
                                                );
                            });

            return switch (cornerCount.get().intValue())
            {
                case 0 -> Optional.empty();
                case 1 -> Optional.of(Objects.requireNonNull(result.get()).encapsulate(selfPos));
                default -> Optional.of(Objects.requireNonNull(result.get()));
            };
        }

        return original.call(level, selfPos, structureName);
    }
}
