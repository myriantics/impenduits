package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;

import java.util.function.BiConsumer;

public class ImpenduitsBlockInteractionLootTableProvider extends SimpleFabricLootTableProvider {
    public ImpenduitsBlockInteractionLootTableProvider(FabricDataOutput output) {
        super(output, LootContextTypes.BLOCK);
    }

    @Override
    public void accept(BiConsumer<Identifier, LootTable.Builder> exporter) {
        generateImpenduitPylonPowerCoreRemovalLootTable(exporter, ImpenduitsCommon.IMPENDUIT_PYLON, Items.HEART_OF_THE_SEA);
    }

    public void generateImpenduitPylonPowerCoreRemovalLootTable(BiConsumer<Identifier, LootTable.Builder> exporter, Block pylon, Item powerCoreItem) {

        Identifier tableId = locatePylonPowerCoreRemovalId(pylon);

        exporter.accept(tableId, LootTable.builder()
                .pool(
                        LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(1.0f))
                                .with(
                                        ItemEntry.builder(powerCoreItem)
                                )
                )
        );
    }

    public static Identifier locatePylonPowerCoreRemovalId(Block pylon) {
        return locate(Registries.BLOCK.getId(pylon).getPath() + "_power_core");
    }

    public static Identifier locate(String name) {
        return ImpenduitsCommon.locate(name).withPrefixedPath("block_interaction/");
    }
}
