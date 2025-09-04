package net.myriantics.impenduits.registry;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsItems {

    public static final Item IMPENDUIT_PYLON = register(
            "impenduit_pylon",
            new BlockItem(ImpenduitsBlocks.IMPENDUIT_PYLON, new Item.Settings())
    );

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, ImpenduitsCommon.locate(name), item);
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Registered Impenduits' Items!");
    }
}
