package fi.dy.masa.tweakeroo.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import fi.dy.masa.malilib.render.InventoryOverlayScreen;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.data.CachedTagManager;
import fi.dy.masa.tweakeroo.data.CameraPresetManager;
import fi.dy.masa.tweakeroo.data.EntityDataManager;
import fi.dy.masa.tweakeroo.gui.GuiCameraPresetEditor;
import fi.dy.masa.tweakeroo.gui.GuiConfigs;
import fi.dy.masa.tweakeroo.mixin.block.IMixinAbstractBlock;
import fi.dy.masa.tweakeroo.mixin.option.IMixinSimpleOption;
import fi.dy.masa.tweakeroo.renderer.InventoryOverlayHandler;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import fi.dy.masa.tweakeroo.util.*;

public class Callbacks
{
    public static boolean skipWorldRendering;

    public static void init(Minecraft mc)
    {
//		FeatureToggle.TWEAK_F3_CURSOR.setValueChangeCallback(new FeatureCallbackF3Toggle(FeatureToggle.TWEAK_F3_CURSOR, mc));
        FeatureToggle.TWEAK_GAMMA_OVERRIDE.setValueChangeCallback(new FeatureCallbackGamma(FeatureToggle.TWEAK_GAMMA_OVERRIDE, mc));
        FeatureToggle.TWEAK_DARKNESS_VISIBILITY.setValueChangeCallback(new FeatureCallbackDarkness(FeatureToggle.TWEAK_DARKNESS_VISIBILITY, mc));
        Configs.Disable.DISABLE_SLIME_BLOCK_SLOWDOWN.setValueChangeCallback(new FeatureCallbackSlime(Configs.Disable.DISABLE_SLIME_BLOCK_SLOWDOWN));

        FeatureToggle.TWEAK_FAST_BLOCK_PLACEMENT.getKeybind().setCallback(new KeyCallbackToggleFastMode(FeatureToggle.TWEAK_FAST_BLOCK_PLACEMENT));
        FeatureToggle.TWEAK_FAST_BLOCK_PLACEMENT.setValueChangeCallback((cfg) ->
                                                                        {
                                                                            if (Configs.Generic.PLACEMENT_RESTRICTION_TIED_TO_FAST.getBooleanValue())
                                                                            {
                                                                                FeatureToggle.TWEAK_PLACEMENT_RESTRICTION.setBooleanValue(cfg.getBooleanValue());
                                                                            }
                                                                        });
        FeatureToggle.TWEAK_FREE_CAMERA.setValueChangeCallback((cfg) -> CameraEntity.setCameraState(cfg.getBooleanValue(), null));
        FeatureToggle.TWEAK_HOLD_ATTACK.setValueChangeCallback(new FeatureCallbackHold(mc.options.keyAttack));
        FeatureToggle.TWEAK_HOLD_USE.setValueChangeCallback(new FeatureCallbackHold(mc.options.keyUse));
        Configs.Generic.ENTITY_DATA_SYNC.setValueChangeCallback((config) -> EntityDataManager.getInstance().onEntityDataSyncToggled(config));

        IHotkeyCallback callbackGeneric = new KeyCallbackHotkeysGeneric(mc);
        IHotkeyCallback callbackMessage = new KeyCallbackHotkeyWithMessage(mc);
		IHotkeyCallback callbackFreeCamPresets = new KeyCallbackFreeCameraPresets(mc);

        Hotkeys.BREAKING_RESTRICTION_MODE_COLUMN.getKeybind().setCallback(callbackGeneric);
        Hotkeys.BREAKING_RESTRICTION_MODE_DIAGONAL.getKeybind().setCallback(callbackGeneric);
        Hotkeys.BREAKING_RESTRICTION_MODE_FACE.getKeybind().setCallback(callbackGeneric);
        Hotkeys.BREAKING_RESTRICTION_MODE_LAYER.getKeybind().setCallback(callbackGeneric);
        Hotkeys.BREAKING_RESTRICTION_MODE_LINE.getKeybind().setCallback(callbackGeneric);
        Hotkeys.BREAKING_RESTRICTION_MODE_PLANE.getKeybind().setCallback(callbackGeneric);
        Hotkeys.COPY_SIGN_TEXT.getKeybind().setCallback(callbackGeneric);
        Hotkeys.FLY_PRESET_1.getKeybind().setCallback(callbackGeneric);
        Hotkeys.FLY_PRESET_2.getKeybind().setCallback(callbackGeneric);
        Hotkeys.FLY_PRESET_3.getKeybind().setCallback(callbackGeneric);
        Hotkeys.FLY_PRESET_4.getKeybind().setCallback(callbackGeneric);
        Hotkeys.FLY_INCREMENT_1.getKeybind().setCallback(callbackGeneric);
        Hotkeys.FLY_INCREMENT_2.getKeybind().setCallback(callbackGeneric);
        Hotkeys.FREE_CAMERA_PLAYER_INPUTS.getKeybind().setCallback((action, key) ->
                                                                   {
                                                                       IConfigBoolean config = Configs.Generic.FREE_CAMERA_PLAYER_INPUTS;
                                                                       config.toggleBooleanValue();
                                                                       InfoUtils.printBooleanConfigToggleMessage(config.getPrettyName(), config.getBooleanValue());
                                                                       return true;
                                                                   });
        Hotkeys.FREE_CAMERA_PLAYER_MOVEMENT.getKeybind().setCallback((action, key) ->
                                                                     {
                                                                         IConfigBoolean config = Configs.Generic.FREE_CAMERA_PLAYER_MOVEMENT;
                                                                         config.toggleBooleanValue();
                                                                         InfoUtils.printBooleanConfigToggleMessage(config.getPrettyName(), config.getBooleanValue());
                                                                         return true;
                                                                     });
		Hotkeys.FREE_CAMERA_PRESET_ADD.getKeybind().setCallback(callbackFreeCamPresets);
	    Hotkeys.FREE_CAMERA_PRESET_CYCLE.getKeybind().setCallback(callbackFreeCamPresets);
	    Hotkeys.FREE_CAMERA_PRESET_DELETE.getKeybind().setCallback(callbackFreeCamPresets);
	    Hotkeys.FREE_CAMERA_PRESET_DELETE_ALL.getKeybind().setCallback(callbackFreeCamPresets);
        Hotkeys.HOTBAR_SWAP_1.getKeybind().setCallback(callbackGeneric);
        Hotkeys.HOTBAR_SWAP_2.getKeybind().setCallback(callbackGeneric);
        Hotkeys.HOTBAR_SWAP_3.getKeybind().setCallback(callbackGeneric);
        Hotkeys.HOTBAR_SCROLL.getKeybind().setCallback(callbackGeneric);
        Hotkeys.INVENTORY_PREVIEW_TOGGLE_SCREEN.getKeybind().setCallback(callbackGeneric);
        Hotkeys.OPEN_CONFIG_GUI.getKeybind().setCallback(callbackGeneric);
	    Hotkeys.OPEN_CAMERA_PRESET_EDITOR_GUI.getKeybind().setCallback(callbackGeneric);
        Hotkeys.PLACEMENT_RESTRICTION_MODE_COLUMN.getKeybind().setCallback(callbackGeneric);
        Hotkeys.PLACEMENT_RESTRICTION_MODE_DIAGONAL.getKeybind().setCallback(callbackGeneric);
        Hotkeys.PLACEMENT_RESTRICTION_MODE_FACE.getKeybind().setCallback(callbackGeneric);
        Hotkeys.PLACEMENT_RESTRICTION_MODE_LAYER.getKeybind().setCallback(callbackGeneric);
        Hotkeys.PLACEMENT_RESTRICTION_MODE_LINE.getKeybind().setCallback(callbackGeneric);
        Hotkeys.PLACEMENT_RESTRICTION_MODE_PLANE.getKeybind().setCallback(callbackGeneric);
        Hotkeys.SIT_DOWN_NEARBY_PETS.getKeybind().setCallback((a, k) -> MiscUtils.commandNearbyPets(true));
        Hotkeys.STAND_UP_NEARBY_PETS.getKeybind().setCallback((a, k) -> MiscUtils.commandNearbyPets(false));
        Hotkeys.SWAP_ELYTRA_CHESTPLATE.getKeybind().setCallback(callbackGeneric);
	    Hotkeys.SWAP_SPYGLASS_AND_ACTIVATE.getKeybind().setCallback(callbackGeneric);
        Hotkeys.TOGGLE_AP_PROTOCOL.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(Configs.Generic.ACCURATE_PLACEMENT_PROTOCOL));
        Hotkeys.TOGGLE_GRAB_CURSOR.getKeybind().setCallback(callbackGeneric);
        Hotkeys.TOOL_PICK.getKeybind().setCallback(callbackGeneric);
        Hotkeys.WRITE_MAPS_AS_IMAGES.getKeybind().setCallback((a, k) -> MiscUtils.writeAllMapsAsImages());
        Hotkeys.ZOOM_ACTIVATE.getKeybind().setCallback(callbackGeneric);
        Hotkeys.AREA_SELECTION_ADD_TO_LIST.getKeybind().setCallback(callbackGeneric);
        Hotkeys.AREA_SELECTION_REMOVE_FROM_LIST.getKeybind().setCallback(callbackGeneric);

