package fi.dy.masa.tweakeroo.mixin.input;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.GameRules.Key;

@Mixin(value = FillCommand.class, priority = 1001)
public abstract class MixinFillCommand
{
    @Redirect(method = "fillBlocks", require = 0,
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/level/GameRules;getInt(Lnet/minecraft/world/level/GameRules$Key;)I"))
    private static int tweakeroo_overrideBlockLimit(GameRules instance, Key<IntegerValue> rule)
    {
        if (FeatureToggle.TWEAK_FILL_CLONE_LIMIT.getBooleanValue())
        {
            return Configs.Generic.FILL_CLONE_LIMIT.getIntegerValue();
        }

        return instance.getInt(rule);
    }
}
