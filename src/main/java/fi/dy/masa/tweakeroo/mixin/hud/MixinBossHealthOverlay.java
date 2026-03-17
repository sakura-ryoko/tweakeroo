package fi.dy.masa.tweakeroo.mixin.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(BossHealthOverlay.class)
public abstract class MixinBossHealthOverlay
{
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableBossBarRendering(GuiGraphicsExtractor graphics, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_BOSS_BAR.getBooleanValue())
        {
            ci.cancel();
        }
    }

	@Inject(method = "shouldCreateWorldFog", at = @At("RETURN"), cancellable = true)
	private void tweakeroo_disableBossFog(CallbackInfoReturnable<Boolean> cir)
	{
		if (Configs.Disable.DISABLE_BOSS_FOG.getBooleanValue())
		{
			cir.setReturnValue(false);
		}
	}
}
