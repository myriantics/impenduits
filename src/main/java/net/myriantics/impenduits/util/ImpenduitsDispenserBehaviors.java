package net.myriantics.impenduits.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;

public class ImpenduitsDispenserBehaviors {

    public static void registerDispenserBehaviors() {
        DispenserBlock.registerBehavior(Items.HEART_OF_THE_SEA, new FallibleItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                World world = pointer.getWorld();
                BlockPos targetPos = pointer.getPos().offset(pointer.getBlockState().get(Properties.FACING), 1);
                BlockState targetState = world.getBlockState(targetPos);
                this.setSuccess(true);

                // if it's an impenduit without the power core, run this
                if (targetState.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)) {

                    if (!targetState.get(ImpenduitPylonBlock.POWER_SOURCE_PRESENT)) {
                        ImpenduitPylonBlock.insertPowerCore(world, targetPos);
                        stack.decrement(1);
                    } else {
                        this.setSuccess(false);
                    }

                    return stack;
                } else {
                    return super.dispenseSilently(pointer, stack);
                }


            }
        });


        DispenserBehavior powerCoreRemoverBehavior = new FallibleItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                World world = pointer.getWorld();
                BlockPos targetPos = pointer.getPos().offset(pointer.getBlockState().get(Properties.FACING), 1);
                BlockState targetState = world.getBlockState(targetPos);

                this.setSuccess(true);

                if (targetState.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)) {
                    if (targetState.get(ImpenduitPylonBlock.POWER_SOURCE_PRESENT)) {
                        ImpenduitPylonBlock.removePowerCore(world, targetPos);
                        stack.damage(1, world.getRandom(), null);
                    } else {
                        this.setSuccess(false);
                    }

                    return stack;
                } else {

                    return super.dispenseSilently(pointer, stack);
                }

            }
        };

        // sadly dispensers dont support tags so these are hardcoded
        // maybe trigger an event on data reload that cycles through the tag but ill do that later
        DispenserBlock.registerBehavior(Items.WOODEN_PICKAXE, powerCoreRemoverBehavior);
        DispenserBlock.registerBehavior(Items.STONE_PICKAXE, powerCoreRemoverBehavior);
        DispenserBlock.registerBehavior(Items.GOLDEN_PICKAXE, powerCoreRemoverBehavior);
        DispenserBlock.registerBehavior(Items.IRON_PICKAXE, powerCoreRemoverBehavior);
        DispenserBlock.registerBehavior(Items.DIAMOND_PICKAXE, powerCoreRemoverBehavior);
        DispenserBlock.registerBehavior(Items.NETHERITE_PICKAXE, powerCoreRemoverBehavior);
    }
}
