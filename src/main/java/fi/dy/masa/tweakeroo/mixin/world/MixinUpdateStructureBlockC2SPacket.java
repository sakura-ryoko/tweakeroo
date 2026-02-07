package fi.dy.masa.tweakeroo.mixin.world;

//@Mixin(value = ServerboundSetStructureBlockPacket.class, priority = 990)
@Deprecated
public abstract class MixinUpdateStructureBlockC2SPacket
{
//    @ModifyConstant(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
//            slice = @Slice(from = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
//                                      target = "Lnet/minecraft/network/protocol/game/ServerboundSetStructureBlockPacket;name:Ljava/lang/String;"),
//                           to   = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
//                                      target = "Lnet/minecraft/network/protocol/game/ServerboundSetStructureBlockPacket;mirror:Lnet/minecraft/world/level/block/Mirror;")),
//            constant = { @Constant(intValue = -48), @Constant(intValue = 48) }, require = 0)
//    private int tweakeroo_overrideStructureBlockSizeLimit(int original)
//    {
//        if (FeatureToggle.TWEAK_STRUCTURE_BLOCK_LIMIT.getBooleanValue())
//        {
//            int overridden = Configs.Generic.STRUCTURE_BLOCK_MAX_SIZE.getIntegerValue();
//            return original == -48 ? -overridden : overridden;
//        }
//
//        return original;
//    }
}
