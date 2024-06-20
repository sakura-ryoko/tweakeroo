package fi.dy.masa.tweakeroo.mixin;

import java.util.function.BooleanSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.world.ServerChunkLoadingManager;
import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(ServerChunkLoadingManager.class)
public abstract class MixinServerChunkLoadingManager
{
    @Inject(method = "unloadChunks", cancellable = true, at = @At(value = "FIELD",
            target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;chunkHolders:Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;"))
    private void tweakeroo_disableSaving20ChunksEveryTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CONSTANT_CHUNK_SAVING.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
