package net.myriantics.impenduits.registry.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.registry.block.ImpenduitsBlocks;

public abstract class ImpenduitsItems {

    public static final Item IMPENDUIT_PYLON = register(
            "impenduit_pylon",
            new BlockItem(ImpenduitsBlocks.IMPENDUIT_PYLON, new Item.Properties())
    );

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ImpenduitsCommon.locate(name), item);
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Registered Impenduits' Items!");
    }
}
