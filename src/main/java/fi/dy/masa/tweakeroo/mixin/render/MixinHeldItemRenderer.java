package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinHeldItemRenderer
{
    @Redirect(method = "tick()V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
    public float tweakeroo_redirectedGetCooledAttackStrength(LocalPlayer player, float adjustTicks)
    {
        return Configs.Disable.DISABLE_ITEM_SWITCH_COOLDOWN.getBooleanValue() ? 1.0F : player.getAttackStrengthScale(adjustTicks);
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_preventOffhandRendering(AbstractClientPlayer player, float tickProgress, float pitch,
												   InteractionHand hand, float swingProgress, ItemStack item,
												   float equipProgress, PoseStack matrices,
												   SubmitNodeCollector orderedRenderCommandQueue,
												   int light, CallbackInfo ci)
    {
        if (hand == InteractionHand.OFF_HAND && Configs.Disable.DISABLE_OFFHAND_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
