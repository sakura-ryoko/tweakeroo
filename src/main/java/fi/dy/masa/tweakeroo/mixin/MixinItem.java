package fi.dy.masa.tweakeroo.mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.item.Item.class)
public abstract class MixinItem
{
    @Shadow public abstract Item asItem();

    /* // TODO 1.19.3+
    @Inject(method = "getGroup", at = @At("HEAD"), cancellable = true)
    private void overrideItemGroup(CallbackInfoReturnable<net.minecraft.item.ItemGroup> cir)
    {
        if (FeatureToggle.TWEAK_CREATIVE_EXTRA_ITEMS.getBooleanValue())
        {
            net.minecraft.item.ItemGroup group = CreativeExtraItems.getGroupFor((net.minecraft.item.Item) (Object) this);

            if (group != null)
            {
                cir.setReturnValue(group);
            }
        }
    }
    */
}
