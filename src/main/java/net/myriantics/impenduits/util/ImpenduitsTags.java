package net.myriantics.impenduits.util;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.myriantics.impenduits.ImpenduitsCommon;

import javax.swing.text.html.HTML;

public class ImpenduitsTags {
    public static final TagKey<Item> IMPENDUIT_PYLON_POWER_SOURCE =
            TagKey.of(RegistryKeys.ITEM, ImpenduitsCommon.locate("impenduit_pylon_power_source"));

    public static final TagKey<Item> IMPENDUIT_PYLON_POWER_SOURCE_REMOVER =
            TagKey.of(RegistryKeys.ITEM, ImpenduitsCommon.locate("impenduit_pylon_power_source_remover"));

    public static final TagKey<Block> IMPENDUIT_FIELD_BLOCK_REPLACEMENT_ALLOWLIST =
            TagKey.of(RegistryKeys.BLOCK, ImpenduitsCommon.locate("impenduit_field_block_replacement_allowlist"));

    public static final TagKey<Block> IMPENDUIT_FIELD_BLOCK_REPLACEMENT_DENYLIST =
            TagKey.of(RegistryKeys.BLOCK, ImpenduitsCommon.locate("impenduit_field_block_replacement_denylist"));

    public static final TagKey<Block> ENTITY_RAIN_MIMICKING_BLOCKS =
            TagKey.of(RegistryKeys.BLOCK, ImpenduitsCommon.locate("entity_rain_mimicking_blocks"));
}
