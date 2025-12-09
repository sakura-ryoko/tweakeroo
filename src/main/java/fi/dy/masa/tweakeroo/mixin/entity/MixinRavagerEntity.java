package fi.dy.masa.tweakeroo.mixin.entity;

import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.world.entity.monster.Ravager;

@Mixin(Ravager.class)
public abstract class MixinRavagerEntity
{
    @Redirect(method = "aiStep", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/monster/Ravager;horizontalCollision:Z",
            opcode = Opcodes.GETFIELD))
    private boolean fixDontBreakBlocksOnClient(Ravager entity)
    {
        if (Configs.Fixes.RAVAGER_CLIENT_BLOCK_BREAK_FIX.getBooleanValue())
        {
            return entity.horizontalCollision && entity.level().isClientSide() == false;
        }

        return entity.horizontalCollision;
    }
}
