package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

/**
 * Copied From Tweak Fork by Andrew54757
 */
@Mixin(PistonBlock.class)
public class MixinPistonBlock
{
    @Environment(EnvType.CLIENT)
    @Inject(method = "onSyncedBlockEvent", at = @At("HEAD"))
    private void onSyncedBlockEventInject(BlockState state, World world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> ci)
    {
        if (!world.isClient)
        {
            return;
        }

        RenderTweaks.onPistonEvent(state, world, pos, type, data);
    }
}
