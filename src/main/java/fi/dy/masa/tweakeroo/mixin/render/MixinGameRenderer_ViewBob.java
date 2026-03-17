package fi.dy.masa.tweakeroo.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.malilib.compat.ModIds;
import fi.dy.masa.tweakeroo.config.Configs;

/**
 * Separated out for Iris compatibility by adjusting the Mixin Priority
 */
@Mixin(value = GameRenderer.class, priority = 1001)
@Restriction(conflict = @Condition(value = ModIds.iris))
public abstract class MixinGameRenderer_ViewBob
{
    @Shadow protected abstract void bobView(CameraRenderState cameraState, PoseStack poseStack);

    @WrapOperation(method = "renderLevel",
                   require = 0,
                   at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    )
    private void tweakeroo_disableWorldViewBob(GameRenderer instance, CameraRenderState cameraState, PoseStack poseStack, Operation<Void> original)
    {
        if (!Configs.Disable.DISABLE_WORLD_VIEW_BOB.getBooleanValue())
        {
            this.bobView(cameraState, poseStack);
        }
    }
}
