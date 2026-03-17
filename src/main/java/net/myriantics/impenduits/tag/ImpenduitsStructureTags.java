package net.myriantics.impenduits.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsStructureTags {
    public static TagKey<Structure> BONEMEAL_SPAWNS_CORAL_FANS_WITHIN = createTag("bonemeal_spawns_coral_fans_within");
    public static TagKey<Structure> IMPENDUIT_SANCTUMS = createTag("impenduit_sanctums");
    public static TagKey<Structure> RUINED_IMPENDUIT_SANCTUMS = createTag("ruined_impenduit_sanctums");
    public static TagKey<Structure> INTACT_IMPENDUIT_SANCTUMS = createTag("intact_impenduit_sanctums");

    private static TagKey<Structure> createTag(String name) {
        return TagKey.create(Registries.STRUCTURE, ImpenduitsCommon.locate(name));
    }
}
