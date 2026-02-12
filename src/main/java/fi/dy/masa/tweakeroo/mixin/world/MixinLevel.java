package fi.dy.masa.tweakeroo.mixin.world;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

@Mixin(Level.class)
public abstract class MixinLevel
{
    @Shadow @Final private boolean isClientSide;

    @Inject(method = "tickBlockEntities", at = @At("HEAD"), cancellable = true)
    private void disableBlockEntityTicking(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_TILE_ENTITY_TICKING.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "guardEntityTick(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void preventEntityTicking(Consumer<T> consumer, T entityIn, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_ENTITY_TICKING.getBooleanValue() && (entityIn instanceof Player) == false)
        {
            ci.cancel();
        }
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void setBlockStateInject(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> ci)
    {
        if (!this.isClientSide)
        {
            return;
        }

        if (!RenderTweaks.isPositionValidForRendering(pos))
        {
            if ((flags & RenderTweaks.PASSTHROUGH) != 0)
            {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            RenderTweaks.setFakeBlockState(mc.level, pos, state, null);
            ci.setReturnValue(false);
        }
    }
}
