package net.myriantics.impenduits.util;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.myriantics.impenduits.ImpenduitsCommon;

public class ImpenduitsTags {
    public static final TagKey<Item> IMPENDUIT_PYLON_POWER_SOURCE =
            TagKey.of(RegistryKeys.ITEM, ImpenduitsCommon.locate("impenduit_pylon_power_source"));
}
