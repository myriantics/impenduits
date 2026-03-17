package net.myriantics.impenduits.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import net.myriantics.impenduits.tag.ImpenduitsItemTags;

public abstract class ImpenduitsDispenserBehaviors {

    public static DispenseItemBehavior getPylonDispenserBehavior(ItemStack stack, ImpenduitPylonBlock pylonBlock) {

        if (stack.is(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE)) {
            return new OptionalDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource pointer, ItemStack stack) {
                    ServerLevel world = pointer.level();
                    BlockPos targetPos = pointer.pos().relative(pointer.state().getValue(BlockStateProperties.FACING), 1);
                    BlockState targetState = world.getBlockState(targetPos);
                    this.setSuccess(true);

                    // if it's an impenduit without the power core, run this
                    if (targetState.getBlock() instanceof ImpenduitPylonBlock pylonBlock) {

                        if (!targetState.getValue(ImpenduitPylonBlock.POWER_SOURCE_PRESENT)) {
                            pylonBlock.insertPowerCore(world, targetPos);
                            stack.shrink(1);
                        } else {
                            this.setSuccess(false);
                        }

                        return stack;
                    } else {
                        return super.execute(pointer, stack);
                    }


                }
            };
        }

        if (stack.is(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)) {
            return new OptionalDispenseItemBehavior() {
                @Override
                protected ItemStack execute(BlockSource pointer, ItemStack stack) {
                    Level world = pointer.level();
                    BlockPos targetPos = pointer.pos().relative(pointer.state().getValue(BlockStateProperties.FACING), 1);
                    BlockState targetState = world.getBlockState(targetPos);

                    this.setSuccess(true);

                    if (targetState.getBlock() instanceof ImpenduitPylonBlock pylonBlock) {
                        if (targetState.getValue(ImpenduitPylonBlock.POWER_SOURCE_PRESENT)) {
                            pylonBlock.removePowerCore(world, targetPos, stack, null, null);
                        } else {
                            this.setSuccess(false);
                        }

                        return stack;
                    } else {

                        return super.execute(pointer, stack);
                    }

                }
            };
        }

        return null;
    }
}
