package fi.dy.masa.tweakeroo.mixin;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VaultBlockEntity.Client.class)
public class MixinVaultBlockEntity
{
    @Inject(method = "spawnActivateParticles", at = @At("HEAD"), cancellable = true)
    private static void disable$spawnActivateParticles(World world, BlockPos pos, BlockState state, VaultSharedData sharedData, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_TRIAL_VAULT_PARTICLES.getBooleanValue())
        {
            ci.cancel();
        }
    }
    @Inject(method = "spawnDeactivateParticles", at = @At("HEAD"), cancellable = true)
    private static void disable$spawnDeactivateParticles(World world, BlockPos pos, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_TRIAL_VAULT_PARTICLES.getBooleanValue())
        {
            ci.cancel();
        }
    }
    @Inject(method = "spawnAmbientParticles", at = @At("HEAD"), cancellable = true)
    private static void disable$spawnAmbientParticles(World world, BlockPos pos, VaultSharedData sharedData, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_TRIAL_VAULT_PARTICLES.getBooleanValue())
        {
            ci.cancel();
        }
    }
    @Inject(method = "spawnConnectedParticlesFor", at = @At("HEAD"), cancellable = true)
    private static void disable$spawnConnectedParticlesFor(World world, Vec3d pos, PlayerEntity player, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_TRIAL_VAULT_PARTICLES.getBooleanValue())
        {
            ci.cancel();
        }
    }
    @Inject(method = "spawnConnectedParticles", at = @At("HEAD"), cancellable = true)
    private static void disable$spawnConnectedParticles(World world, BlockPos pos, BlockState state, VaultSharedData sharedData, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_TRIAL_VAULT_PARTICLES.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
