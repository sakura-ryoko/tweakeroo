package fi.dy.masa.tweakeroo.mixin.option;

import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameOptions.class)
public class MixinGameOptions
{
//    @Inject(method = "getTextBackgroundColor(I)I", at = @At("RETURN"), cancellable = true)
//    private void tweakeroo_tweakChatBackgroundColor(int fallbackColor, CallbackInfoReturnable<Integer> cir)
//    {
//        if (FeatureToggle.TWEAK_CHAT_BACKGROUND_COLOR.getBooleanValue())
//        {
//            cir.setReturnValue(Configs.Generic.CHAT_BACKGROUND_COLOR.getIntegerValue());
//        }
//    }
}
