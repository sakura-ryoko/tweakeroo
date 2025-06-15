package fi.dy.masa.tweakeroo.renderer;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;

import fi.dy.masa.malilib.interfaces.IDataSyncer;
import fi.dy.masa.malilib.interfaces.IInventoryOverlayHandler;
import fi.dy.masa.malilib.mixin.entity.IMixinAbstractHorseEntity;
import fi.dy.masa.malilib.mixin.entity.IMixinPiglinEntity;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.util.EntityUtils;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.malilib.util.nbt.NbtBlockUtils;
import fi.dy.masa.malilib.util.nbt.NbtKeys;
import fi.dy.masa.malilib.util.nbt.NbtView;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.data.ServerDataSyncer;
import fi.dy.masa.tweakeroo.util.RayTraceUtils;

public class InventoryOverlayHandler implements IInventoryOverlayHandler
{
    private static final InventoryOverlayHandler INSTANCE = new InventoryOverlayHandler();
    public static InventoryOverlayHandler getInstance() { return INSTANCE; }

    InventoryOverlay.Context context;
    InventoryOverlay.Refresher refresher;
    IDataSyncer syncer;

    private Pair<BlockPos, InventoryOverlay.Context> lastBlockEntityContext;
    private Pair<Integer,  InventoryOverlay.Context> lastEntityContext;
    
    public InventoryOverlayHandler()
    {
        this.lastBlockEntityContext = null;
        this.lastEntityContext = null;
        this.context = null;
        this.syncer = null;
        this.refresher = null;
    }

    @Override
    public String getModId()
    {
        return Reference.MOD_ID;
    }

    @Override
    public IDataSyncer getDataSyncer()
    {
        if (this.syncer == null)
        {
            this.syncer = ServerDataSyncer.getInstance();
        }
        
        return this.syncer;
    }

    @Override
    public void setDataSyncer(IDataSyncer syncer)
    {
        this.syncer = syncer;
    }

    @Override
    public InventoryOverlay.Refresher getRefreshHandler()
    {
        if (this.refresher == null)
        {
            this.refresher = new Refresher();
        }

        return this.refresher;
    }

    @Override
    public boolean isEmpty()
    {
        return this.context == null;
    }

    @Override
    public @Nullable InventoryOverlay.Context getRenderContextNullable()
    {
        return this.context;
    }

    @Override
    public @Nullable InventoryOverlay.Context getRenderContext(DrawContext drawContext, Profiler profiler, MinecraftClient mc)
    {
        profiler.push(this.getClass().getName() + "_inventory_overlay");
        this.getTargetInventory(mc);

        if (!this.isEmpty())
        {
            this.renderInventoryOverlay(drawContext, this.getRenderContextNullable(), mc,
                                        Configs.Generic.SHULKER_DISPLAY_BACKGROUND_COLOR.getBooleanValue(),
                                        Configs.Generic.INVENTORY_PREVIEW_VILLAGER_BG_COLOR.getBooleanValue());
        }

        profiler.pop();

        return this.getRenderContextNullable();
    }

