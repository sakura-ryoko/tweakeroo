package fi.dy.masa.tweakeroo.mixin.client;

import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @ModifyArgs(method = "handleTickingState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/TickRateManager;setTickRate(F)V"))
    private void tweakeroo_stopPlayerSlowdown(Args args) {
        float tickRate = args.get(0);
        Configs.Internal.REAL_TICK_RATE.setFloatValue((int) tickRate);

        if (!Configs.Disable.DISABLE_TICKRATE_PLAYER_SLOWDOWN.getBooleanValue()) return;

        if (tickRate < 20.0f) {
            args.set(0, 20.0f);
        }
    }
}
