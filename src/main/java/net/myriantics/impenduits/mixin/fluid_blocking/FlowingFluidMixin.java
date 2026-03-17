package net.myriantics.impenduits.mixin.fluid_blocking;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FlowingFluid;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {

    @ModifyExpressionValue(
            method = "canHoldFluid",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/tags/BlockTags;SIGNS:Lnet/minecraft/tags/TagKey;", opcode = Opcodes.GETSTATIC),
                    to = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/Blocks;SUGAR_CANE:Lnet/minecraft/world/level/block/Block;", opcode = Opcodes.GETSTATIC)
            ),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    )
    private boolean impenduitFieldOverride(boolean original, @Local Block block) {
        // if the block is an impenduit field, then fluids can't flow through it
        return original || block instanceof ImpenduitFieldBlock;
    }
}
