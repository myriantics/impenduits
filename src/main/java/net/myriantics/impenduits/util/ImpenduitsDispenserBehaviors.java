package net.myriantics.impenduits.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import net.myriantics.impenduits.tag.ImpenduitsItemTags;

public abstract class ImpenduitsDispenserBehaviors {

    public static DispenserBehavior getPylonDispenserBehavior(ItemStack stack, ImpenduitPylonBlock pylonBlock) {

        if (stack.isIn(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE)) {
            return new FallibleItemDispenserBehavior() {
                @Override
                protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                    World world = pointer.world();
                    BlockPos targetPos = pointer.pos().offset(pointer.state().get(Properties.FACING), 1);
                    BlockState targetState = world.getBlockState(targetPos);
                    this.setSuccess(true);

                    // if it's an impenduit without the power core, run this
                    if (targetState.getBlock() instanceof ImpenduitPylonBlock pylonBlock) {

                        if (!targetState.get(ImpenduitPylonBlock.POWER_SOURCE_PRESENT)) {
                            pylonBlock.insertPowerCore(world, targetPos);
                            stack.decrement(1);
                        } else {
                            this.setSuccess(false);
                        }

                        return stack;
                    } else {
                        return super.dispenseSilently(pointer, stack);
                    }


                }
            };
        }

        if (stack.isIn(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)) {
            return new FallibleItemDispenserBehavior() {
                @Override
                protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                    World world = pointer.world();
                    BlockPos targetPos = pointer.pos().offset(pointer.state().get(Properties.FACING), 1);
                    BlockState targetState = world.getBlockState(targetPos);

                    this.setSuccess(true);

                    if (targetState.getBlock() instanceof ImpenduitPylonBlock pylonBlock) {
                        if (targetState.get(ImpenduitPylonBlock.POWER_SOURCE_PRESENT)) {
                            pylonBlock.removePowerCore(world, targetPos, stack, null, null);
                        } else {
                            this.setSuccess(false);
                        }

                        return stack;
                    } else {

                        return super.dispenseSilently(pointer, stack);
                    }

                }
            };
        }

        return null;
    }
}
