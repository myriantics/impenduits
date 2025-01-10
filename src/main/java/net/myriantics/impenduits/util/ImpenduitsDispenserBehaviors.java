package net.myriantics.impenduits.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import net.myriantics.impenduits.mixin.DispenserBlockInvoker;

import java.util.Map;

public class ImpenduitsDispenserBehaviors implements ServerLifecycleEvents.EndDataPackReload, ServerLifecycleEvents.ServerStarted {

    @Override
    public void onServerStarted(MinecraftServer server) {
        ImpenduitsCommon.LOGGER.info("Loaded Impenduits' dispenser behaviors!");

        refreshDispenserBehaviors();
    }

    @Override
    public void endDataPackReload(MinecraftServer minecraftServer, LifecycledResourceManager lifecycledResourceManager, boolean b) {
        ImpenduitsCommon.LOGGER.info("Reloaded Impenduits' dispenser behaviors!");

        refreshDispenserBehaviors();
    }

    private void refreshDispenserBehaviors() {

        registerIngredientBehavior(Ingredient.fromTag(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE), new TransientFallibleDispenserBehavior() {
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

        registerIngredientBehavior(Ingredient.fromTag(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER), new TransientFallibleDispenserBehavior() {
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
        });
    }

    private void registerIngredientBehavior(Ingredient ingredient, DispenserBehavior behavior) {
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            DispenserBehavior existingBehavior = ((DispenserBlockInvoker) Blocks.DISPENSER).impenduits$invokeGetBehaviorForItem(stack);

            // don't overwrite previously existing dispenser behaviors that weren't written by this
            // also dont' overwrite identical behaviors
            if (!behavior.equals(existingBehavior) && canOverwriteDispenserBehavior(existingBehavior)) {
                DispenserBlock.registerBehavior(stack.getItem(), behavior);
            }
        }
    }


    // gotta love marker interface / class bullshit amirite haha
    public static boolean canOverwriteDispenserBehavior(DispenserBehavior behavior) {
        return behavior.getClass().equals(ItemDispenserBehavior.class) || behavior instanceof TransientDispenserBehavior;
    }

    public interface TransientDispenserBehavior {}

    public class TransientFallibleDispenserBehavior extends FallibleItemDispenserBehavior implements TransientDispenserBehavior {}
}