        Hotkeys.SKIP_ALL_RENDERING.getKeybind().setCallback(callbackMessage);
        Hotkeys.SKIP_WORLD_RENDERING.getKeybind().setCallback(callbackMessage);

        Configs.Generic.TOOL_SWITCHABLE_SLOTS.setValueChangeCallback((cfg) -> InventoryUtils.setToolSwitchableSlots(cfg.getStringValue()));
        // TODO 1.19.3+
        //Configs.Lists.CREATIVE_EXTRA_ITEMS.setValueChangeCallback((cfg) -> CreativeExtraItems.setCreativeExtraItems(cfg.getStrings()));

        FeatureToggle.TWEAK_AFTER_CLICKER.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_AFTER_CLICKER));
        FeatureToggle.TWEAK_BREAKING_GRID.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_BREAKING_GRID));
        FeatureToggle.TWEAK_FLY_SPEED.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_FLY_SPEED));
        FeatureToggle.TWEAK_HOTBAR_SLOT_CYCLE.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_HOTBAR_SLOT_CYCLE));
        FeatureToggle.TWEAK_HOTBAR_SLOT_RANDOMIZER.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_HOTBAR_SLOT_RANDOMIZER));
        FeatureToggle.TWEAK_PLACEMENT_GRID.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_PLACEMENT_GRID));
        FeatureToggle.TWEAK_PLACEMENT_LIMIT.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_PLACEMENT_LIMIT));
        FeatureToggle.TWEAK_SNAP_AIM.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_SNAP_AIM));
        FeatureToggle.TWEAK_ZOOM.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_ZOOM));

