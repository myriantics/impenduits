package net.myriantics.impenduits;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.myriantics.impenduits.datagen.*;
import net.myriantics.impenduits.datagen.loot_table.ImpenduitsBlockInteractionLootTableProvider;
import net.myriantics.impenduits.datagen.loot_table.ImpenduitsBlockLootTableProvider;
import net.myriantics.impenduits.datagen.tag.ImpenduitsBlockTagProvider;
import net.myriantics.impenduits.datagen.tag.ImpenduitsEnchantmentTagProvider;
import net.myriantics.impenduits.datagen.tag.ImpenduitsItemTagProvider;
import net.myriantics.impenduits.datagen.tag.ImpenduitsStructureTagProvider;

public class ImpenduitsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ImpenduitsBlockTagProvider::new);
		pack.addProvider(ImpenduitsItemTagProvider::new);
        pack.addProvider(ImpenduitsEnchantmentTagProvider::new);
        pack.addProvider(ImpenduitsStructureTagProvider::new);
        pack.addProvider(ImpenduitsModelProvider::new);
        pack.addProvider(ImpenduitsBlockLootTableProvider::new);
        pack.addProvider(ImpenduitsBlockInteractionLootTableProvider::new);
        pack.addProvider(ImpenduitsRecipeProvider::new);
        pack.addProvider(ImpenduitsAdvancementProvider::new);
	}
}
