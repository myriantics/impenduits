package net.myriantics.impenduits.tag;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsItemTags {
    /**
     * Defines the items that can be used manually or via dispenser as a power source for an Impenduit Pylon.
     */
    public static final TagKey<Item> IMPENDUIT_PYLON_POWER_SOURCE = createTag("impenduit_pylon_power_source");
    /**
     * Defines the items that can be used manually or via dispenser to remove the power source for an Impenduit Pylon.
     */
    public static final TagKey<Item> IMPENDUIT_PYLON_POWER_SOURCE_REMOVER = createTag("impenduit_pylon_power_source_remover");

    private static TagKey<Item> createTag(String name) {
        return TagKey.of(RegistryKeys.ITEM, ImpenduitsCommon.locate(name));
    }
}
