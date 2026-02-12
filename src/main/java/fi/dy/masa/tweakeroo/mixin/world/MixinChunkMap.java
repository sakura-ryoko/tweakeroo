package fi.dy.masa.tweakeroo.mixin.world;

import java.util.function.BooleanSupplier;
import org.objectweb.asm.Opcodes;

import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.Configs;

/**
 * The "Moonrise" mod breaks this mixin.
 */
@Mixin(value = ChunkMap.class, priority = 990)
public abstract class MixinChunkMap
{
    @Inject(method = "saveChunksEagerly",
            cancellable = true,
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/server/level/ChunkMap;visibleChunkMap:Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;",
                     opcode = Opcodes.GETFIELD)
    )
    private void tweakeroo_disableSaving20ChunksEveryTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CONSTANT_CHUNK_SAVING.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
