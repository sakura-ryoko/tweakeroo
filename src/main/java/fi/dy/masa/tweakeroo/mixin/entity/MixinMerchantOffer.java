package fi.dy.masa.tweakeroo.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.world.item.trading.MerchantOffer;

@Mixin(MerchantOffer.class)
public abstract class MixinMerchantOffer
{
    @Inject(method = "increaseUses", at = @At("HEAD"), cancellable = true)
    private void preventTradeLocking(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_VILLAGER_TRADE_LOCKING.getBooleanValue())
        {
            // Prevents the trade from getting locked by not incrementing uses
            ci.cancel();
        }
    }
}
