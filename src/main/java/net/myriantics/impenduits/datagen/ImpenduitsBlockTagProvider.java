package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.myriantics.impenduits.registry.ImpenduitsBlocks;
import net.myriantics.impenduits.tag.ImpenduitsBlockTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ImpenduitsBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        // minecraft tags
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ImpenduitsBlocks.IMPENDUIT_PYLON);
        getOrCreateTagBuilder(BlockTags.IMPERMEABLE)
                .add(ImpenduitsBlocks.IMPENDUIT_FIELD);
        getOrCreateTagBuilder(BlockTags.DOES_NOT_BLOCK_HOPPERS)
                .add(ImpenduitsBlocks.IMPENDUIT_PYLON);

        // impenduits tags
        getOrCreateTagBuilder(ImpenduitsBlockTags.ENTITY_RAIN_MIMICKING_BLOCKS)
                .add(ImpenduitsBlocks.IMPENDUIT_FIELD);
        getOrCreateTagBuilder(ImpenduitsBlockTags.DIRECTIONAL_OUTPUT_DISABLING)
                .add(Blocks.HOPPER);
    }
}
