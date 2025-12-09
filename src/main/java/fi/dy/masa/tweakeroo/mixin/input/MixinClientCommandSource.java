package fi.dy.masa.tweakeroo.mixin.input;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.malilib.util.position.PositionUtils;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(ClientSuggestionProvider.class)
public abstract class MixinClientCommandSource
{
    @Inject(method = "getRelevantCoordinates", at = @At("HEAD"), cancellable = true)
    private void onGetBlockPositionSuggestions(CallbackInfoReturnable<Collection<SharedSuggestionProvider.TextCoordinates>> cir)
    {
        Minecraft mc = Minecraft.getInstance();

        if (FeatureToggle.TWEAK_TAB_COMPLETE_COORDINATE.getBooleanValue() &&
            mc.player != null && (mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS))
        {
            BlockPos pos = PositionUtils.getEntityBlockPos(mc.player);
            //System.out.printf("onGetBlockPositionSuggestions(): suggestedPos: [%s]\n", pos.toShortString());
            cir.setReturnValue(Collections.singleton(new SharedSuggestionProvider.TextCoordinates(formatInt(pos.getX()), formatInt(pos.getY()), formatInt(pos.getZ()))));
        }
    }

    @Inject(method = "getAbsoluteCoordinates", at = @At("HEAD"), cancellable = true)
    private void onGetPositionSuggestions(CallbackInfoReturnable<Collection<SharedSuggestionProvider.TextCoordinates>> cir)
    {
        Minecraft mc = Minecraft.getInstance();

        if (FeatureToggle.TWEAK_TAB_COMPLETE_COORDINATE.getBooleanValue() &&
            mc.player != null && (mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS))
        {
            cir.setReturnValue(Collections.singleton(new SharedSuggestionProvider.TextCoordinates(formatDouble(mc.player.getX()), formatDouble(mc.player.getY()), formatDouble(mc.player.getZ()))));
        }
    }

     @Unique
     private static String formatDouble(double val)
     {
         return String.format(Locale.ROOT, "%.2f", val);
     }

     @Unique
     private static String formatInt(int val)
     {
         return Integer.toString(val);
     }
}
