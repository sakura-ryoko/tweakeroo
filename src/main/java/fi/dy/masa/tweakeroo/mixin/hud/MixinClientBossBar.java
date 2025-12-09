package fi.dy.masa.tweakeroo.mixin.hud;

import java.util.UUID;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(LerpingBossEvent.class)
public abstract class MixinClientBossBar extends BossEvent
{
    public MixinClientBossBar(UUID uniqueIdIn, Component nameIn, BossEvent.BossBarColor colorIn, BossEvent.BossBarOverlay styleIn)
    {
        super(uniqueIdIn, nameIn, colorIn, styleIn);
    }

    @Override
    public boolean shouldCreateWorldFog()
    {
        if (Configs.Disable.DISABLE_BOSS_FOG.getBooleanValue())
        {
            return false;
        }

        return super.shouldCreateWorldFog();
    }
}
