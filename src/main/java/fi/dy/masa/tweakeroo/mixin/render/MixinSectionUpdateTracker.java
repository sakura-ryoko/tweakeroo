package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.SectionUpdateTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(SectionUpdateTracker.class)
public abstract class MixinSectionUpdateTracker
{
	@Inject(method = "setDirty", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_disableChunkReRenders(int sectionX, int sectionY, int sectionZ, boolean playerChanged, CallbackInfo ci)
	{
		if (Configs.Disable.DISABLE_CHUNK_RENDERING.getBooleanValue())
		{
			ci.cancel();
		}
	}

	@Inject(method = "doesChunkExistAt", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_allowEdgeChunksToRender(ClientLevel level, long sectionNode, CallbackInfoReturnable<Boolean> cir)
	{
		if (FeatureToggle.TWEAK_RENDER_EDGE_CHUNKS.getBooleanValue())
		{
			cir.setReturnValue(true);
		}
	}
}
