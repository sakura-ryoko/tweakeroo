package fi.dy.masa.tweakeroo.mixin.world;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

@Mixin(World.class)
public abstract class MixinWorld
{
    @Shadow
    @Final
    public boolean isClient;

    @Inject(method = "tickBlockEntities", at = @At("HEAD"), cancellable = true)
    private void disableBlockEntityTicking(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_TILE_ENTITY_TICKING.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "tickEntity(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void preventEntityTicking(Consumer<T> consumer, T entityIn, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_ENTITY_TICKING.getBooleanValue() && (entityIn instanceof PlayerEntity) == false)
        {
            ci.cancel();
        }
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void setBlockStateInject(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> ci)
    {
        if (!this.isClient)
        {
            return;
        }

        if (!RenderTweaks.isPositionValidForRendering(pos))
        {
            if ((flags & RenderTweaks.PASSTHROUGH) != 0)
            {
                return;
            }

            MinecraftClient mc = MinecraftClient.getInstance();
            RenderTweaks.setFakeBlockState(mc.world, pos, state, null);
            ci.setReturnValue(false);
        }
    }
}
