package fi.dy.masa.tweakeroo.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.LevelAccessor;

@Mixin(Bat.class)
public abstract class MixinBatEntity
{
    @Inject(method = "checkBatSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void tweakeroo_disableBatSpawning(EntityType<Bat> type,
                                                     LevelAccessor world,
                                                     EntitySpawnReason spawnReason,
                                                     BlockPos pos,
                                                     RandomSource random,
                                                     CallbackInfoReturnable<Boolean> cir)
    {
        if (Configs.Disable.DISABLE_BAT_SPAWNING.getBooleanValue())
        {
            cir.setReturnValue(false);
        }
    }
}
