package net.myriantics.impenduits.registry.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;

public abstract class ImpenduitsBlocks {
    public static final Block IMPENDUIT_FIELD = register("impenduit_field", new ImpenduitFieldBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .friction(0.98f)
                    .noCollission()
                    .noLootTable()
                    .destroyTime(-1.0f)
                    .pushReaction(PushReaction.BLOCK)
                    .sound(SoundType.AMETHYST)
                    .lightLevel((state) -> 8)
    ));

    public static final Block IMPENDUIT_PYLON = register("impenduit_pylon", new ImpenduitPylonBlock(
                    BlockBehaviour.Properties
                            .ofFullCopy(Blocks.DARK_PRISMARINE)
                            .forceSolidOn()
                            .sound(SoundType.STONE)
                            .lightLevel((state) -> state.getValue(ImpenduitPylonBlock.POWERED) ? 4 : 0)
                    ,(ImpenduitFieldBlock) IMPENDUIT_FIELD
    ));

    private static Block register(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, ImpenduitsCommon.locate(name), block);
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Registered Impenduits' Blocks!");
    }
}
