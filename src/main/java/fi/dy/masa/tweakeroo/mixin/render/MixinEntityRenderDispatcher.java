package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import fi.dy.masa.tweakeroo.util.IDecorationEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher
{
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(Entity entityIn, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir)
    {
        boolean isPlayer = (entityIn instanceof Player);

        if (entityIn instanceof HangingEntity)
        {
            if (!RenderTweaks.isPositionValidForRendering(((IDecorationEntity) entityIn).tweakeroo$getAttached()))
            {
                cir.setReturnValue(false);
            }
        }

        if (!isPlayer && Configs.Generic.SELECTIVE_BLOCKS_HIDE_ENTITIES.getBooleanValue())
        {
            if (!RenderTweaks.isPositionValidForRendering(entityIn.blockPosition()))
            {
                cir.setReturnValue(false);
            }
        }

        if (Configs.Disable.DISABLE_ENTITY_RENDERING.getBooleanValue() && (entityIn instanceof Player) == false)
        {
            cir.setReturnValue(false);
        }

        if (entityIn instanceof FallingBlockEntity && Configs.Disable.DISABLE_FALLING_BLOCK_RENDER.getBooleanValue())
        {
            cir.setReturnValue(false);
        }
        else if (entityIn instanceof ArmorStand && Configs.Disable.DISABLE_ARMOR_STAND_RENDERING.getBooleanValue())
        {
            cir.setReturnValue(false);
        }
        else if (entityIn instanceof ExperienceOrb)
        {
            if (FeatureToggle.TWEAK_RENDER_LIMIT_ENTITIES.getBooleanValue())
            {
                int max = Configs.Generic.RENDER_LIMIT_XP_ORB.getIntegerValue();

                if (max >= 0 && ++MiscTweaks.renderCountXPOrbs > max)
                {
                    cir.setReturnValue(false);
                }
            }
        }
        else if (entityIn instanceof ItemEntity)
        {
            if (FeatureToggle.TWEAK_RENDER_LIMIT_ENTITIES.getBooleanValue())
            {
                int max = Configs.Generic.RENDER_LIMIT_ITEM.getIntegerValue();

                if (max >= 0 && ++MiscTweaks.renderCountItems > max)
                {
                    cir.setReturnValue(false);
                }
            }
        }
        else if (Configs.Disable.DISABLE_DEAD_MOB_RENDERING.getBooleanValue() &&
                entityIn instanceof LivingEntity && ((LivingEntity) entityIn).getHealth() <= 0f)
        {
            cir.setReturnValue(false);
        }
    }
}
