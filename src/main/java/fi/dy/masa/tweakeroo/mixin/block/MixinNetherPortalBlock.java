package fi.dy.masa.tweakeroo.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.NetherPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(NetherPortalBlock.class)
public abstract class MixinNetherPortalBlock
{
    @WrapOperation(method = "animateTick", at = @At(value = "INVOKE",
                                                    target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int tweakeroo_disablePortalSound(RandomSource instance, int i, Operation<Integer> original)
    {
        if (Configs.Disable.DISABLE_NETHER_PORTAL_SOUND.getBooleanValue())
        {
            return -1;
        }

        return original.call(instance, i);
    }
}
