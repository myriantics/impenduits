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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ImpenduitsBlockInteractionLootTableProvider extends SimpleFabricLootTableProvider {
    public ImpenduitsBlockInteractionLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup, LootContextTypes.BLOCK);
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        generateImpenduitPylonPowerCoreRemovalLootTable(lootTableBiConsumer, ImpenduitsCommon.IMPENDUIT_PYLON, Items.HEART_OF_THE_SEA);
    }

    public void generateImpenduitPylonPowerCoreRemovalLootTable(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> exporter, Block pylon, Item powerCoreItem) {

        Identifier tableId = locatePylonPowerCoreRemovalId(pylon);

        exporter.accept(RegistryKey.of(RegistryKeys.LOOT_TABLE, tableId), LootTable.builder()
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
