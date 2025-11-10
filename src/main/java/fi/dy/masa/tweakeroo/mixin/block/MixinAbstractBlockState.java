package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class MixinAbstractBlockState
{
    // todo this has the same effect as noClip; and causes your player to fall through the world.
//    @Inject(method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
//            at = @At("HEAD"), cancellable = true)
//    private void tweakeroo_checkCollisionState(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir)
//    {
//        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
//        {
//            if (context instanceof EntityShapeContext ctx && ctx.getEntity() instanceof ClientPlayerEntity cli)
//            {
//                Tweakeroo.LOGGER.warn("tweakeroo_checkCollisionState(): pos [{}], instanceof [{}]", pos.toShortString(), (cli instanceof CameraEntity));
////                cir.setReturnValue(VoxelShapes.empty());
//            }
//        }
//    }
}
