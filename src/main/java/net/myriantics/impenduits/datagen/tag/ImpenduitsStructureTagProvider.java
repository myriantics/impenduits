package net.myriantics.impenduits.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.gen.structure.Structure;
import net.myriantics.impenduits.tag.ImpenduitsStructureTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsStructureTagProvider extends FabricTagProvider<Structure> {
    public ImpenduitsStructureTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.STRUCTURE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ImpenduitsStructureTags.BONEMEAL_SPAWNS_CORAL_FANS_WITHIN)
                .forceAddTag(ImpenduitsStructureTags.INTACT_IMPENDUIT_SANCTUMS);
        getOrCreateTagBuilder(ImpenduitsStructureTags.IMPENDUIT_SANCTUMS)
                .forceAddTag(ImpenduitsStructureTags.INTACT_IMPENDUIT_SANCTUMS)
                .forceAddTag(ImpenduitsStructureTags.RUINED_IMPENDUIT_SANCTUMS);
    }
}
