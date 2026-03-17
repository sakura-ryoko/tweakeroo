package fi.dy.masa.tweakeroo.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;

import net.minecraft.world.entity.monster.Ravager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(Ravager.class)
public abstract class MixinRavagerEntity
{
    @WrapOperation(method = "aiStep", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/monster/Ravager;horizontalCollision:Z",
            opcode = Opcodes.GETFIELD))
    private boolean tweakeroo_fixDontBreakBlocksOnClient(Ravager instance, Operation<Boolean> original)
    {
        if (Configs.Fixes.RAVAGER_CLIENT_BLOCK_BREAK_FIX.getBooleanValue())
        {
            return instance.horizontalCollision && instance.level().isClientSide() == false;
        }

        return original.call(instance);
    }
}
