package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;

@Mixin(SpawnerRenderer.class)
public abstract class MixinSpawnerRenderer
{
    @Inject(method = "submitEntityInSpawner(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;FFLnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private static void cancelRender(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_MOB_SPAWNER_MOB_RENDER.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
