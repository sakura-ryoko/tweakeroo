package fi.dy.masa.tweakeroo.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.IGuiEditSign;
import fi.dy.masa.tweakeroo.util.ISignTextAccess;
import fi.dy.masa.tweakeroo.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

@Mixin(SignBlockEntity.class)
public abstract class MixinSignBlockEntity extends BlockEntity implements ISignTextAccess
{
    @Shadow private SignText frontText;
    @Shadow private SignText backText;

    private MixinSignBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState)
    {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void tweakeroo_restoreCopiedText(ValueInput view, CallbackInfo ci)
    {
        // Restore the copied/pasted text after the TileEntity sync overrides it with empty lines
        if (FeatureToggle.TWEAK_SIGN_COPY.getBooleanValue() && this.getLevel() != null && this.getLevel().isClientSide())
        {
            Minecraft mc = Minecraft.getInstance();

            if (mc.screen instanceof SignEditScreen || mc.screen instanceof HangingSignEditScreen)
            {
                if (((IGuiEditSign) mc.screen).tweakeroo$getTile() == (Object) this)
                {
                    MiscUtils.applyPreviousTextToSign((SignBlockEntity) (Object) this, null, ((SignBlockEntity) (Object) this).isFacingFrontText(mc.player));
                }
            }
        }
    }

    @Override
    public SignText tweakeroo$getText(boolean front)
    {
        return front ? this.frontText : this.backText;
    }
}
