package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBlock.class)
public abstract class PistonBlockMixin {

    @ModifyExpressionValue(
            method = "isMovable",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getPistonBehavior()Lnet/minecraft/block/piston/PistonBehavior;")
    )
    private static PistonBehavior pistonBehaviorOverride2(PistonBehavior original, @Local(argsOnly = true) BlockState pushedState) {

        // you're not allowed to push anything in an established impenduit field
        if (pushedState.getBlock() instanceof ImpenduitPylonBlock impenduitPylonBlock) {
            return impenduitPylonBlock.getPistonBehaviorFromState(pushedState);
        }

        return original;
    }
}
