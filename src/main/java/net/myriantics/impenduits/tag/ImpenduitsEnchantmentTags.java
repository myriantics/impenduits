package net.myriantics.impenduits.tag;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsEnchantmentTags {
    /**
     * Defines the enchantments that allow you to walk on Impenduit Fields.
     */
    public static final TagKey<Enchantment> IMPENDUIT_FIELD_WALKABLE_ENCHANTMENTS = createTag("impenduit_field_walkable");

    private static TagKey<Enchantment> createTag(String name) {
        return TagKey.of(RegistryKeys.ENCHANTMENT, ImpenduitsCommon.locate(name));
    }
}
