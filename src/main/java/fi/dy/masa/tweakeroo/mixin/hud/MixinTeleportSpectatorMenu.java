package fi.dy.masa.tweakeroo.mixin.hud;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.spectator.PlayerMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.categories.TeleportToPlayerMenuCategory;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(TeleportToPlayerMenuCategory.class)
public abstract class MixinTeleportSpectatorMenu
{
    @Shadow @Final private static Comparator<PlayerInfo> PROFILE_ORDER;
    @Shadow @Final @Mutable private List<SpectatorMenuItem> items;

    @Inject(method = "<init>(Ljava/util/Collection;)V", at = @At("RETURN"))
    private void allowSpectatorTeleport(Collection<PlayerInfo> profiles, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_SPECTATOR_TELEPORT.getBooleanValue())
        {
            this.items = profiles.stream().sorted(PROFILE_ORDER).map(
                    entry -> (SpectatorMenuItem) new PlayerMenuItem(entry)).toList();
        }
    }
}
