package fi.dy.masa.tweakeroo.mixin.input;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.commands.FillCommand;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = FillCommand.class, priority = 999)
public abstract class MixinFillCommand
{
	@SuppressWarnings("unchecked")
    @WrapOperation(method = "fillBlocks", require = 0,
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"
                   )
    )
    private static <T> T tweakeroo_overrideBlockLimit(GameRules instance, GameRule<T> gameRule, Operation<T> original)
    {
        if (FeatureToggle.TWEAK_FILL_CLONE_LIMIT.getBooleanValue() &&
            gameRule.gameRuleType() == GameRuleType.INT)         // Ensure it's an Integer type
        {
            return (T) (Object) Configs.Generic.FILL_CLONE_LIMIT.getIntegerValue();
        }

        return instance.get(gameRule);
    }
}
