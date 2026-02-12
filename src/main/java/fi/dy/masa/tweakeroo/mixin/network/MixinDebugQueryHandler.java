package fi.dy.masa.tweakeroo.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.data.EntityDataManager;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.nbt.CompoundTag;

@Mixin(DebugQueryHandler.class)
public class MixinDebugQueryHandler
{
    @Inject(
            method = "handleResponse",
            at = @At("HEAD")
    )
    private void tweakeroo_queryResponse(int transactionId, CompoundTag nbt, CallbackInfoReturnable<Boolean> cir)
    {
        if (Configs.Generic.ENTITY_DATA_SYNC.getBooleanValue() ||
	        Configs.Generic.ENTITY_DATA_SYNC_BACKUP.getBooleanValue())
        {
            EntityDataManager.getInstance().handleVanillaQueryNbt(transactionId, nbt);
        }
    }
}
