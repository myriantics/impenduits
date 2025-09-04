package net.myriantics.impenduits.registry.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;

public abstract class ImpenduitsBlocks {
    public static final Block IMPENDUIT_FIELD = register("impenduit_field", new ImpenduitFieldBlock(
            AbstractBlock.Settings.copy(Blocks.GLASS)
                    .slipperiness(0.98f)
                    .noCollision()
                    .dropsNothing()
                    .hardness(-1.0f)
                    .pistonBehavior(PistonBehavior.BLOCK)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .luminance((state) -> 8)
    ));

    public static final Block IMPENDUIT_PYLON = register("impenduit_pylon", new ImpenduitPylonBlock(
                    AbstractBlock.Settings
                            .copy(Blocks.DARK_PRISMARINE)
                            .solid()
                            .sounds(BlockSoundGroup.STONE)
                            .luminance((state) -> state.get(ImpenduitPylonBlock.POWERED) ? 4 : 0)
                    ,(ImpenduitFieldBlock) IMPENDUIT_FIELD
    ));

    private static Block register(String name, Block block) {
        return Registry.register(Registries.BLOCK, ImpenduitsCommon.locate(name), block);
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Registered Impenduits' Blocks!");
    }
}