    @Override
    public @Nullable InventoryOverlay.Context getTargetInventory(MinecraftClient mc)
    {
        World world = WorldUtils.getBestWorld(mc);
        Entity cameraEntity = EntityUtils.getCameraEntity();
        this.context = null;

        if (mc.player == null || world == null)
        {
            return null;
        }

        if (cameraEntity == mc.player && world instanceof ServerWorld)
        {
            // We need to get the player from the server world (if available, ie. in single player),
            // so that the player itself won't be included in the ray trace
            Entity serverPlayer = world.getPlayerByUuid(mc.player.getUuid());

            if (serverPlayer != null)
            {
                cameraEntity = serverPlayer;
            }
        }

        if (cameraEntity == null)
        {
            return null;
        }

        HitResult trace = RayTraceUtils.getRayTraceFromEntity(cameraEntity.getWorld(), cameraEntity, false);
        NbtCompound nbt = new NbtCompound();

        if (trace == null || trace.getType() == HitResult.Type.MISS)
        {
            return null;
        }

        if (trace.getType() == HitResult.Type.BLOCK)
        {
            BlockPos pos = ((BlockHitResult) trace).getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block blockTmp = state.getBlock();
            BlockEntity be = null;

            //Tweakeroo.LOGGER.warn("getTarget():1: pos [{}], state [{}]", pos.toShortString(), state.toString());

            if (blockTmp instanceof BlockEntityProvider)
            {
                if (world instanceof ServerWorld)
                {
                    be = world.getWorldChunk(pos).getBlockEntity(pos);

                    if (be != null)
                    {
                        nbt = be.createNbtWithIdentifyingData(world.getRegistryManager());
                    }
                }
                else
                {
                    Pair<BlockEntity, NbtCompound> pair = this.requestBlockEntityAt(world, pos);

                    if (pair != null)
                    {
                        nbt = pair.getRight();
                    }
                }

                //Tweakeroo.LOGGER.warn("getTarget():2: pos [{}], be [{}], nbt [{}]", pos.toShortString(), be != null, nbt != null);
                InventoryOverlay.Context ctx = getTargetInventoryFromBlock(world, pos, be, nbt);
                //dumpContext(ctx);

                if (this.lastBlockEntityContext != null && !this.lastBlockEntityContext.getLeft().equals(pos))
                {
                    this.lastBlockEntityContext = null;
                }

                if (ctx != null && ctx.inv() != null)
                {
                    this.lastBlockEntityContext = Pair.of(pos, ctx);
                    this.context = ctx;
                    return this.context;
                }
                else if (this.lastBlockEntityContext != null && this.lastBlockEntityContext.getLeft().equals(pos))
                {
                    this.context = this.lastBlockEntityContext.getRight();
                    return this.context;
                }
            }

            return null;
        }
        else if (trace.getType() == HitResult.Type.ENTITY)
        {
            Entity entity = ((EntityHitResult) trace).getEntity();

            if (world instanceof ServerWorld)
            {
//                entity.saveSelfNbt(nbt);
                NbtView view = NbtView.getWriter(world.getRegistryManager());
                entity.writeData(view.getWriter());
                nbt = view.readNbt();
                Identifier id = EntityType.getId(entity.getType());

                if (nbt != null && id != null)
                {
                    nbt.putString("id", id.toString());
                }
            }
            else
            {
                Pair<Entity, NbtCompound> pair = this.getDataSyncer().requestEntity(world, entity.getId());

                if (pair != null)
                {
                    nbt = pair.getRight();
                }
            }

            //Tweakeroo.LOGGER.error("getTarget(): Entity [{}] raw NBT [{}]", entity.getId(), nbt.toString());
            InventoryOverlay.Context ctx = getTargetInventoryFromEntity(world.getEntityById(entity.getId()), nbt);
            //dumpContext(ctx);

            if (this.lastEntityContext != null && this.lastEntityContext.getLeft() != entity.getId())
            {
                this.lastEntityContext = null;
            }

            // Has Inventory, Not Empty
            if (ctx != null && ctx.inv() != null && !ctx.inv().isEmpty())
            {
                this.lastEntityContext = Pair.of(entity.getId(), ctx);
                this.context = ctx;
                return this.context;
            }
            // Has Inventory, Empty, but Has the Offers tag (Villager/Merchant)
            else if (ctx != null && ctx.inv() != null && ctx.inv().isEmpty() &&
                     ctx.nbt() != null && ctx.nbt().contains(NbtKeys.OFFERS))
            {
                this.lastEntityContext = Pair.of(entity.getId(), ctx);
                this.context = ctx;
                return this.context;
            }
            // Non-Inventory/Empty Entity
            else if (ctx != null && ctx.inv() == null &&
                    (ctx.type() == InventoryOverlay.InventoryRenderType.WOLF ||
                     ctx.type() == InventoryOverlay.InventoryRenderType.VILLAGER ||
                     ctx.type() == InventoryOverlay.InventoryRenderType.HORSE ||
                     ctx.type() == InventoryOverlay.InventoryRenderType.PLAYER ||
                     ctx.type() == InventoryOverlay.InventoryRenderType.ARMOR_STAND ||
                     ctx.type() == InventoryOverlay.InventoryRenderType.LIVING_ENTITY))
            {
                this.lastEntityContext = Pair.of(entity.getId(), ctx);
                this.context = ctx;
                return this.context;
            }
            // Has Inventory, but empty, and exists in lastEntityContext
            else if (this.lastEntityContext != null && this.lastEntityContext.getLeft() == entity.getId() &&
                    ctx != null && ctx.inv() != null && ctx.inv().isEmpty() &&
                    (ctx.type() == InventoryOverlay.InventoryRenderType.VILLAGER ||
                     ctx.type() == InventoryOverlay.InventoryRenderType.HORSE ||
                     ctx.type() == InventoryOverlay.InventoryRenderType.PLAYER))
            {
                this.context = this.lastEntityContext.getRight();
                return this.context;
            }
            // Other, and exists in lastEntityContext
            else if (this.lastEntityContext != null && this.lastEntityContext.getLeft() == entity.getId())
            {
                this.context = this.lastEntityContext.getRight();
                return this.context;
            }
        }

        return null;
    }

