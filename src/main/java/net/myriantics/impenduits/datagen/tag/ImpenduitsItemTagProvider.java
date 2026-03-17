package net.myriantics.impenduits.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.myriantics.impenduits.tag.ImpenduitsItemTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ImpenduitsItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        getOrCreateTagBuilder(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE)
                .add(Items.HEART_OF_THE_SEA);

        getOrCreateTagBuilder(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)
                .forceAddTag(ItemTags.PICKAXES);
    }
}
