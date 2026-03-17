package fi.dy.masa.tweakeroo.mixin.render;

// TODO -- This is cursed
//@Mixin(value = GameRenderer.class, priority = 1050)
public abstract class MixinGameRenderer_skipAll
{
//	@Shadow @Final private GuiRenderer guiRenderer;
//	@Shadow @Final private FogRenderer fogRenderer;
//	@Shadow private boolean useUiLightmap;
//	@Shadow @Final private SubmitNodeStorage submitNodeStorage;
//	@Shadow @Final private FeatureRenderDispatcher featureRenderDispatcher;
//	@Shadow @Final private CrossFrameResourcePool resourcePool;
//	@Shadow protected abstract void extractGui(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded);
//	@Shadow public abstract Lighting getLighting();
//
//	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
//	private void tweakeroo_skipAll1(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci)
//	{
//		if (MiscTweaks.skipAlLRendering)
//		{
//			ci.cancel();
//		}
//	}
//
//	@Inject(method = "extract",
//	        at = @At(value = "INVOKE",
//	                 target = "Lnet/minecraft/client/renderer/GameRenderer;extractOptions()V",
//	                 shift = At.Shift.AFTER), cancellable = true)
//	private void tweakeroo_skipAll2(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci,
//	                                @Local(name = "resourcesLoaded") boolean resourcesLoaded)
//	{
//		if (MiscTweaks.skipAlLRendering)
//		{
//			this.extractGui(deltaTracker, advanceGameTime, resourcesLoaded);
//			ci.cancel();
//		}
//	}
//
//	@Inject(method = "render",
//	        at = @At(value = "INVOKE",
//	                 target = "Lnet/minecraft/client/renderer/GlobalSettingsUniform;update(IIDJLnet/minecraft/client/DeltaTracker;ILnet/minecraft/world/phys/Vec3;Z)V",
//	                 shift = At.Shift.AFTER), cancellable = true)
//	private void tweakeroo_skipAll3(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci,
//	                                @Local(name = "profiler") ProfilerFiller profiler,
//	                                @Local(name = "mainRenderTarget") RenderTarget mainRenderTarget)
//	{
//		if (MiscTweaks.skipAlLRendering)
//		{
//			this.fogRenderer.endFrame();
//			RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(mainRenderTarget.getDepthTexture(), 1.0);
//			this.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
//			this.useUiLightmap = true;
//			profiler.push("gui");
//			this.guiRenderer.render(this.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
//			this.guiRenderer.endFrame();
//			profiler.pop();
//			this.useUiLightmap = false;
//			this.submitNodeStorage.endFrame();
//			this.featureRenderDispatcher.endFrame();
//			this.resourcePool.endFrame();
//			profiler.pop();
//			ci.cancel();
//		}
//	}
}
