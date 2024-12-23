package net.myriantics.impenduits.mixin;

import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.myriantics.impenduits.ImpenduitsCommon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ConduitBlockEntity.class)
public abstract class ConduitBlockEntityMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), method = "updateActivatingBlocks")
	private static void init(World world, BlockPos pos, List<BlockPos> activatingBlocks, CallbackInfoReturnable<Boolean> cir) {
		// allow impenduit pylons to be used in conduit frames
		if (world.getBlockState(pos).isOf(ImpenduitsCommon.IMPENDUIT_PYLON)) {
			 activatingBlocks.add(pos);
		 }
	}
}