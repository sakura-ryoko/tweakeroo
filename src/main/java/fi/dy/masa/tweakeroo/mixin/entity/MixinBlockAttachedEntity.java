package fi.dy.masa.tweakeroo.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import fi.dy.masa.tweakeroo.util.IDecorationEntity;

/**
 * Copied From Tweak Fork by Andrew54757
 */
@Mixin(BlockAttachedEntity.class)
public abstract class MixinBlockAttachedEntity extends Entity implements IDecorationEntity
{
    @Shadow protected BlockPos attachedBlockPos;

    public MixinBlockAttachedEntity(EntityType<?> type, World world)
    {
        super(type, world);
    }

    @Override
    public BlockPos tweakeroo$getAttached()
    {
        return this.attachedBlockPos;
    }
}