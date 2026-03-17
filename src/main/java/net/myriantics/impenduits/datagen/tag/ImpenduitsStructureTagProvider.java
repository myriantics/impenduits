package net.myriantics.impenduits.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.myriantics.impenduits.tag.ImpenduitsStructureTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsStructureTagProvider extends FabricTagProvider<Structure> {
    public ImpenduitsStructureTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.STRUCTURE, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(ImpenduitsStructureTags.BONEMEAL_SPAWNS_CORAL_FANS_WITHIN)
                .forceAddTag(ImpenduitsStructureTags.INTACT_IMPENDUIT_SANCTUMS);
        getOrCreateTagBuilder(ImpenduitsStructureTags.IMPENDUIT_SANCTUMS)
                .forceAddTag(ImpenduitsStructureTags.INTACT_IMPENDUIT_SANCTUMS)
                .forceAddTag(ImpenduitsStructureTags.RUINED_IMPENDUIT_SANCTUMS);
    }
}
