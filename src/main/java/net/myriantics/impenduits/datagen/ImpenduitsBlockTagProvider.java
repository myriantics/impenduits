package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.myriantics.impenduits.ImpenduitsCommon;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ImpenduitsBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {

        getOrCreateTagBuilder(TagKey.of(RegistryKeys.BLOCK, Identifier.of("create", "wrench_pickup")))
                .add(ImpenduitsCommon.IMPENDUIT_PYLON);
    }
}
