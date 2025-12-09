package fi.dy.masa.tweakeroo.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import fi.dy.masa.tweakeroo.util.IDecorationEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.level.Level;

/**
 * Copied From Tweak Fork by Andrew54757
 */
@Mixin(BlockAttachedEntity.class)
public abstract class MixinBlockAttachedEntity extends Entity implements IDecorationEntity
{
    @Shadow protected BlockPos pos;

    public MixinBlockAttachedEntity(EntityType<?> type, Level world)
    {
        super(type, world);
    }

    @Override
    public BlockPos tweakeroo$getAttached()
    {
        return this.pos;
    }
}