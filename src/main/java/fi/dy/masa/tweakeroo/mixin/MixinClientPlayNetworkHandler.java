package fi.dy.masa.tweakeroo.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.DataManager;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler
{
    @Shadow private ClientWorld world;
    @Shadow private int simulationDistance;

    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState)
    {
        super(client, connection, connectionState);
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "onOpenScreen", at = @At("HEAD"), cancellable = true)
    private void onOpenScreenListener(OpenScreenS2CPacket packet, CallbackInfo ci)
    {
        if (!RenderTweaks.onOpenScreen(packet.getName(), packet.getScreenHandlerType(), packet.getSyncId()))
        {
            ci.cancel();
        }
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/screen/ScreenHandler;setStackInSlot(IILnet/minecraft/item/ItemStack;)V"),
            cancellable = true)
    private void onHandleSetSlot(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci)
    {
        if (PlacementTweaks.shouldSkipSlotSync(packet.getSlot(), packet.getStack()))
        {
            ci.cancel();
        }
    }

    @Inject(method = "onDeathMessage", at = @At(value = "INVOKE", // onCombatEvent
                                                target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void onPlayerDeath(DeathMessageS2CPacket packetIn, CallbackInfo ci)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (FeatureToggle.TWEAK_PRINT_DEATH_COORDINATES.getBooleanValue() && mc.player != null)
        {
            MiscUtils.printDeathCoordinates(mc);
        }
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    private void tweakeroo_onCustomPayload(CustomPayload payload, CallbackInfo ci)
    {
        if (payload.getId().id().equals(DataManager.CARPET_HELLO))
        {
            DataManager.getInstance().setHasCarpetServer(true);
        }
        else if (payload.getId().id().equals(DataManager.SERVUX_ENTITY_DATA))
        {
            DataManager.getInstance().setHasServuxServer(true);
        }
    }

    @Inject(
            method = "onEntityStatus",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;getActiveTotemOfUndying(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;")
    )
    private void onPlayerUseTotemOfUndying(EntityStatusS2CPacket packet, CallbackInfo ci)
    {
        if (this.client.player == null)
        {
            return;
        }
        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue())
        {
            for (Hand hand : Hand.values())
            {
                if (this.client.player.getStackInHand(hand).isOf(Items.TOTEM_OF_UNDYING))
                {
                    PlacementTweaks.cacheStackInHand(hand);
                    // the slot update packet goes after this packet, let's set it to empty and restock
                    this.client.player.setStackInHand(hand, ItemStack.EMPTY);
                    PlacementTweaks.onProcessRightClickPost(this.client.player, hand);
                }
            }
        }
    }
}
