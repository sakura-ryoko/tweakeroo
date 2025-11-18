package fi.dy.masa.tweakeroo.mixin.hud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;

@Mixin(BossBarHud.class)
public abstract class MixinBossBarHud
{
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableBossBarRendering(DrawContext drawContext, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_BOSS_BAR.getBooleanValue())
        {
            ci.cancel();
        }
    }

	@Inject(method = "shouldThickenFog", at = @At("RETURN"), cancellable = true)
	private void tweakeroo_disableBossFog(CallbackInfoReturnable<Boolean> cir)
	{
		if (Configs.Disable.DISABLE_BOSS_FOG.getBooleanValue())
		{
			cir.setReturnValue(false);
		}
	}
}