    @Override
    public @Nullable InventoryOverlay.Context getTargetInventoryFromBlock(World world, BlockPos pos, @Nullable BlockEntity be, NbtCompound nbt)
    {
        Inventory inv;

        if (world == null) return null;

        if (be != null)
        {
            if (nbt.isEmpty())
            {
                nbt = be.createNbtWithIdentifyingData(world.getRegistryManager());
            }

            inv = InventoryUtils.getInventory(world, pos);
        }
        else
        {
            if (nbt.isEmpty())
            {
                Pair<BlockEntity, NbtCompound> pair = this.requestBlockEntityAt(world, pos);

                if (pair != null)
                {
                    nbt = pair.getRight();
                }
            }

            inv = this.getDataSyncer().getBlockInventory(world, pos, false);
        }

        BlockEntityType<?> beType = nbt != null ? NbtBlockUtils.getBlockEntityTypeFromNbt(nbt) : null;
        //Tweakeroo.LOGGER.warn("getTargetInventoryFromBlock() beType: [{}], inv [{}]", beType != null ? beType.toString() : "<null>", inv != null ? inv.size() : "<null>");

        if ((beType != null && beType.equals(BlockEntityType.ENDER_CHEST)) ||
            be instanceof EnderChestBlockEntity)
        {
            if (MinecraftClient.getInstance().player != null)
            {
                PlayerEntity player = world.getPlayerByUuid(MinecraftClient.getInstance().player.getUuid());

                if (player != null)
                {
                    // Fetch your own EnderItems from Server ...
                    Pair<Entity, NbtCompound> enderPair = this.getDataSyncer().requestEntity(world, player.getId());
                    EnderChestInventory enderItems;

                    if (enderPair != null && enderPair.getRight() != null && enderPair.getRight().contains(NbtKeys.ENDER_ITEMS))
                    {
                        enderItems = InventoryUtils.getPlayerEnderItemsFromNbt(enderPair.getRight(), world.getRegistryManager());
                    }
                    else
                    {
                        enderItems = player.getEnderChestInventory();
                    }

                    if (enderItems != null)
                    {
                        inv = enderItems;
                    }
                }
            }
        }

        if (nbt != null && !nbt.isEmpty())
        {
            //Tweakeroo.LOGGER.warn("getTargetInventoryFromBlock(): rawNbt: [{}]", nbt.toString());
            Inventory inv2 = InventoryUtils.getNbtInventory(nbt, inv != null ? inv.size() : -1, world.getRegistryManager());

            if (inv == null)
            {
                inv = inv2;
            }
        }

        //Tweakeroo.LOGGER.warn("getTarget():3: pos [{}], inv [{}], be [{}], nbt [{}]", pos.toShortString(), inv != null, be != null, nbt != null ? nbt.getString("id") : new NbtCompound());

        if (inv == null || nbt == null)
        {
            return null;
        }

        this.context = new InventoryOverlay.Context(InventoryOverlay.getBestInventoryType(inv, nbt), inv, be != null ? be : world.getBlockEntity(pos), null, nbt, this.getRefreshHandler());

        return this.context;
    }

