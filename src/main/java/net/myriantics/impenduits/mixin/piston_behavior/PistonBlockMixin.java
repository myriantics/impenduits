package net.myriantics.impenduits.mixin.piston_behavior;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBlockMixin {

    @ModifyExpressionValue(
            method = "isPushable",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getPistonPushReaction()Lnet/minecraft/world/level/material/PushReaction;")
    )
    private static PushReaction pistonBehaviorOverride2(PushReaction original, @Local(argsOnly = true) BlockState pushedState) {

        // you're not allowed to push anything in an established impenduit field
        if (pushedState.getBlock() instanceof ImpenduitPylonBlock impenduitPylonBlock) {
            return impenduitPylonBlock.getPistonBehaviorFromState(pushedState);
        }

        return original;
    }
}
