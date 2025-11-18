package fi.dy.masa.tweakeroo.mixin.hud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.MiscUtils;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

@Mixin(value = ChatHud.class, priority = 1100)
public abstract class MixinChatHud
{
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

	// 1.21.10:
	// method_71992(Lnet/minecraft/client/gui/GuiGraphics;IFFIIIIILnet/minecraft/client/GuiMessage$Line;IF)V // ARGB;color
	// 25w46a:
	// method_75802(IILnet/minecraft/client/gui/hud/ChatHud$Backend;IFLnet/minecraft/client/gui/hud/ChatHudLine$Visible;IF)V
	//
    // INVOKEVIRTUAL Bytecode Mixin
    @Redirect(method = "method_75802(IILnet/minecraft/client/gui/hud/ChatHud$Backend;IFLnet/minecraft/client/gui/hud/ChatHudLine$Visible;IF)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/util/math/ColorHelper;toAlpha(F)I",
                       ordinal = 0))
    private static int tweakeroo_overrideChatBackgroundColor(float alpha)
    {
        if (FeatureToggle.TWEAK_CHAT_BACKGROUND_COLOR.getBooleanValue())
        {
            return MiscUtils.getChatBackgroundColor(ColorHelper.toAlpha(alpha));
        }

//        return ColorHelper.withAlpha(alpha, rgb);
	    return ColorHelper.toAlpha(alpha);
    }
}
