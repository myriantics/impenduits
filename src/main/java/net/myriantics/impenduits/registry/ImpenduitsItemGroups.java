package net.myriantics.impenduits.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsItemGroups {

    static {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register((fabricItemGroupEntries -> fabricItemGroupEntries.add(ImpenduitsItems.IMPENDUIT_PYLON)));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((fabricItemGroupEntries -> fabricItemGroupEntries.add(ImpenduitsItems.IMPENDUIT_PYLON)));
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Applied Impenduits' Item Group Modifications!");
    }
}
