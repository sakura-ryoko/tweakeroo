package fi.dy.masa.tweakeroo.mixin.network;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.objectweb.asm.Opcodes;

import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

import fi.dy.masa.malilib.compat.ModIds;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = ServerboundSetStructureBlockPacket.class, priority = 999)
@Restriction(conflict = @Condition(value = ModIds.carpet))
public abstract class MixinServerboundSetStructureBlockPacket
{
    @ModifyConstant(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
                    slice = @Slice(from = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                                      target = "Lnet/minecraft/network/protocol/game/ServerboundSetStructureBlockPacket;name:Ljava/lang/String;"),
                           to   = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                                      target = "Lnet/minecraft/network/protocol/game/ServerboundSetStructureBlockPacket;mirror:Lnet/minecraft/world/level/block/Mirror;")),
                    constant = {@Constant(intValue = -StructureBlockEntity.MAX_SIZE_PER_AXIS),
                                @Constant(intValue = StructureBlockEntity.MAX_SIZE_PER_AXIS)},
                    require = 0)
    private int tweakeroo_overrideStructureBlockSizeLimit(int original)
    {
        if (FeatureToggle.TWEAK_STRUCTURE_BLOCK_LIMIT.getBooleanValue())
        {
            int overridden = Configs.Generic.STRUCTURE_BLOCK_MAX_SIZE.getIntegerValue();
            return original == -StructureBlockEntity.MAX_SIZE_PER_AXIS ? -overridden : overridden;
        }

        return original;
    }
}
