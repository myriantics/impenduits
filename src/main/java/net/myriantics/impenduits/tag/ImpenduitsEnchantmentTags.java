package net.myriantics.impenduits.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsEnchantmentTags {
    /**
     * Defines the enchantments that allow you to walk on Impenduit Fields.
     */
    public static final TagKey<Enchantment> IMPENDUIT_FIELD_WALKABLE_ENCHANTMENTS = createTag("impenduit_field_walkable");

    private static TagKey<Enchantment> createTag(String name) {
        return TagKey.create(Registries.ENCHANTMENT, ImpenduitsCommon.locate(name));
    }
}
