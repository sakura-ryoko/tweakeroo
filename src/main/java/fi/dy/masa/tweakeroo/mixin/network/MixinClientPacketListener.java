package fi.dy.masa.tweakeroo.mixin.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.DataManager;
import fi.dy.masa.tweakeroo.data.EntityDataManager;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener extends ClientCommonPacketListenerImpl
{
    @Shadow private ClientLevel level;
    @Shadow private int serverSimulationDistance;

    protected MixinClientPacketListener(Minecraft client, Connection connection, CommonListenerCookie connectionState)
    {
        super(client, connection, connectionState);
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "handleOpenScreen", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_onOpenScreenListener(ClientboundOpenScreenPacket packet, CallbackInfo ci)
    {
        if (!RenderTweaks.onOpenScreen(packet.getTitle(), packet.getType(), packet.getContainerId()))
        {
            ci.cancel();
        }
    }

    @Inject(method = "handleContainerSetSlot", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setItem(IILnet/minecraft/world/item/ItemStack;)V"),
            cancellable = true)
    private void tweakeroo_onHandleSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci)
    {
        if (PlacementTweaks.shouldSkipSlotSync(packet.getSlot(), packet.getItem()))
        {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerCombatKill", at = @At(value = "INVOKE", // onCombatEvent
                                                target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void tweakeroo_onPlayerDeath(ClientboundPlayerCombatKillPacket packetIn, CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();

        if (FeatureToggle.TWEAK_PRINT_DEATH_COORDINATES.getBooleanValue() && mc.player != null)
        {
            MiscUtils.printDeathCoordinates(mc);
        }
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "handleBlockEvent", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_onBlockEvent(ClientboundBlockEventPacket clientboundBlockEventPacket, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CLIENT_BLOCK_EVENTS.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void tweakeroo_onCustomPayload(CustomPacketPayload payload, CallbackInfo ci)
    {
        if (payload.type().id().equals(DataManager.CARPET_HELLO))
        {
            DataManager.getInstance().setHasCarpetServer(true);
        }
        else if (payload.type().id().getNamespace().equals("servux"))
        {
            DataManager.getInstance().setHasServuxServer(true);
        }
    }

    @Inject(method = "handleCommands", at = @At("RETURN"))
    private void tweakeroo_onCommandTree(CallbackInfo ci)
    {
        if (Configs.Generic.ENTITY_DATA_SYNC_BACKUP.getBooleanValue())
        {
            // when the player becomes OP, the server sends the command tree to the client
            EntityDataManager.getInstance().resetOpCheck();
        }
    }

    @Inject(method = "handleEntityEvent",
            at = @At(value = "INVOKE", ordinal = 0,
                     target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;findTotem(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"))
    private void tweakeroo_onPlayerUseTotemOfUndying(ClientboundEntityEventPacket packet, CallbackInfo ci)
    {
        if (this.minecraft.player == null)
        {
            return;
        }
        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue())
        {
            for (InteractionHand hand : InteractionHand.values())
            {
                if (this.minecraft.player.getItemInHand(hand).is(Items.TOTEM_OF_UNDYING))
                {
                    PlacementTweaks.cacheStackInHand(hand);
                    // the slot update packet goes after this packet, let's set it to empty and restock
                    this.minecraft.player.setItemInHand(hand, ItemStack.EMPTY);
                    PlacementTweaks.onProcessRightClickPost(this.minecraft.player, hand);
                }
            }
        }
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "handleRespawn", at = @At(value = "NEW",
                                                 target = "net/minecraft/client/multiplayer/ClientLevel"))
    private void tweakeroo_onPlayerRespawnInject(ClientboundRespawnPacket packet, CallbackInfo ci)
    {
        RenderTweaks.resetWorld(this.serverSimulationDistance);
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "handleLogin", at = @At(value = "NEW",
                                            target = "net/minecraft/client/multiplayer/ClientLevel"))
    private void tweakeroo_onGameJoinInject(ClientboundLoginPacket packet, CallbackInfo ci)
    {
        RenderTweaks.resetWorld(this.serverSimulationDistance);
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "handleLevelChunkWithLight", at = @At("RETURN"))
    private void tweakeroo_onChunkDataInject(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci)
    {
        int cx = packet.getX();
        int cz = packet.getZ();
        RenderTweaks.loadFakeChunk(cx, cz);

        if (!FeatureToggle.TWEAK_SELECTIVE_BLOCKS_RENDERING.getBooleanValue())
        {
            return;
        }
        LevelChunk worldChunk = this.level.getChunkSource().getChunkNow(cx, cz);

        if (worldChunk != null)
        {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            LevelChunkSection[] sections = worldChunk.getSections();
            for (int i = 0; i < sections.length; i++)
            {
                LevelChunkSection section = sections[i];
                if (section != null && !section.hasOnlyAir())
                {
                    for (int x = 0; x < 16; x++)
                    {
                        for (int y = 0; y < 16; y++)
                        {
                            for (int z = 0; z < 16; z++)
                            {
                                pos.set(x + worldChunk.getPos().getMinBlockX(), y + this.level.getSectionYFromSectionIndex(i), z + worldChunk.getPos().getMinBlockZ());

                                if (!RenderTweaks.isPositionValidForRendering(pos))
                                {
                                    BlockEntity be = worldChunk.getBlockEntity(pos);
                                    BlockState state = section.getBlockState(x, y, z);
                                    worldChunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), -1);
                                    RenderTweaks.setFakeBlockState(this.level, pos, state, be);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "handleForgetLevelChunk", at = @At("RETURN"))
    private void tweakeroo_onUnloadChunkInject(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci)
    {
        int i = packet.pos().x;
        int j = packet.pos().z;
        RenderTweaks.unloadFakeChunk(i, j);
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "handleSetChunkCacheRadius", at = @At("RETURN"))
    private void tweakeroo_onChunkLoadDistanceInject(ClientboundSetChunkCacheRadiusPacket packet, CallbackInfo ci)
    {
        RenderTweaks.getFakeWorld().getChunkSource().updateLoadDistance(packet.getRadius());
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "handleSetChunkCacheCenter", at = @At("RETURN"))
    private void tweakeroo_onChunkRenderDistanceCenterInject(ClientboundSetChunkCacheCenterPacket packet, CallbackInfo ci)
    {
        RenderTweaks.getFakeWorld().getChunkSource().setChunkMapCenter(packet.getX(), packet.getZ());
    }

    @ModifyArgs(method = "handleTickingState",
                at = @At(value = "INVOKE",
                         target = "Lnet/minecraft/world/TickRateManager;setTickRate(F)V"
                )
    )
    private void tweakeroo_stopPlayerSlowdown(Args args)
    {
        float tickRate = args.get(0);
        MiscUtils.setTickRate(tickRate);

        if (Configs.Disable.DISABLE_TICKRATE_PLAYER_SLOWDOWN.getBooleanValue())
        {
            if (tickRate < MiscUtils.DEFAULT_TICK_RATE)
            {
                args.set(0, MiscUtils.DEFAULT_TICK_RATE);
            }
        }
    }
}
