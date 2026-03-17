package net.myriantics.impenduits.mixin.dispenser_behavior;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import net.myriantics.impenduits.util.ImpenduitsDispenserBehaviors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin {

    @ModifyExpressionValue(
            method = "dispenseFrom",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/DispenserBlock;getDispenseMethod(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/core/dispenser/DispenseItemBehavior;")
    )
    public DispenseItemBehavior impenduits$impenduitPylonDispenserOverride(DispenseItemBehavior original, @Local(argsOnly = true) ServerLevel world, @Local(argsOnly = true) BlockState dispenserState, @Local(argsOnly = true) BlockPos dispenserPos, @Local ItemStack dispenserStack) {
        Block targetedBlock = world.getBlockState(dispenserPos.relative(dispenserState.getValue(DispenserBlock.FACING), 1)).getBlock();

        DispenseItemBehavior pylonOverrideBehavior = null;

        // yoink behavior if the targeted block is an impenduit pylon
        if (targetedBlock instanceof ImpenduitPylonBlock pylonBlock) {
            pylonOverrideBehavior = ImpenduitsDispenserBehaviors.getPylonDispenserBehavior(dispenserStack, pylonBlock);
        }

        // dont run the behavior if the override was null
        return pylonOverrideBehavior == null ? original : pylonOverrideBehavior;
    }
}
