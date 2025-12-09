package fi.dy.masa.tweakeroo.mixin.input;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRules;

@Mixin(value = FillCommand.class, priority = 1001)
public abstract class MixinFillCommand
{
	@SuppressWarnings("unchecked")
    @Redirect(method = "fillBlocks", require = 0,
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"))
    private static <T> T tweakeroo_overrideBlockLimit(GameRules instance, GameRule<T> rule)
    {
        if (FeatureToggle.TWEAK_FILL_CLONE_LIMIT.getBooleanValue() &&
            rule.gameRuleType() == GameRuleType.INT)         // Ensure it's an Integer type
        {
            return (T) (Object) Configs.Generic.FILL_CLONE_LIMIT.getIntegerValue();
        }

        return instance.get(rule);
    }
}
