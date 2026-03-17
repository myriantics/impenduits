package net.myriantics.impenduits.datagen.loot_table;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import net.myriantics.impenduits.registry.block.ImpenduitsBlocks;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ImpenduitsBlockLootTableProvider extends FabricBlockLootTableProvider {
    public ImpenduitsBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
    }


    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        buildImpenduitPylonLootTable(biConsumer, ImpenduitsBlocks.IMPENDUIT_PYLON, Items.HEART_OF_THE_SEA);
    }

    public void buildImpenduitPylonLootTable(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer, Block pylon, Item powerCore) {
        biConsumer.accept(pylon.getLootTable(), LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(
                                        this.applyExplosionDecay(
                                                pylon,
                                                LootItem.lootTableItem(pylon)
                                        )
                                )
                )
                .withPool(
                        LootPool.lootPool()
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(pylon)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ImpenduitPylonBlock.POWER_SOURCE_PRESENT, true)))
                                .setRolls(ConstantValue.exactly(1.0f))
                                .add(
                                        this.applyExplosionDecay(
                                                powerCore,
                                                LootItem.lootTableItem(powerCore))
                                )
                )
        );
    }
}
