package fi.dy.masa.tweakeroo.mixin;

import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.ServerDataSyncer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.DataQueryHandler;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DataQueryHandler.class)
public class MixinDataQueryHandler
{
    @Shadow @Final private ClientPlayNetworkHandler networkHandler;

    @Inject(
            method = "handleQueryResponse",
            at = @At("HEAD")
    )
    private void queryResponse(int transactionId, NbtCompound nbt, CallbackInfoReturnable<Boolean> cir)
    {
        Tweakeroo.logger.warn("MixinDataQueryHandler: nbt {}", nbt.toString());

        if (FeatureToggle.TWEAK_SERVER_ENTITY_DATA_SYNCER.getBooleanValue())
        {
            ServerDataSyncer.getInstance().handleQueryResponse(transactionId, nbt);
        }
    }
}
