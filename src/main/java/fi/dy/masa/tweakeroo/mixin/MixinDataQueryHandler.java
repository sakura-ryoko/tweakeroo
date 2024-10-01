package fi.dy.masa.tweakeroo.mixin;

import net.minecraft.client.network.DataQueryHandler;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.ServerDataSyncer;

@Mixin(DataQueryHandler.class)
public class MixinDataQueryHandler
{
    @Inject(
            method = "handleQueryResponse",
            at = @At("HEAD")
    )
    private void queryResponse(int transactionId, NbtCompound nbt, CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue())
        {
            ServerDataSyncer.getInstance().handleVanillaQueryNbt(transactionId, nbt);
        }
    }
}
