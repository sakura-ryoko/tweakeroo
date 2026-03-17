package fi.dy.masa.tweakeroo.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(NetherPortalBlock.class)
public abstract class MixinNetherPortalBlock
{
    @WrapOperation(method = "animateTick", at = @At(value = "INVOKE",
                                                    target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    private void tweakeroo_disablePortalSound(Level instance, double x, double y, double z, SoundEvent sound, SoundSource source,
                                              float volume, float pitch, boolean distanceDelay,
                                              Operation<Void> original)
    {
        if (Configs.Disable.DISABLE_NETHER_PORTAL_SOUND.getBooleanValue() == false)
        {
            instance.playLocalSound(x, y, z, sound, source, volume, pitch, distanceDelay);
        }

        original.call(instance, x, y, z, sound, source, volume, pitch, distanceDelay);
    }
}
