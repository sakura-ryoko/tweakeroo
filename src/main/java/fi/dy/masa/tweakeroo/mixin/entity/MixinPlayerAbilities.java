package fi.dy.masa.tweakeroo.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

@Mixin(Abilities.class)
public abstract class MixinPlayerAbilities
{
    @Inject(method = "getFlyingSpeed", at = @At("HEAD"), cancellable = true)
    private void overrideFlySpeed(CallbackInfoReturnable<Float> cir)
    {
        Player player = Minecraft.getInstance().player;

        if (FeatureToggle.TWEAK_FLY_SPEED.getBooleanValue() &&
            player != null && player.getAbilities().mayfly)
        {
            cir.setReturnValue((float) Configs.getActiveFlySpeedConfig().getDoubleValue());
        }
    }
}