    @Override
    public @Nullable InventoryOverlay.Context getTargetInventoryFromEntity(Entity entity, NbtCompound nbt)
    {
        Inventory inv = null;
        LivingEntity entityLivingBase = null;

        if (entity instanceof LivingEntity)
        {
            entityLivingBase = (LivingEntity) entity;
        }

        if (entity instanceof Inventory)
        {
            inv = (Inventory) entity;
        }
        else if (entity instanceof PlayerEntity player)
        {
            inv = new SimpleInventory(player.getInventory().getMainStacks().toArray(new ItemStack[36]));
        }
        else if (entity instanceof VillagerEntity)
        {
            inv = ((VillagerEntity) entity).getInventory();
        }
        else if (entity instanceof AbstractHorseEntity)
        {
            inv = ((IMixinAbstractHorseEntity) entity).malilib_getHorseInventory();
        }
        else if (entity instanceof PiglinEntity)
        {
            inv = ((IMixinPiglinEntity) entity).malilib_getInventory();
        }
        if (!nbt.isEmpty())
        {
            Inventory inv2;

            //Tweakeroo.LOGGER.warn("getTargetInventoryFromEntity(): rawNbt: [{}]", nbt.toString());
            //Tweakeroo.LOGGER.warn("getTargetInventoryFromEntity(): pre-inv: [{}]", inv != null ? inv.size() : "<NULL>");

            // Fix for empty horse inv
            if (inv != null &&
                nbt.contains(NbtKeys.ITEMS) &&
                nbt.getListOrEmpty(NbtKeys.ITEMS).size() > 1)
            {
                if (entity instanceof AbstractHorseEntity)
                {
                    inv2 = InventoryUtils.getNbtInventoryHorseFix(nbt, -1, entity.getRegistryManager());
                }
                else
                {
                    inv2 = InventoryUtils.getNbtInventory(nbt, -1, entity.getRegistryManager());
                }

                inv = null;
            }
            // Fix for saddled horse, no inv
            else if (inv != null &&
                     nbt.contains(NbtKeys.EQUIPMENT) && nbt.contains(NbtKeys.EATING_HAY))
            {
                inv2 = InventoryUtils.getNbtInventoryHorseFix(nbt, -1, entity.getRegistryManager());
                inv = null;
            }
            // Fix for empty Villager/Piglin inv
            else if (inv != null && inv.size() == 8 &&
                     nbt.contains(NbtKeys.INVENTORY) &&
                     !nbt.getListOrEmpty(NbtKeys.INVENTORY).isEmpty())
            {
                inv2 = InventoryUtils.getNbtInventory(nbt, 8, entity.getRegistryManager());
                inv = null;
            }
            else
            {
                inv2 = InventoryUtils.getNbtInventory(nbt, inv != null ? inv.size() : -1, entity.getRegistryManager());

                if (inv2 != null)
                {
                    inv = null;
                }
            }

            //Tweakeroo.LOGGER.error("getTargetInventoryFromEntity(): inv.size [{}], inv2.size [{}]", inv != null ? inv.size() : "null", inv2 != null ? inv2.size() : "null");

            if (inv2 != null)
            {
                inv = inv2;
            }
        }

        if (inv == null && entityLivingBase == null)
        {
            return null;
        }

        this.context = new InventoryOverlay.Context(inv != null ? InventoryOverlay.getBestInventoryType(inv, nbt) : InventoryOverlay.getInventoryType(nbt),
                                                    inv, null, entityLivingBase, nbt, this.getRefreshHandler());

        return this.context;
    }

    private static void dumpContext(InventoryOverlay.Context ctx)
    {
        System.out.print("Context Dump --> ");

        if (ctx == null)
        {
            System.out.print("NULL!\n");
            return;
        }

        System.out.printf("\nTYPE: [%s]\n", ctx.type().name());
        System.out.printf("BE  : [%s]\n", ctx.be() != null ? Registries.BLOCK_ENTITY_TYPE.getId(ctx.be().getType()) : "<NULL>");
        System.out.printf("ENT : [%s]\n", ctx.entity() != null ? Registries.ENTITY_TYPE.getId(ctx.entity().getType()) : "<NULL>");
        System.out.printf("INV : [%s]\n", ctx.inv() != null ? "size: "+ctx.inv().size()+"/ empty: "+ctx.inv().isEmpty() : "<NULL>");
        System.out.printf("NBT : [%s]\n", ctx.nbt() != null ? ctx.nbt().toString() : "<NULL>");

        System.out.print("--> EOF\n");
    }

    public static class Refresher implements InventoryOverlay.Refresher
    {

        @Override
        public InventoryOverlay.Context onContextRefresh(InventoryOverlay.Context data, World world)
        {
            // Refresh data
            if (data.be() != null)
            {
                InventoryOverlayHandler.getInstance().requestBlockEntityAt(world, data.be().getPos());
                data = InventoryOverlayHandler.getInstance().getTargetInventoryFromBlock(data.be().getWorld(), data.be().getPos(), data.be(), data.nbt());
            }
            else if (data.entity() != null)
            {
                InventoryOverlayHandler.getInstance().getDataSyncer().requestEntity(world, data.entity().getId());
                data = InventoryOverlayHandler.getInstance().getTargetInventoryFromEntity(data.entity(), data.nbt());
            }

            return data;
        }
    }
}
