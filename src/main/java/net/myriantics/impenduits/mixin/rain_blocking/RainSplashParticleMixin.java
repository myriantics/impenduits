package net.myriantics.impenduits.mixin.rain_blocking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.RainSplashParticle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RainSplashParticle.class)
public abstract class RainSplashParticleMixin {
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;")
    )
    private VoxelShape impenduits$treatImpenduitFieldBlocksAsFullBlocks(BlockState instance, BlockView blockView, BlockPos blockPos, Operation<VoxelShape> original) {
        return instance.getBlock() instanceof ImpenduitFieldBlock ? VoxelShapes.fullCube() : original.call(instance, blockView, blockPos);
    }
}
