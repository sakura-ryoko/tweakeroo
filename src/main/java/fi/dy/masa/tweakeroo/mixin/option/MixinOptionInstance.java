package fi.dy.masa.tweakeroo.mixin.option;

import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;

@Mixin(value = OptionInstance.class, priority = 1010)
public abstract class MixinOptionInstance<T>
{
	@Mutable @Shadow @Final private OptionInstance.ValueSet<T> values;
	@Mutable @Shadow @Final private Codec<T> codec;
	@Shadow private T value;

	@Unique private boolean isGamma;
	@Unique private T preValue;

	@SuppressWarnings("unchecked")
	@ModifyArgs(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/OptionInstance$TooltipSupplier;Lnet/minecraft/client/OptionInstance$CaptionBasedToString;"+
						        "Lnet/minecraft/client/OptionInstance$ValueSet;Lcom/mojang/serialization/Codec;Ljava/lang/Object;Ljava/util/function/Consumer;)V",
	            at = @At(value = "INVOKE",
	                     target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"
	            )
	)
	private void tweakeroo_onInit(Args args)
	{
		if (args.get(0).equals("options.gamma"))
		{
			this.isGamma = true;
			this.values = (OptionInstance.ValueSet<T>) MiscTweaks.GammaOverrideValue.INSTANCE;
			this.codec = (Codec<T>) MiscTweaks.GammaOverrideValue.INSTANCE.codec();
			// Allows values of up to 32.0
		}
	}

	@Inject(method = "set", at = @At("HEAD"))
	private void tweakeroo_onValueSetPre(T object, CallbackInfo ci)
	{
		if (this.isGamma)
		{
			this.preValue = this.value;
		}
	}

	@Inject(method = "set", at = @At("RETURN"))
	private void tweakeroo_onValueSetPost(T object, CallbackInfo ci)
	{
		if (this.isGamma)
		{
			MiscTweaks.onVanillaGammaChange((Double) this.preValue, (Double) this.value);
		}
	}
}
