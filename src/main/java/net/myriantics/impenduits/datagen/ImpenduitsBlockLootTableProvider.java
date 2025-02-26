package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.*;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ImpenduitsBlockLootTableProvider extends FabricBlockLootTableProvider {
    public ImpenduitsBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
    }


    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> biConsumer) {
        buildImpenduitPylonLootTable(biConsumer, ImpenduitsCommon.IMPENDUIT_PYLON, Items.HEART_OF_THE_SEA);
    }

    public void buildImpenduitPylonLootTable(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> biConsumer, Block pylon, Item powerCore) {
        biConsumer.accept(pylon.getLootTableKey(), LootTable.builder()
                .pool(
                        LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(1.0F))
                                .with(
                                        this.applyExplosionDecay(
                                                pylon,
                                                ItemEntry.builder(pylon)
                                        )
                                )
                )
                .pool(
                        LootPool.builder()
                                .conditionally(BlockStatePropertyLootCondition.builder(pylon)
                                                .properties(StatePredicate.Builder.create().exactMatch(ImpenduitPylonBlock.POWER_SOURCE_PRESENT, true)))
                                .rolls(ConstantLootNumberProvider.create(1.0f))
                                .with(
                                        this.applyExplosionDecay(
                                                powerCore,
                                                ItemEntry.builder(powerCore))
                                )
                )
        );
    }
}
