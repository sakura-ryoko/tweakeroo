package fi.dy.masa.tweakeroo.mixin.input;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.server.command.CloneCommand;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleType;
import net.minecraft.world.rule.GameRules;

@Mixin(value = CloneCommand.class, priority = 999)
public abstract class MixinCloneCommand
{
	@SuppressWarnings("unchecked")
    @Redirect(method = "execute", require = 0,
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/rule/GameRules;getValue(Lnet/minecraft/world/rule/GameRule;)Ljava/lang/Object;"))
    private static <T> T tweakeroo_overrideBlockLimit(GameRules instance, GameRule<T> rule)
    {
        if (FeatureToggle.TWEAK_FILL_CLONE_LIMIT.getBooleanValue() &&
	        rule.getType() == GameRuleType.INT)     // Ensure it's an Integer
        {
            return (T) (Object) Configs.Generic.FILL_CLONE_LIMIT.getIntegerValue();
        }

        return instance.getValue(rule);
    }
}
