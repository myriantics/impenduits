package net.myriantics.impenduits.tag;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsBlockTags {
    /**
     * Defines additional blocks that Impenduit Fields can replace, in addition to its automatic ones.
     */
    public static final TagKey<Block> IMPENDUIT_FIELD_BLOCK_REPLACEMENT_ALLOWLIST = createTag("impenduit_field_block_replacement_allowlist");
    /**
     * Defines blocks that Impenduit Fields CANNOT replace. This overrides automatic detection and the allowlist.
     */
    public static final TagKey<Block> IMPENDUIT_FIELD_BLOCK_REPLACEMENT_DENYLIST = createTag("impenduit_field_block_replacement_denylist");
    /**
     * Defines blocks that treat entities standing within them as being in rain.
     */
    public static final TagKey<Block> ENTITY_RAIN_MIMICKING_BLOCKS = createTag("entity_rain_mimicking_blocks");
    /**
     * Defines blocks that disable the Directional Output feature of Impenduit Pylons when placed underneath them.
     * By default, this tag contains Hoppers.
     */
    public static final TagKey<Block> DIRECTIONAL_OUTPUT_DISABLING = createTag("directional_output_disabling");

    private static TagKey<Block> createTag(String name) {
        return TagKey.of(RegistryKeys.BLOCK, ImpenduitsCommon.locate(name));
    }
}
