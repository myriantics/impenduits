package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.myriantics.impenduits.util.ImpenduitsTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ImpenduitsItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE)
                .add(Items.HEART_OF_THE_SEA);
    }
}
