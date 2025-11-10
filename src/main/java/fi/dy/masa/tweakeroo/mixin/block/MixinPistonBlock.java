package fi.dy.masa.tweakeroo.mixin.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

/**
 * Copied From Tweak Fork by Andrew54757
 */
@Mixin(PistonBaseBlock.class)
public class MixinPistonBlock
{
    @Environment(EnvType.CLIENT)
    @Inject(method = "triggerEvent", at = @At("HEAD"))
    private void onSyncedBlockEventInject(BlockState state, Level world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> ci)
    {
        if (!world.isClientSide())
        {
            return;
        }

        RenderTweaks.onPistonEvent(state, world, pos, type, data);
    }
}
