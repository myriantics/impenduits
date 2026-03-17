package net.myriantics.impenduits.registry.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsItemGroups {

    static {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register((fabricItemGroupEntries -> fabricItemGroupEntries.accept(ImpenduitsItems.IMPENDUIT_PYLON)));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((fabricItemGroupEntries -> fabricItemGroupEntries.accept(ImpenduitsItems.IMPENDUIT_PYLON)));
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Applied Impenduits' Item Group Modifications!");
    }
}
