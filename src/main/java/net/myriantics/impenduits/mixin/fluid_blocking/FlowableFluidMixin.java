package net.myriantics.impenduits.mixin.fluid_blocking;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlowingFluid.class)
public abstract class FlowableFluidMixin {

    @ModifyExpressionValue(
            method = "canHoldFluid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
            ordinal = 0)
    )
    private boolean impenduitFieldOverride(boolean original, @Local Block block) {
        // if the block is an impenduit field, then fluids can't flow through it
        return original || block instanceof ImpenduitFieldBlock;
    }
}
