package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import net.myriantics.impenduits.util.ImpenduitsDispenserBehaviors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin {

    @ModifyExpressionValue(
            method = "dispense",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/DispenserBlock;getBehaviorForItem(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;")
    )
    public DispenserBehavior impenduits$impenduitPylonDispenserOverride(DispenserBehavior original, @Local(argsOnly = true) ServerWorld world, @Local(argsOnly = true) BlockState dispenserState, @Local(argsOnly = true) BlockPos dispenserPos, @Local ItemStack dispenserStack) {
        Block targetedBlock = world.getBlockState(dispenserPos.offset(dispenserState.get(DispenserBlock.FACING), 1)).getBlock();

        DispenserBehavior pylonOverrideBehavior = null;

        // yoink behavior if the targeted block is an impenduit pylon
        if (targetedBlock instanceof ImpenduitPylonBlock pylonBlock) {
            pylonOverrideBehavior = ImpenduitsDispenserBehaviors.getPylonDispenserBehavior(dispenserStack, pylonBlock);
        }

        // dont run the behavior if the override was null
        return pylonOverrideBehavior == null ? original : pylonOverrideBehavior;
    }
}
