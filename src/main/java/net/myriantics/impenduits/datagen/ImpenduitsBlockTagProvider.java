package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.myriantics.impenduits.registry.ImpenduitsBlocks;
import net.myriantics.impenduits.util.ImpenduitsTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ImpenduitsBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {

        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ImpenduitsBlocks.IMPENDUIT_PYLON);

        getOrCreateTagBuilder(BlockTags.IMPERMEABLE)
                .add(ImpenduitsBlocks.IMPENDUIT_FIELD);

        getOrCreateTagBuilder(ImpenduitsTags.ENTITY_RAIN_MIMICKING_BLOCKS)
                .add(ImpenduitsBlocks.IMPENDUIT_FIELD);
    }
}
