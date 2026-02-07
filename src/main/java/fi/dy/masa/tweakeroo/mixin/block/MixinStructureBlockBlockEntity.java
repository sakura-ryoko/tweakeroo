package fi.dy.masa.tweakeroo.mixin.block;

//@Mixin(value = StructureBlockEntity.class, priority = 990)
@Deprecated
public abstract class MixinStructureBlockBlockEntity
//        extends BlockEntity
{
//    private MixinStructureBlockBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState)
//    {
//        super(blockEntityType, blockPos, blockState);
//    }
//
//    @ModifyConstant(method = "loadAdditional",
//                    slice = @Slice(from = @At(value = "FIELD",
//                                              target = "Lnet/minecraft/world/level/block/entity/StructureBlockEntity;metaData:Ljava/lang/String;",
//                                              opcode = Opcodes.PUTFIELD),
//                                   to = @At(value = "FIELD",
//                                            target = "Lnet/minecraft/world/level/block/entity/StructureBlockEntity;structureSize:Lnet/minecraft/core/Vec3i;",
//                                            opcode = Opcodes.PUTFIELD)),
//                    constant = {@Constant(intValue = -48), @Constant(intValue = 48)}, remap = false, require = -1)
//    private int overrideMaxSize(int original)
//    {
//        if (FeatureToggle.TWEAK_STRUCTURE_BLOCK_LIMIT.getBooleanValue())
//        {
//            int overridden = Configs.Generic.STRUCTURE_BLOCK_MAX_SIZE.getIntegerValue();
//            return original == -48 ? -overridden : overridden;
//        }
//
//        return original;
//    }
//
//    @Inject(method = "getRelatedCorners", at = @At("HEAD"), cancellable = true)
//    private void overrideCornerBlockScan(BlockPos start, BlockPos end, CallbackInfoReturnable<Stream<BlockPos>> cir)
//    {
//        if (FeatureToggle.TWEAK_STRUCTURE_BLOCK_LIMIT.getBooleanValue())
//        {
//            BlockPos pos = this.getBlockPos();
//            Level world = this.getLevel();
//            String name = ((StructureBlockEntity) (Object) this).getStructureName();
//            int maxSize = Configs.Generic.STRUCTURE_BLOCK_MAX_SIZE.getIntegerValue();
//            int maxOffset = 48;
//
//            // Expand by the maximum position/offset and a bit of margin
//            final int minX = pos.getX() - maxSize - maxOffset - 2;
//            final int minZ = pos.getZ() - maxSize - maxOffset - 2;
//            final int maxX = pos.getX() + maxSize + maxOffset + 2;
//            final int maxZ = pos.getZ() + maxSize + maxOffset + 2;
//
//            final int minY = Math.max(world.getMinY() , pos.getY() - maxSize - maxOffset - 2);
//            final int maxY = Math.min(world.getMaxY(), pos.getY() + maxSize + maxOffset + 2);
//            List<BlockPos> positions = new ArrayList<>();
//
//            for (int cz = minZ >> 4; cz <= (maxZ >> 4); ++cz)
//            {
//                for (int cx = minX >> 4; cx <= (maxX >> 4); ++cx)
//                {
//                    LevelChunk chunk = world.getChunk(cx, cz);
//
//                    if (chunk == null)
//                    {
//                        continue;
//                    }
//
//                    Collection<BlockEntity> list = chunk.getBlockEntities().values();
//
//                    for (BlockEntity te : list)
//                    {
//                        if (te instanceof StructureBlockEntity)
//                        {
//                            StructureBlockEntity tes = (StructureBlockEntity) te;
//                            BlockPos p = te.getBlockPos();
//
//                            if (tes.getMode() == StructureMode.CORNER &&
//                                Objects.equals(tes.getStructureName(), name) &&
//                                p.getX() >= minX && p.getX() <= maxX &&
//                                p.getY() >= minY && p.getY() <= maxY &&
//                                p.getZ() >= minZ && p.getZ() <= maxZ)
//                            {
//                                positions.add(p);
//                            }
//                        }
//                    }
//                }
//            }
//
//            cir.setReturnValue(positions.stream());
//        }
//    }
}
