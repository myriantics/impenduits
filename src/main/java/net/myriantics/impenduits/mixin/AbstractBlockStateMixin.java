package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.block.AbstractBlock$AbstractBlockState")
public abstract class AbstractBlockStateMixin {

    @Shadow public abstract Block getBlock();

    @ModifyReturnValue(
            method = "blocksMovement",
            at = @At(value = "RETURN")
    )
    private boolean impenduits$impenduitFieldRainOverride(boolean original) {
        return original || getBlock() instanceof ImpenduitFieldBlock;
    }
}