//        FeatureToggle.TWEAK_PERIODIC_ATTACK.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_PERIODIC_ATTACK));
//        FeatureToggle.TWEAK_PERIODIC_USE.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_PERIODIC_USE));
//        FeatureToggle.TWEAK_PERIODIC_HOLD_ATTACK.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_PERIODIC_HOLD_ATTACK));
//        FeatureToggle.TWEAK_PERIODIC_HOLD_USE.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.TWEAK_PERIODIC_HOLD_USE));

        Configs.Lists.SELECTIVE_BLOCKS_BLACKLIST.setValueChangeCallback((cfg) -> RenderTweaks.rebuildLists());
        Configs.Lists.SELECTIVE_BLOCKS_WHITELIST.setValueChangeCallback((cfg) -> RenderTweaks.rebuildLists());
        Configs.Lists.SELECTIVE_BLOCKS_LIST_TYPE.setValueChangeCallback((cfg) -> RenderTweaks.rebuildLists());
        FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDERING.setValueChangeCallback((cfg) -> RenderTweaks.rebuildLists());
        Configs.Disable.DISABLE_RENDERING_SCAFFOLDING.setValueChangeCallback((cfg) -> mc.levelRenderer.allChanged());
        Configs.Generic.TOOL_SWAP_SILK_TOUCH_OVERRIDE.setValueChangeCallback(
                (cfg) ->
                        CachedTagManager.parseSilkTouchOverride(Configs.Lists.SILK_TOUCH_OVERRIDE.getStrings())
        );
	    Configs.Generic.TOOL_SWAP_PICKAXE_OVERRIDE.setValueChangeCallback(
			    (cfg) ->
					    CachedTagManager.parsePickaxeOverride(Configs.Lists.PICKAXE_OVERRIDE.getStrings())
	    );
        Configs.Disable.DISABLE_TICKRATE_PLAYER_SLOWDOWN.setValueChangeCallback(new TickRateYeetToggle());
    }

