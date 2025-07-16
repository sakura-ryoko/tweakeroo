package fi.dy.masa.tweakeroo.mixin.hud;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.profiler.Profiler;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(value = ChatHud.class, priority = 1100)
public abstract class MixinChatHud
{
//    @Shadow protected abstract int getIndicatorX(ChatHudLine.Visible line);
//    @Shadow protected abstract void drawIndicatorIcon(DrawContext context, int x, int y, MessageIndicator.Icon icon);
//
//    @Unique DrawContext localContext;
//    @Unique private int localK;
//    @Unique private int localN;
//    @Unique private int localO;
//    @Unique private float localH;
//    @Unique private float localG;

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
                    at = @At("HEAD"), argsOnly = true)
    private Text tweakeroo_addMessageTimestamp(Text componentIn, Text parameterMessage, MessageSignatureData data, MessageIndicator indicator)
    {
        if (FeatureToggle.TWEAK_CHAT_TIMESTAMP.getBooleanValue())
        {
            MutableText newComponent = Text.literal(MiscUtils.getChatTimestamp() + " ");
            newComponent.append(componentIn);
            return newComponent;
        }

        return componentIn;
    }

    // FIXME --> This DOES work, but I hate it until I hear from Masa.
//    @Inject(method = "render",
//            at = @At(value = "INVOKE",
//                     target = "Lnet/minecraft/client/gui/hud/ChatHud;method_71990(IIZILnet/minecraft/client/gui/hud/ChatHud$class_11511;)I")
//    )
//    private void tweakeroo_captureChatBackgroundLocals(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci,
//                                                       @Local(ordinal = 5) int k,
//                                                       @Local(ordinal = 8) int n,
//                                                       @Local(ordinal = 1) float g,
//                                                       @Local(ordinal = 2) float h,
//                                                       @Local(ordinal = 9) int o)
//    {
//        this.localContext = context;
//        this.localH = h;
//        this.localO = o;
//        this.localG = g;
//        this.localN = n;
//        this.localK = k;
//    }
//
//    // This is just dumb.
//    @ModifyArgs(method = "render",
//              at = @At(value = "INVOKE",
//                       target = "Lnet/minecraft/client/gui/hud/ChatHud;method_71990(IIZILnet/minecraft/client/gui/hud/ChatHud$class_11511;)I",
//                       ordinal = 0)
//    )
//    private void tweakeroo_overrideChatBackgroundColor(Args args)
//    {
//        if (FeatureToggle.TWEAK_CHAT_BACKGROUND_COLOR.getBooleanValue() && args.size() == 5)
//        {
//            ChatHud.class_11511 drawBackground =
//                    (lx, mx, nx, visible, ox, hx) ->
//            {
//                this.localContext.fill(lx - 4, mx, lx + this.localK + 4 + 4, nx, MiscUtils.getChatBackgroundColor(ColorHelper.withAlpha(hx * this.localH, Colors.BLACK)));
//                MessageIndicator messageIndicator = visible.indicator();
//
//                if (messageIndicator != null)
//                {
//                    int px = ColorHelper.withAlpha(hx * this.localG, messageIndicator.indicatorColor());
//
//                    this.localContext.fill(lx - 4, mx, lx - 2, nx, px);
//
//                    if (ox == this.localN && messageIndicator.icon() != null)
//                    {
//                        int qx = this.getIndicatorX(visible);
//                        int rx = nx + this.localO + 9;
//
//                        this.drawIndicatorIcon(this.localContext, qx, rx, messageIndicator.icon());
//                    }
//                }
//            };
//
//            args.set(4, drawBackground);
//        }
//    }

// BROKEN
//    @Redirect(method = "render",
//              at = @At(value = "INVOKE",
//                target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V",
//                       ordinal = 0))
//    private void overrideChatBackgroundColor(DrawContext drawableHelper, int left, int top, int right, int bottom, int color)
//    {
//        if (FeatureToggle.TWEAK_CHAT_BACKGROUND_COLOR.getBooleanValue())
//        {
//            color = MiscUtils.getChatBackgroundColor(color);
//        }
//
//        drawableHelper.fill(left, top, right, bottom, color);
//    }
}
