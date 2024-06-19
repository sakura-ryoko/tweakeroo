package fi.dy.masa.tweakeroo.mixin;

import fi.dy.masa.tweakeroo.data.ServerDataSyncer;
import net.minecraft.client.network.DataQueryHandler;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DataQueryHandler.class)
public class MixinDataQueryHandler {
    @Inject(
            method = "handleQueryResponse",
            at = @At("HEAD")
    )
    private void queryResponse(int transactionId, NbtCompound nbt, CallbackInfoReturnable<Boolean> cir) {
        ServerDataSyncer.INSTANCE.handleQueryResponse(transactionId, nbt);
    }
}
