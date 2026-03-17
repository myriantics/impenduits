package net.myriantics.impenduits.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.myriantics.impenduits.tag.ImpenduitsEntityTypeTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsEntityTypeTagProvider extends FabricTagProvider<EntityType<?>> {
    public ImpenduitsEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ENTITY_TYPE, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        getOrCreateTagBuilder(ImpenduitsEntityTypeTags.IMPENDUIT_FIELDS_KILL_ON_CONTACT)
                .add(EntityType.FIREBALL)
                .add(EntityType.SMALL_FIREBALL)
                .add(EntityType.SNOW_GOLEM)
                .add(EntityType.SNOWBALL);
    }
}