//	public static class FeatureCallbackF3Toggle implements IValueChangeCallback<IConfigBoolean>
//	{
//		private final Minecraft mc;
//
//		public FeatureCallbackF3Toggle(FeatureToggle feature, Minecraft mc)
//		{
//			this.mc = mc;
//			this.applyValue(mc.debugEntries, feature.getBooleanValue());
//		}
//
//		@Override
//		public void onValueChanged(IConfigBoolean config)
//		{
//			this.applyValue(this.mc.debugEntries, config.getBooleanValue());
//		}
//
//		private void applyValue(DebugScreenEntryList profile, boolean enable)
//		{
//			if (enable &&
//					profile.getStatus(DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR) != DebugScreenEntryStatus.ALWAYS_ON)
//			{
//				profile.setStatus(DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR, DebugScreenEntryStatus.ALWAYS_ON);
//			}
//		}
//	}

    public static class FeatureCallbackHold implements IValueChangeCallback<IConfigBoolean>
    {
        private final KeyMapping keyBind;

        public FeatureCallbackHold(KeyMapping keyBind)
        {
            this.keyBind = keyBind;
        }

        @Override
        public void onValueChanged(IConfigBoolean config)
        {
            if (config.getBooleanValue())
            {
                KeyMapping.set(InputConstants.getKey(this.keyBind.saveString()), true);
                KeyMapping.click(InputConstants.getKey(this.keyBind.saveString()));
            }
            else
            {
                KeyMapping.set(InputConstants.getKey(this.keyBind.saveString()), false);
            }
        }
    }

    public static class FeatureCallbackGamma implements IValueChangeCallback<IConfigBoolean>
    {
        private final Minecraft mc;

        public FeatureCallbackGamma(FeatureToggle feature, Minecraft mc)
        {
            this.mc = mc;
            double gamma = this.mc.options.gamma().get();

            if (gamma <= 1.0F)
            {
                Configs.Internal.GAMMA_VALUE_ORIGINAL.setDoubleValue(gamma);
            }

            // If the feature is enabled on game launch, apply it here
            if (feature.getBooleanValue())
            {
                this.applyValue(Configs.Generic.GAMMA_OVERRIDE_VALUE.getDoubleValue());
            }
        }

        @Override
        public void onValueChanged(IConfigBoolean config)
        {
            double gamma;

            if (config.getBooleanValue())
            {
				if (this.mc.player != null && this.mc.player.hasEffect(MobEffects.NIGHT_VISION))
				{
					// Warn Only; Do not change because the
					// Night Vision status persists while it's being removed.
					InfoUtils.showInGameMessage(Message.MessageType.WARNING, "tweakeroo.message.gamma_override.night_vision_warn");
				}

	            Configs.Internal.GAMMA_VALUE_ORIGINAL.setDoubleValue(this.mc.options.gamma().get());
                gamma = Configs.Generic.GAMMA_OVERRIDE_VALUE.getDoubleValue();
            }
            else
            {
                gamma = Configs.Internal.GAMMA_VALUE_ORIGINAL.getDoubleValue();
            }

            this.applyValue(gamma);
        }

        private void applyValue(double gamma)
        {
            @SuppressWarnings("unchecked")
            IMixinSimpleOption<Double> opt = (IMixinSimpleOption<Double>) (Object) this.mc.options.gamma();

            if (opt != null)
            {
                opt.tweakeroo_setValueWithoutCheck(gamma);
            }
        }
    }

    public static class FeatureCallbackDarkness implements IValueChangeCallback<IConfigBoolean>
    {
        private final Minecraft mc;

        public FeatureCallbackDarkness(FeatureToggle feature, Minecraft mc)
        {
            this.mc = mc;
            double darkness = this.mc.options.darknessEffectScale().get();

            if (darkness <= 1.0F)
            {
                Configs.Internal.DARKNESS_SCALE_VALUE_ORIGINAL.setDoubleValue(darkness);
            }

            // If the feature is enabled on game launch, apply it here
            if (feature.getBooleanValue())
            {
                this.applyValue(Configs.Generic.DARKNESS_SCALE_OVERRIDE_VALUE.getDoubleValue());
            }
        }

        @Override
        public void onValueChanged(IConfigBoolean config)
        {
            double darkness;

            if (config.getBooleanValue())
            {
                Configs.Internal.DARKNESS_SCALE_VALUE_ORIGINAL.setDoubleValue(this.mc.options.darknessEffectScale().get());
                darkness = Configs.Generic.DARKNESS_SCALE_OVERRIDE_VALUE.getDoubleValue();
            }
            else
            {
                darkness = Configs.Internal.DARKNESS_SCALE_VALUE_ORIGINAL.getDoubleValue();
            }

            this.applyValue(darkness);
        }

        private void applyValue(double darkness)
        {
            @SuppressWarnings("unchecked")
            IMixinSimpleOption<Double> opt = (IMixinSimpleOption<Double>) (Object) this.mc.options.darknessEffectScale();

            if (opt != null)
            {
                opt.tweakeroo_setValueWithoutCheck(darkness);
            }
        }
    }

    public static class FeatureCallbackSlime implements IValueChangeCallback<ConfigBoolean>
    {
        public FeatureCallbackSlime(ConfigBoolean feature)
        {
            Configs.Internal.SLIME_BLOCK_SLIPPERINESS_ORIGINAL.setDoubleValue(Blocks.SLIME_BLOCK.getFriction());

            // If the feature is enabled on game launch, apply the overridden value here
            if (feature.getBooleanValue())
            {
                ((IMixinAbstractBlock) Blocks.SLIME_BLOCK).setFriction(Blocks.STONE.getFriction());
            }
        }

        @Override
        public void onValueChanged(ConfigBoolean config)
        {
            if (config.getBooleanValue())
            {
                ((IMixinAbstractBlock) Blocks.SLIME_BLOCK).setFriction(Blocks.STONE.getFriction());
            }
            else
            {
                ((IMixinAbstractBlock) Blocks.SLIME_BLOCK).setFriction((float) Configs.Internal.SLIME_BLOCK_SLIPPERINESS_ORIGINAL.getDoubleValue());
            }
        }
    }

    public static class TickRateYeetToggle implements IValueChangeCallback<ConfigBoolean>
    {
        @Override
        public void onValueChanged(ConfigBoolean config)
        {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level == null) return;

            TickRateManager manager = level.tickRateManager();

            if (config.getBooleanValue())
			{
                if (manager.tickrate() < MiscUtils.DEFAULT_TICK_RATE)
                {
	                manager.setTickRate(MiscUtils.DEFAULT_TICK_RATE);
                }
            }
			else
			{
				final float tickRate = Math.clamp(MiscUtils.getRealTickRate(), MiscUtils.MIN_TICK_RATE, MiscUtils.MAX_TICK_RATE);

//				Tweakeroo.debugLog("[Debug] TickRateYeetToggle: Tick rate: {}", tickRate);
                manager.setTickRate(tickRate);
            }
        }
    }

    public static class KeyCallbackHotkeyWithMessage implements IHotkeyCallback
    {
        private final Minecraft mc;

        public KeyCallbackHotkeyWithMessage(Minecraft mc)
        {
            this.mc = mc;
        }

        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key)
        {
            if (key == Hotkeys.SKIP_ALL_RENDERING.getKeybind())
            {
                this.mc.noRender = !this.mc.noRender;

                String pre = mc.noRender ? GuiBase.TXT_GREEN : GuiBase.TXT_RED;
                String status = StringUtils.translate("tweakeroo.message.value." + (this.mc.noRender ? "on" : "off"));
                String message = StringUtils.translate("tweakeroo.message.toggled", "Skip All Rendering", pre + status + GuiBase.TXT_RST);
                InfoUtils.printActionbarMessage(message);
            }
            else if (key == Hotkeys.SKIP_WORLD_RENDERING.getKeybind())
            {
                skipWorldRendering = !skipWorldRendering;

                boolean enabled = skipWorldRendering;
                String pre = enabled ? GuiBase.TXT_GREEN : GuiBase.TXT_RED;
                String status = StringUtils.translate("tweakeroo.message.value." + (enabled ? "on" : "off"));
                String message = StringUtils.translate("tweakeroo.message.toggled", "Skip World Rendering", pre + status + GuiBase.TXT_RST);
                InfoUtils.printActionbarMessage(message);
            }

            return true;
        }
    }

	private static class KeyCallbackFreeCameraPresets implements IHotkeyCallback
	{
		private final String PREFIX = Reference.MOD_ID+".message.free_cam.preset";
		private final Minecraft mc;

		public KeyCallbackFreeCameraPresets(Minecraft mc)
		{
			this.mc = mc;
		}

		@Override
		public boolean onKeyAction(KeyAction action, IKeybind key)
		{
			if (this.mc.player == null || this.mc.level == null || this.mc.getCameraEntity() == null)
			{
				return false;
			}

			ResourceKey<Level> dimKey = this.mc.level.dimension();
			Entity camera = this.mc.getCameraEntity();

			if (key == Hotkeys.FREE_CAMERA_PRESET_ADD.getKeybind())
			{
				final int id = CameraPresetManager.getInstance().getNextId(-1);
				String name = "Preset "+id;
				CameraPreset newPreset = new CameraPreset(id, name, dimKey.identifier(), camera.position(), camera.getYRot(), camera.getXRot());

				if (CameraUtils.addPreset(newPreset))
				{
					InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_added", newPreset.toShortString()));
				}
				else
				{
					InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_already_in_use"));
				}

				return true;
			}
			else if (key == Hotkeys.FREE_CAMERA_PRESET_DELETE.getKeybind())
			{
				CameraPreset preset = CameraPresetManager.getInstance().getAtPosition(camera);

				if (CameraUtils.deletePreset(preset))
				{
					InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_deleted", preset.toShortString()));
				}
				else
				{
					InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_not_found", String.format("%02d", 0)));
				}

				return true;
			}
			else if (key == Hotkeys.FREE_CAMERA_PRESET_DELETE_ALL.getKeybind())
			{
				if (CameraUtils.deleteAllPresets(dimKey))
				{
					InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_deleted_all_dim", dimKey.identifier().toString()));
				}

				return true;
			}
			else if (key == Hotkeys.FREE_CAMERA_PRESET_CYCLE.getKeybind())
			{
				CameraPreset preset = CameraPresetManager.getInstance().cycle(dimKey);

				if (preset != null && this.mc.level != null)
				{
					if (this.mc.level.dimension().identifier().equals(preset.getDim()))
					{
						if (CameraUtils.recallPreset(preset, this.mc))
						{
							InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX + "_recalled", FeatureToggle.TWEAK_FREE_CAMERA.getPrettyName(), String.format("%02d", preset.getId()), preset.getName()));
						}
						else
						{
							InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX + "_matches_camera", String.format("%02d", preset.getId())));
						}
					}
					else
					{
						InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX + "_wrong_dimension", String.format("%02d", preset.getId()), preset.getName()));
					}
				}
				else
				{
					InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_cycle_not_found"));
				}

				return true;
			}

			return false;
		}
	}

    private static class KeyCallbackHotkeysGeneric implements IHotkeyCallback
    {
        private final Minecraft mc;

        public KeyCallbackHotkeysGeneric(Minecraft mc)
        {
            this.mc = mc;
        }

        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key)
        {
            if (key == Hotkeys.AREA_SELECTION_ADD_TO_LIST.getKeybind())
            {
                RenderTweaks.addSelectionToList();
                return true;
            }
            else if (key == Hotkeys.AREA_SELECTION_REMOVE_FROM_LIST.getKeybind())
            {
                RenderTweaks.removeSelectionFromList();
                return true;
            }
            else if (key == Hotkeys.TOOL_PICK.getKeybind())
            {
                if (this.mc.hitResult != null && this.mc.hitResult.getType() == HitResult.Type.BLOCK)
                {
                    InventoryUtils.trySwitchToEffectiveTool(((BlockHitResult) this.mc.hitResult).getBlockPos());
                    return true;
                }
            }
            else if (key == Hotkeys.COPY_SIGN_TEXT.getKeybind())
            {
                HitResult trace = this.mc.hitResult;

                if (trace != null && trace.getType() == HitResult.Type.BLOCK &&
                    this.mc.level != null)
                {
                    BlockPos pos = ((BlockHitResult) trace).getBlockPos();
                    BlockEntity te = this.mc.level.getBlockEntity(pos);

                    if (te instanceof SignBlockEntity && this.mc.player != null)
                    {
                        MiscUtils.copyTextFromSign((SignBlockEntity) te, ((SignBlockEntity) te).isFacingFrontText(this.mc.player));
                        InfoUtils.printActionbarMessage("tweakeroo.message.sign_text_copied");
                    }
                }

                return true;
            }
            else if (key == Hotkeys.HOTBAR_SWAP_1.getKeybind())
            {
                if (FeatureToggle.TWEAK_HOTBAR_SWAP.getBooleanValue() && this.mc.player != null)
                {
                    InventoryUtils.swapHotbarWithInventoryRow(this.mc.player, 0);
                    return true;
                }
            }
            else if (key == Hotkeys.HOTBAR_SWAP_2.getKeybind())
            {
                if (FeatureToggle.TWEAK_HOTBAR_SWAP.getBooleanValue() && this.mc.player != null)
                {
                    InventoryUtils.swapHotbarWithInventoryRow(this.mc.player, 1);
                    return true;
                }
            }
            else if (key == Hotkeys.HOTBAR_SWAP_3.getKeybind())
            {
                if (FeatureToggle.TWEAK_HOTBAR_SWAP.getBooleanValue() && this.mc.player != null)
                {
                    InventoryUtils.swapHotbarWithInventoryRow(this.mc.player, 2);
                    return true;
                }
            }
            else if (key == Hotkeys.FLY_PRESET_1.getKeybind())
            {
                this.setFlySpeedPreset(0);
                return true;
            }
            else if (key == Hotkeys.FLY_PRESET_2.getKeybind())
            {
                this.setFlySpeedPreset(1);
                return true;
            }
            else if (key == Hotkeys.FLY_PRESET_3.getKeybind())
            {
                this.setFlySpeedPreset(2);
                return true;
            }
            else if (key == Hotkeys.FLY_PRESET_4.getKeybind())
            {
                this.setFlySpeedPreset(3);
                return true;
            }
            else if (key == Hotkeys.FLY_INCREMENT_1.getKeybind())
            {
                ConfigDouble config = Configs.getActiveFlySpeedConfig();
                double newValue = config.getDoubleValue() + (Configs.Generic.FLY_SPEED_INCREMENT_1.getDoubleValue());
                config.setDoubleValue(newValue);

                String strIndex = GuiBase.TXT_GREEN + (Configs.Internal.FLY_SPEED_PRESET.getIntegerValue() + 1) + GuiBase.TXT_RST;
                String strValue = GuiBase.TXT_GREEN + String.format("%.3f", config.getDoubleValue()) + GuiBase.TXT_RST;
                InfoUtils.printActionbarMessage("tweakeroo.message.set_fly_speed_to", strIndex, strValue);
                return true;
            }
            else if (key == Hotkeys.FLY_INCREMENT_2.getKeybind())
            {
                ConfigDouble config = Configs.getActiveFlySpeedConfig();
                double newValue = config.getDoubleValue() + (Configs.Generic.FLY_SPEED_INCREMENT_2.getDoubleValue());
                config.setDoubleValue(newValue);

                String strIndex = GuiBase.TXT_GREEN + (Configs.Internal.FLY_SPEED_PRESET.getIntegerValue() + 1) + GuiBase.TXT_RST;
                String strValue = GuiBase.TXT_GREEN + String.format("%.3f", config.getDoubleValue()) + GuiBase.TXT_RST;
                InfoUtils.printActionbarMessage("tweakeroo.message.set_fly_speed_to", strIndex, strValue);
                return true;
            }
            else if (key == Hotkeys.HOTBAR_SCROLL.getKeybind())
            {
                if (FeatureToggle.TWEAK_HOTBAR_SCROLL.getBooleanValue() && this.mc.player != null)
                {
                    int currentRow = Configs.Internal.HOTBAR_SCROLL_CURRENT_ROW.getIntegerValue();
                    InventoryUtils.swapHotbarWithInventoryRow(this.mc.player, currentRow);
                    return true;
                }
            }
            else if (key == Hotkeys.BREAKING_RESTRICTION_MODE_COLUMN.getKeybind())
            {
                this.setBreakingRestrictionMode(PlacementRestrictionMode.COLUMN);
                return true;
            }
            else if (key == Hotkeys.BREAKING_RESTRICTION_MODE_DIAGONAL.getKeybind())
            {
                this.setBreakingRestrictionMode(PlacementRestrictionMode.DIAGONAL);
                return true;
            }
            else if (key == Hotkeys.BREAKING_RESTRICTION_MODE_FACE.getKeybind())
            {
                this.setBreakingRestrictionMode(PlacementRestrictionMode.FACE);
                return true;
            }
            else if (key == Hotkeys.BREAKING_RESTRICTION_MODE_LAYER.getKeybind())
            {
                this.setBreakingRestrictionMode(PlacementRestrictionMode.LAYER);
                return true;
            }
            else if (key == Hotkeys.BREAKING_RESTRICTION_MODE_LINE.getKeybind())
            {
                this.setBreakingRestrictionMode(PlacementRestrictionMode.LINE);
                return true;
            }
            else if (key == Hotkeys.BREAKING_RESTRICTION_MODE_PLANE.getKeybind())
            {
                this.setBreakingRestrictionMode(PlacementRestrictionMode.PLANE);
                return true;
            }
            else if (key == Hotkeys.PLACEMENT_RESTRICTION_MODE_COLUMN.getKeybind())
            {
                this.setPlacementRestrictionMode(PlacementRestrictionMode.COLUMN);
                return true;
            }
            else if (key == Hotkeys.PLACEMENT_RESTRICTION_MODE_DIAGONAL.getKeybind())
            {
                this.setPlacementRestrictionMode(PlacementRestrictionMode.DIAGONAL);
                return true;
            }
            else if (key == Hotkeys.PLACEMENT_RESTRICTION_MODE_FACE.getKeybind())
            {
                this.setPlacementRestrictionMode(PlacementRestrictionMode.FACE);
                return true;
            }
            else if (key == Hotkeys.PLACEMENT_RESTRICTION_MODE_LAYER.getKeybind())
            {
                this.setPlacementRestrictionMode(PlacementRestrictionMode.LAYER);
                return true;
            }
            else if (key == Hotkeys.PLACEMENT_RESTRICTION_MODE_LINE.getKeybind())
            {
                this.setPlacementRestrictionMode(PlacementRestrictionMode.LINE);
                return true;
            }
            else if (key == Hotkeys.PLACEMENT_RESTRICTION_MODE_PLANE.getKeybind())
            {
                this.setPlacementRestrictionMode(PlacementRestrictionMode.PLANE);
                return true;
            }
            else if (key == Hotkeys.OPEN_CONFIG_GUI.getKeybind())
            {
                GuiBase.openGui(new GuiConfigs());
                return true;
            }
            else if (key == Hotkeys.OPEN_CAMERA_PRESET_EDITOR_GUI.getKeybind())
            {
	            GuiBase.openGui(new GuiCameraPresetEditor());
	            return true;
            }
            else if (key == Hotkeys.SWAP_ELYTRA_CHESTPLATE.getKeybind())
            {
                InventoryUtils.swapElytraAndChestPlate(this.mc.player);
                return true;
            }
            else if (key == Hotkeys.TOGGLE_GRAB_CURSOR.getKeybind())
            {
                if (this.mc.isWindowActive())
                {
                    if (this.mc.mouseHandler.isMouseGrabbed())
                    {
                        this.mc.mouseHandler.releaseMouse();
                        InfoUtils.printActionbarMessage("tweakeroo.message.unfocusing_game");
                    }
                    else
                    {
                        this.mc.mouseHandler.grabMouse();
                        InfoUtils.printActionbarMessage("tweakeroo.message.focusing_game");
                    }
                }
            }
            else if (key == Hotkeys.ZOOM_ACTIVATE.getKeybind())
            {
                if (action == KeyAction.PRESS)
                {
                    //InfoUtils.printActionbarMessage("tweakeroo.message.toggled_zoom_activate_on",
                    //String.format("%s%.1f%s", GuiBase.TXT_GREEN, Configs.Generic.ZOOM_FOV.getDoubleValue(), GuiBase.TXT_RST));
                    MiscUtils.onZoomActivated();
                }
                else
                {
                    MiscUtils.onZoomDeactivated();
                    //InfoUtils.printActionbarMessage("tweakeroo.message.toggled_zoom_activate_off",
                    //String.format("%s%.1f%s", GuiBase.TXT_GREEN, Configs.Generic.ZOOM_FOV.getDoubleValue(), GuiBase.TXT_RST));
                }
            }
            else if (key == Hotkeys.SWAP_SPYGLASS_AND_ACTIVATE.getKeybind() &&
		             FeatureToggle.TWEAK_SPYGLASS_USES_TWEAK_ZOOM.getBooleanValue())
            {
	            if (action == KeyAction.PRESS)
	            {
//		            InfoUtils.printActionbarMessage("tweakeroo.message.toggled_zoom_activate_on",
//		            String.format("%s%.1f%s", GuiBase.TXT_GREEN, Configs.Generic.ZOOM_FOV.getDoubleValue(), GuiBase.TXT_RST));
		            InventoryUtils.swapSpyglassToHand();
					MiscUtils.onSpyglassZoomActivated();
	            }
	            else
	            {
		            MiscUtils.onSpyglassZoomDeactivated();
		            InventoryUtils.returnSpyglassToInventory();
//		            InfoUtils.printActionbarMessage("tweakeroo.message.toggled_zoom_activate_off",
//		            String.format("%s%.1f%s", GuiBase.TXT_GREEN, Configs.Generic.ZOOM_FOV.getDoubleValue(), GuiBase.TXT_RST));
	            }
            }
            else if (key == Hotkeys.INVENTORY_PREVIEW_TOGGLE_SCREEN.getKeybind())
            {
                if (mc.screen instanceof InventoryOverlayScreen)
                {
                    mc.setScreen(null);
                }
                else if (FeatureToggle.TWEAK_INVENTORY_PREVIEW.getBooleanValue() &&
                        Hotkeys.INVENTORY_PREVIEW.getKeybind().isKeybindHeld())
                {
                    InventoryOverlayHandler.getInstance().refreshInventoryOverlay(mc, Configs.Generic.SHULKER_DISPLAY_BACKGROUND_COLOR.getBooleanValue());
                }
            }

            return false;
        }

        private void setFlySpeedPreset(int preset)
        {
            Configs.Internal.FLY_SPEED_PRESET.setIntegerValue(preset);

            float speed = (float) Configs.getActiveFlySpeedConfig().getDoubleValue();
            String strPreset = GuiBase.TXT_GREEN + (preset + 1) + GuiBase.TXT_RST;
            String strSpeed = String.format("%s%.3f%s", GuiBase.TXT_GREEN, speed, GuiBase.TXT_RST);
            InfoUtils.printActionbarMessage("tweakeroo.message.set_fly_speed_preset_to", strPreset, strSpeed);
        }

        private void setBreakingRestrictionMode(PlacementRestrictionMode mode)
        {
            Configs.Generic.BREAKING_RESTRICTION_MODE.setOptionListValue(mode);

            String str = GuiBase.TXT_GREEN + mode.name() + GuiBase.TXT_RST;
            InfoUtils.printActionbarMessage("tweakeroo.message.set_breaking_restriction_mode_to", str);
        }

        private void setPlacementRestrictionMode(PlacementRestrictionMode mode)
        {
            Configs.Generic.PLACEMENT_RESTRICTION_MODE.setOptionListValue(mode);

            String str = GuiBase.TXT_GREEN + mode.name() + GuiBase.TXT_RST;
            InfoUtils.printActionbarMessage("tweakeroo.message.set_placement_restriction_mode_to", str);
        }
    }

    private static class KeyCallbackToggleFastMode implements IHotkeyCallback
    {
        private final FeatureToggle feature;

        private KeyCallbackToggleFastMode(FeatureToggle feature)
        {
            this.feature = feature;
        }

        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key)
        {
            this.feature.toggleBooleanValue();

            boolean enabled = this.feature.getBooleanValue();
            String strStatus = StringUtils.translate("tweakeroo.message.value." + (enabled ? "on" : "off"));
            String preGreen = GuiBase.TXT_GREEN;
            String preRed = GuiBase.TXT_RED;
            String rst = GuiBase.TXT_RST;
            strStatus = (enabled ? preGreen : preRed) + strStatus + rst;

            if (enabled)
            {
                String strMode = ((PlacementRestrictionMode) Configs.Generic.PLACEMENT_RESTRICTION_MODE.getOptionListValue()).name();
                InfoUtils.printActionbarMessage("tweakeroo.message.toggled_fast_placement_mode_on", strStatus, preGreen + strMode + rst);
            }
            else
            {
                InfoUtils.printActionbarMessage("tweakeroo.message.toggled", this.feature.getPrettyName(), strStatus);
            }

            return true;
        }
    }

    private static class KeyCallbackAdjustableFeature implements IHotkeyCallback
    {
        private final IConfigBoolean config;

        private static IHotkeyCallback createCallback(IConfigBoolean config)
        {
            return new KeyCallbackAdjustable(config, new KeyCallbackAdjustableFeature(config));
        }

        private KeyCallbackAdjustableFeature(IConfigBoolean config)
        {
            this.config = config;
        }

        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key)
        {
            this.config.toggleBooleanValue();

            boolean enabled = this.config.getBooleanValue();
            String strStatus = StringUtils.translate("tweakeroo.message.value." + (enabled ? "on" : "off"));
            String preGreen = GuiBase.TXT_GREEN;
            String preRed = GuiBase.TXT_RED;
            String rst = GuiBase.TXT_RST;
            String prettyName = this.config.getPrettyName();
            strStatus = (enabled ? preGreen : preRed) + strStatus + rst;

            if (key == FeatureToggle.TWEAK_AFTER_CLICKER.getKeybind())
            {
                if (enabled)
                {
                    String strValue = Configs.Generic.AFTER_CLICKER_CLICK_COUNT.getStringValue();
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled_after_clicker_on", strStatus, preGreen + strValue + rst);
                }
                else
                {
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled", prettyName, strStatus);
                }
            }
            else if (key == FeatureToggle.TWEAK_FLY_SPEED.getKeybind())
            {
                if (enabled)
                {
                    String strPreset = preGreen + (Configs.Internal.FLY_SPEED_PRESET.getIntegerValue() + 1) + rst;
                    String strSpeed = String.format("%s%.3f%s", preGreen, Configs.getActiveFlySpeedConfig().getDoubleValue(), rst);
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled_fly_speed_on", strStatus, strPreset, strSpeed);
                }
                else
                {
                    Player player = Minecraft.getInstance().player;

                    if (player != null)
                    {
                        player.getAbilities().setFlyingSpeed(0.05f);
                    }

                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled", prettyName, strStatus);
                }
            }
            else if (key == FeatureToggle.TWEAK_PLACEMENT_LIMIT.getKeybind())
            {
                if (enabled)
                {
                    String strValue = Configs.Generic.PLACEMENT_LIMIT.getStringValue();
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled_placement_limit_on", strStatus, preGreen + strValue + rst);
                }
                else
                {
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled", prettyName, strStatus);
                }
            }
            else if (key == FeatureToggle.TWEAK_HOTBAR_SLOT_CYCLE.getKeybind())
            {
                if (enabled)
                {
                    String strValue = Configs.Generic.HOTBAR_SLOT_CYCLE_MAX.getStringValue();
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled_slot_cycle_on", strStatus, preGreen + strValue + rst);
                }
                else
                {
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled", prettyName, strStatus);
                }
            }
            else if (key == FeatureToggle.TWEAK_HOTBAR_SLOT_RANDOMIZER.getKeybind())
            {
                if (enabled)
                {
                    String strValue = Configs.Generic.HOTBAR_SLOT_RANDOMIZER_MAX.getStringValue();
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled_slot_randomizer_on", strStatus, preGreen + strValue + rst);
                }
                else
                {
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled", prettyName, strStatus);
                }
            }
            else if (key == FeatureToggle.TWEAK_PLACEMENT_GRID.getKeybind())
            {
                if (enabled)
                {
                    String strValue = Configs.Generic.PLACEMENT_GRID_SIZE.getStringValue();
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled_placement_grid_on", strStatus, preGreen + strValue + rst);
                }
                else
                {
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled", prettyName, strStatus);
                }
            }
            else if (key == FeatureToggle.TWEAK_BREAKING_GRID.getKeybind())
            {
                if (enabled)
                {
                    String strValue = Configs.Generic.BREAKING_GRID_SIZE.getStringValue();
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled_breaking_grid_on", strStatus, preGreen + strValue + rst);
                }
                else
                {
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled", prettyName, strStatus);
                }
            }
            else if (key == FeatureToggle.TWEAK_SNAP_AIM.getKeybind())
            {
                if (enabled)
                {
                    SnapAimMode mode = (SnapAimMode) Configs.Generic.SNAP_AIM_MODE.getOptionListValue();

                    if (mode == SnapAimMode.YAW)
                    {
                        String yaw = String.valueOf(Configs.Generic.SNAP_AIM_YAW_STEP.getDoubleValue());
                        InfoUtils.printActionbarMessage("tweakeroo.message.toggled_snap_aim_on_yaw", strStatus, preGreen + yaw + rst);
                    }
                    else if (mode == SnapAimMode.PITCH)
                    {
                        String pitch = String.valueOf(Configs.Generic.SNAP_AIM_PITCH_STEP.getDoubleValue());
                        InfoUtils.printActionbarMessage("tweakeroo.message.toggled_snap_aim_on_pitch", strStatus, preGreen + pitch + rst);
                    }
                    else
                    {
                        String yaw = String.valueOf(Configs.Generic.SNAP_AIM_YAW_STEP.getDoubleValue());
                        String pitch = String.valueOf(Configs.Generic.SNAP_AIM_PITCH_STEP.getDoubleValue());
                        InfoUtils.printActionbarMessage("tweakeroo.message.toggled_snap_aim_on_both", strStatus, preGreen + yaw + rst, preGreen + pitch + rst);
                    }
                }
                else
                {
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled", prettyName, strStatus);
                }
            }
            else if (key == FeatureToggle.TWEAK_ZOOM.getKeybind())
            {
                if (enabled)
                {
                    String strValue = String.format("%s%.1f%s", preGreen, Configs.Generic.ZOOM_FOV.getDoubleValue(), rst);
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled_zoom_on", strStatus, strValue);
                }
                else
                {
                    //String strValue = String.format("%s%.1f%s", preGreen, Configs.Generic.ZOOM_FOV.getDoubleValue(), rst);
                    //InfoUtils.printActionbarMessage("tweakeroo.message.toggled_zoom_off", strStatus, strValue);
                    InfoUtils.printActionbarMessage("tweakeroo.message.toggled", prettyName, strStatus);
                }
            }
//            else if (key == FeatureToggle.TWEAK_PERIODIC_ATTACK.getKeybind())
//            {
//                if (enabled)
//                {
//                    MiscUtils.onPeriodicAttackActivated();
//                }
//                else
//                {
//                    MiscUtils.onPeriodicAttackDeactivated();
//                }
//
//                String strValue = String.format("%s%01d%s", preGreen, Configs.Generic.PERIODIC_ATTACK_INTERVAL.getIntegerValue(), rst);
//                InfoUtils.printActionbarMessage("tweakeroo.message.toggled_periodic", prettyName, strStatus, strValue);
//            }
//            else if (key == FeatureToggle.TWEAK_PERIODIC_USE.getKeybind())
//            {
//                if (enabled)
//                {
//                    MiscUtils.onPeriodicUseActivated();
//                }
//                else
//                {
//                    MiscUtils.onPeriodicUseDeactivated();
//                }
//
//                String strValue = String.format("%s%01d%s", preGreen, Configs.Generic.PERIODIC_USE_INTERVAL.getIntegerValue(), rst);
//                InfoUtils.printActionbarMessage("tweakeroo.message.toggled_periodic", prettyName, strStatus, strValue);
//            }
//            else if (key == FeatureToggle.TWEAK_PERIODIC_HOLD_ATTACK.getKeybind())
//            {
//                if (enabled)
//                {
//                    MiscUtils.onPeriodicHoldAttackActivated();
//                }
//                else
//                {
//                    MiscUtils.onPeriodicHoldAttackDeactivated();
//                }
//
//                String strValue = String.format("%s%01d%s", preGreen, Configs.Generic.PERIODIC_HOLD_ATTACK_INTERVAL.getIntegerValue(), rst);
//                InfoUtils.printActionbarMessage("tweakeroo.message.toggled_periodic", prettyName, strStatus, strValue);
//            }
//            else if (key == FeatureToggle.TWEAK_PERIODIC_HOLD_USE.getKeybind())
//            {
//                if (enabled)
//                {
//                    MiscUtils.onPeriodicHoldUseActivated();
//                }
//                else
//                {
//                    MiscUtils.onPeriodicHoldUseDeactivated();
//                }
//
//                String strValue = String.format("%s%01d%s", preGreen, Configs.Generic.PERIODIC_HOLD_USE_INTERVAL.getIntegerValue(), rst);
//                InfoUtils.printActionbarMessage("tweakeroo.message.toggled_periodic", prettyName, strStatus, strValue);
//            }

            return true;
        }
    }
}
