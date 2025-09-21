package net.myriantics.impenduits.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Items;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.ItemTags;
import net.myriantics.impenduits.tag.ImpenduitsItemTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ImpenduitsItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE)
                .add(Items.HEART_OF_THE_SEA);

        getOrCreateTagBuilder(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)
                .forceAddTag(ItemTags.PICKAXES);
    }
}
