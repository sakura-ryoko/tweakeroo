package fi.dy.masa.tweakeroo.tweaks;

import javax.annotation.Nullable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import java.util.*;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.MessageOutputType;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.*;

public class MiscTweaks
{
    public static final EntityRestriction ENTITY_TYPE_ATTACK_RESTRICTION = new EntityRestriction();
    public static final PotionRestriction POTION_RESTRICTION = new PotionRestriction();

    private static KeybindState KEY_STATE_ATTACK;
    private static KeybindState KEY_STATE_USE;

    public static int renderCountItems;
    public static int renderCountXPOrbs;
    private static int potionWarningTimer;

    private static class KeybindState
    {
        private final KeyMapping keybind;
        private final Consumer<Minecraft> clickFunc;
        private boolean state;
        private int durationCounter;
        private int intervalCounter;

        public KeybindState(KeyMapping keybind, Consumer<Minecraft> clickFunc)
        {
            this.keybind = keybind;
            this.clickFunc = clickFunc;
        }

        public void reset()
        {
            this.state = false;
            this.intervalCounter = 0;
            this.durationCounter = 0;
        }

        public void handlePeriodicHold(int interval, int holdDuration, Minecraft mc)
        {
            if (this.state)
            {
                if (++this.durationCounter >= holdDuration)
                {
                    this.setKeyState(false, mc);
                    this.durationCounter = 0;
                }
            }
            else if (++this.intervalCounter >= interval)
            {
                this.setKeyState(true, mc);
                this.intervalCounter = 0;
                this.durationCounter = 0;
            }
        }

        public void handlePeriodicClick(int interval, Minecraft mc)
        {
            if (++this.intervalCounter >= interval)
            {
                this.clickFunc.accept(mc);
                this.intervalCounter = 0;
                this.durationCounter = 0;
            }
        }

        private void setKeyState(boolean state, Minecraft mc)
        {
            this.state = state;

            InputConstants.Key key = InputConstants.getKey(this.keybind.saveString());
            KeyMapping.set(key, state);

            if (state)
            {
                this.clickFunc.accept(mc);
                KeyMapping.click(key);
            }
        }
    }

    public static void onTick(Minecraft mc)
    {
        LocalPlayer player = mc.player;

        if (player == null)
        {
            return;
        }

        doPeriodicClicks(mc);
        doPotionWarnings(player);

        if (FeatureToggle.TWEAK_REPAIR_MODE.getBooleanValue())
        {
            InventoryUtils.repairModeSwapItems(player);
        }

        CameraEntity.movementTick();
    }

    public static void onGameLoop(Minecraft mc)
    {
        PlacementTweaks.onTick(mc);
        RenderTweaks.onTick();
        // Reset the counters after rendering each frame
        renderCountItems = 0;
        renderCountXPOrbs = 0;
    }

    private static void doPeriodicClicks(Minecraft mc)
    {
        if (GuiUtils.getCurrentScreen() == null)
        {
            handlePeriodicClicks(
                    KEY_STATE_ATTACK,
                    FeatureToggle.TWEAK_PERIODIC_HOLD_ATTACK,
                    FeatureToggle.TWEAK_PERIODIC_ATTACK,
                    Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL,
                    Configs.Generic.PERIODIC_HOLD_ATTACK_DURATION,
                    Configs.Generic.PERIODIC_ATTACK_INTERVAL, mc);

            handlePeriodicClicks(
                    KEY_STATE_USE,
                    FeatureToggle.TWEAK_PERIODIC_HOLD_USE,
                    FeatureToggle.TWEAK_PERIODIC_USE,
                    Configs.Generic.PERIODIC_HOLD_USE_INTERVAL,
                    Configs.Generic.PERIODIC_HOLD_USE_DURATION,
                    Configs.Generic.PERIODIC_USE_INTERVAL, mc);
        }
        else
        {
            KEY_STATE_ATTACK.reset();
            KEY_STATE_USE.reset();
        }
    }

    private static void handlePeriodicClicks(
            KeybindState keyState,
            IConfigBoolean cfgPeriodicHold,
            IConfigBoolean cfgPeriodicClick,
            IConfigInteger cfgHoldClickInterval,
            IConfigInteger cfgHoldDuration,
            IConfigInteger cfgClickInterval,
            Minecraft mc)
    {
        if (cfgPeriodicHold.getBooleanValue())
        {
            int interval = cfgHoldClickInterval.getIntegerValue();
            int holdDuration = cfgHoldDuration.getIntegerValue();
            keyState.handlePeriodicHold(interval, holdDuration, mc);
        }
        else if (cfgPeriodicClick.getBooleanValue())
        {
            int interval = cfgClickInterval.getIntegerValue();
            keyState.handlePeriodicClick(interval, mc);
        }
        else
        {
            keyState.reset();
        }
    }

    private static void doPotionWarnings(Player player)
    {
        if (FeatureToggle.TWEAK_POTION_WARNING.getBooleanValue() &&
            ++potionWarningTimer >= 100)
        {
            potionWarningTimer = 0;

            Collection<MobEffectInstance> effects = player.getActiveEffects();

            if (effects.isEmpty() == false)
            {
                int minDuration = -1;
                int count = 0;

                for (MobEffectInstance effectInstance : effects)
                {
                    if (potionWarningShouldInclude(effectInstance))
                    {
                        ++count;

                        if (effectInstance.getDuration() < minDuration || minDuration < 0)
                        {
                            minDuration = effectInstance.getDuration();
                        }
                    }
                }

                if (count > 0)
                {
                    InfoUtils.printActionbarMessage("tweakeroo.message.potion_effects_running_out",
                            Integer.valueOf(count), Integer.valueOf(minDuration / 20));
                }
            }
        }
    }

