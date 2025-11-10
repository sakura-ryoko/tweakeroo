package fi.dy.masa.tweakeroo.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.ServerDataSyncer;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.nbt.CompoundTag;

@Mixin(DebugQueryHandler.class)
public class MixinDataQueryHandler
{
    @Inject(
            method = "handleResponse",
            at = @At("HEAD")
    )
    private void tweakeroo_queryResponse(int transactionId, CompoundTag nbt, CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue())
        {
            ServerDataSyncer.getInstance().handleVanillaQueryNbt(transactionId, nbt);
        }
    }
}
