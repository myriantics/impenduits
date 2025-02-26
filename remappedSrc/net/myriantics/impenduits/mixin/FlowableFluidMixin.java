package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.fluid.FlowableFluid;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin {

    @ModifyExpressionValue(
            method = "canFill",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z",
            ordinal = 0)
    )
    private boolean impenduitFieldOverride(boolean original, @Local Block block) {
        // if the block is an impenduit field, then fluids can't flow through it
        return original || block instanceof ImpenduitFieldBlock;
    }
}
