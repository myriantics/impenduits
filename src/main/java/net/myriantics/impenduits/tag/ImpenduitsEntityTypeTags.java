package net.myriantics.impenduits.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsEntityTypeTags {
    public static final TagKey<EntityType<?>> IMPENDUIT_FIELDS_KILL_ON_CONTACT = of("impenduit_fields_kill_on_contact");

    private static TagKey<EntityType<?>> of(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ImpenduitsCommon.locate(name));
    }
}
