package net.myriantics.impenduits.datagen.loot_table;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.registry.block.ImpenduitsBlocks;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ImpenduitsBlockInteractionLootTableProvider extends SimpleFabricLootTableProvider {
    public ImpenduitsBlockInteractionLootTableProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup, LootContextParamSets.BLOCK);
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        generateImpenduitPylonPowerCoreRemovalLootTable(lootTableBiConsumer, ImpenduitsBlocks.IMPENDUIT_PYLON, Items.HEART_OF_THE_SEA);
    }

    public void generateImpenduitPylonPowerCoreRemovalLootTable(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> exporter, Block pylon, Item powerCoreItem) {

        ResourceLocation tableId = locatePylonPowerCoreRemovalId(pylon);

        exporter.accept(ResourceKey.create(Registries.LOOT_TABLE, tableId), LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0f))
                                .add(
                                        LootItem.lootTableItem(powerCoreItem)
                                )
                )
        );
    }

    public static ResourceLocation locatePylonPowerCoreRemovalId(Block pylon) {
        return locate(BuiltInRegistries.BLOCK.getKey(pylon).getPath() + "_power_core");
    }

    public static ResourceLocation locate(String name) {
        return ImpenduitsCommon.locate(name).withPrefix("block_interaction/");
    }
}
