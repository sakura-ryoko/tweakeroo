package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.renderer.GameRenderer;

/**
 * Separated out for Iris compatibility by adjusting the Mixin Priority
 */
@Mixin(value = GameRenderer.class, priority = 999)
public abstract class MixinGameRenderer_ViewBob
{
    @Shadow protected abstract void bobView(PoseStack matrices, float tickDelta);

    @Redirect(method = "renderLevel", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void tweakeroo_disableWorldViewBob(GameRenderer renderer, PoseStack matrices, float tickDelta)
    {
        if (!Configs.Disable.DISABLE_WORLD_VIEW_BOB.getBooleanValue())
        {
            this.bobView(matrices, tickDelta);
        }
    }
}