    public static boolean isEntityAllowedByAttackingRestriction(EntityType<?> type)
    {
        if (MiscTweaks.ENTITY_TYPE_ATTACK_RESTRICTION.isAllowed(type) == false)
        {
            MessageOutputType messageOutputType = (MessageOutputType) Configs.Generic.ENTITY_TYPE_ATTACK_RESTRICTION_WARN.getOptionListValue();

            if (messageOutputType == MessageOutputType.MESSAGE)
            {
                InfoUtils.showGuiOrInGameMessage(Message.MessageType.WARNING, "tweakeroo.message.warning.entity_type_attack_restriction");
            }
            else if (messageOutputType == MessageOutputType.ACTIONBAR)
            {
                InfoUtils.printActionbarMessage("tweakeroo.message.warning.entity_type_attack_restriction");
            }

            return false;
        }

        return true;
    }


    private static boolean potionWarningShouldInclude(MobEffectInstance effect)
    {
        return effect.isAmbient() == false &&
               (effect.getEffect().value().isBeneficial() ||
               Configs.Generic.POTION_WARNING_BENEFICIAL_ONLY.getBooleanValue() == false) &&
               effect.getDuration() <= Configs.Generic.POTION_WARNING_THRESHOLD.getIntegerValue() &&
               effect.getDuration() >= 0 &&
               POTION_RESTRICTION.isAllowed(effect.getEffect().value());
    }

    public static @NotNull List<FlatLayerInfo> parseBlockString(String blockString)
    {
        List<FlatLayerInfo> list = new ArrayList<>();
        String[] strings = blockString.split(",");
        final int count = strings.length;
        int thicknessSum = 0;

        for (int i = 0; i < count; ++i)
        {
            String str = strings[i];
            FlatLayerInfo layer = parseLayerString(str, thicknessSum);

            if (layer == null)
            {
                list = Collections.emptyList();
                break;
            }

            list.add(layer);
            thicknessSum += layer.getHeight();
        }

        return list;
    }

    @Nullable
    private static FlatLayerInfo parseLayerString(String string, int startY)
    {
        String[] strings = string.split("\\*", 2);
        int thickness;

        if (strings.length == 2)
        {
            try
            {
                thickness = Math.max(Integer.parseInt(strings[0]), 0);
            }
            catch (NumberFormatException e)
            {
                Tweakeroo.LOGGER.error("Error while parsing flat world string => {}", e.getMessage());
                return null;
            }
        }
        else
        {
            thickness = 1;
        }

        int endY = Math.min(startY + thickness, 256);
        int finalThickness = endY - startY;
        Block block;

        try
        {
            block = getBlockFromName(strings[strings.length - 1]);
        }
        catch (Exception e)
        {
            Tweakeroo.LOGGER.error("Error while parsing flat world string => {}", e.getMessage());
            return null;
        }

        if (block == null)
        {
            Tweakeroo.LOGGER.error("Error while parsing flat world string => Unknown block, {}", strings[strings.length - 1]);
            return null;
        }
        else
        {
            // FIXME 1.17 is this just not needed anymore?
            //layer.setStartY(startY);
            return new FlatLayerInfo(finalThickness, block);
        }
    }

    @Nullable
    private static Block getBlockFromName(String name)
    {
        try
        {
	        Identifier id = Identifier.tryParse(name);

			if (id != null)
			{
				Optional<Holder.Reference<Block>> opt = BuiltInRegistries.BLOCK.get(id);

				if (opt.isPresent())
				{
					return opt.get().value();
				}
			}

            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static void setKeybindStates()
    {
        Options opts = Minecraft.getInstance().options;

	    KEY_STATE_ATTACK = new KeybindState(opts.keyAttack, (mc) -> ((IMinecraftClientInvoker) mc).tweakeroo_invokeDoAttack());
	    KEY_STATE_USE = new KeybindState(opts.keyUse, (mc) -> ((IMinecraftClientInvoker) mc).tweakeroo_invokeDoItemUse());
    }

    public static void onVanillaGammaChange(Double previous, Double newValue)
    {
        Tweakeroo.debugLog("onVanillaGammaChange: [{}] -> [{}]", previous, newValue);

        if (FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue())
        {
            FeatureToggle.TWEAK_GAMMA_OVERRIDE.setBooleanValue(false);
            MiscUtils.toggleGammaOverrideWithMessage();
        }
    }

    /**
     * I don't like this.  Let's just keep using the ugly Vanilla value change warnings instead.
     */
    @ApiStatus.Experimental
    public enum GammaOverrideValue implements OptionInstance.SliderableValueSet<Double>
    {
        INSTANCE;

        @Override
        public double toSliderValue(Double value)
        {
            return value;
        }

        @Override
        public Double fromSliderValue(double value)
        {
            return value;
        }

        @Override
        public @NonNull Optional<Double> validateValue(Double value)
        {
            return value >= 0.0 && value <= 32.0 ? Optional.of(value) : Optional.empty();
        }

        @Override
        public @NonNull Codec<Double> codec()
        {
            return Codec.withAlternative(Codec.doubleRange(0.0, 32.0), Codec.BOOL, boolean_ -> boolean_ ? 32.0 : 0.0);
        }
    }
}
