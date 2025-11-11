package fi.dy.masa.tweakeroo.mixin.screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.MiscUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen
{
    @Shadow protected TextFieldWidget chatField;
    @Mutable @Shadow protected String originalChatText;

    @Inject(method = "removed", at = @At("HEAD"))
    private void tweakeroo_storeChatText(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_CHAT_PERSISTENT_TEXT.getBooleanValue())
        {
            MiscUtils.setLastChatText(this.chatField.getText());
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tweakeroo_restoreText(String text, boolean draft, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_CHAT_PERSISTENT_TEXT.getBooleanValue() &&
			MiscUtils.getLastChatText().isEmpty() == false)
        {
            this.originalChatText = MiscUtils.getLastChatText();
        }
    }

    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyInput;)Z",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatScreen;sendMessage(Ljava/lang/String;Z)V")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", shift = Shift.AFTER))
    private void tweakeroo_onSendMessage(KeyInput input, CallbackInfoReturnable<Boolean> cir)
    {
        MiscUtils.setLastChatText("");
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = Integer.MIN_VALUE))
    private int overrideChatBackgroundColor(int original)
    {
        if (FeatureToggle.TWEAK_CHAT_BACKGROUND_COLOR.getBooleanValue())
        {
            return Configs.Generic.CHAT_BACKGROUND_COLOR.getIntegerValue();
        }

        return original;
    }
}
