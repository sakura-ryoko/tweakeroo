package fi.dy.masa.tweakeroo.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;

@Mixin(NetherPortalBlock.class)
public abstract class MixinNetherPortalBlock
{
    @Redirect(method = "animateTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
    private void tweakeroo_disablePortalSound(Level instance, double x, double y, double z, SoundEvent sound,
                                              SoundSource category, float volume, float pitch, boolean useDistance)
    {
        if (Configs.Disable.DISABLE_NETHER_PORTAL_SOUND.getBooleanValue() == false)
        {
            instance.playLocalSound(x, y, z, sound, category, volume, pitch, useDistance);
        }
    }
}
