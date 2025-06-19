package fi.dy.masa.tweakeroo.mixin.network;

import net.minecraft.client.network.DataQueryHandler;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.data.EntityDataManager;

@Mixin(DataQueryHandler.class)
public class MixinDataQueryHandler
{
    @Inject(
            method = "handleQueryResponse",
            at = @At("HEAD")
    )
    private void tweakeroo_queryResponse(int transactionId, NbtCompound nbt, CallbackInfoReturnable<Boolean> cir)
    {
        if (Configs.Generic.ENTITY_DATA_SYNC.getBooleanValue() ||
	        Configs.Generic.ENTITY_DATA_SYNC_BACKUP.getBooleanValue())
        {
            EntityDataManager.getInstance().handleVanillaQueryNbt(transactionId, nbt);
        }
    }
}
