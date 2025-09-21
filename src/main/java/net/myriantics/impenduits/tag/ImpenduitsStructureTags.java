package net.myriantics.impenduits.tag;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.gen.structure.Structure;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsStructureTags {
    public static TagKey<Structure> BONEMEAL_SPAWNS_CORAL_FANS_WITHIN = createTag("bonemeal_spawns_coral_fans_within");
    public static TagKey<Structure> IMPENDUIT_SANCTUMS = createTag("impenduit_sanctums");
    public static TagKey<Structure> RUINED_IMPENDUIT_SANCTUMS = createTag("ruined_impenduit_sanctums");
    public static TagKey<Structure> INTACT_IMPENDUIT_SANCTUMS = createTag("intact_impenduit_sanctums");

    private static TagKey<Structure> createTag(String name) {
        return TagKey.of(RegistryKeys.STRUCTURE, ImpenduitsCommon.locate(name));
    }
}
