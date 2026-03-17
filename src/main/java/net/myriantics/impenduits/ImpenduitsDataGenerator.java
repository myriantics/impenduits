package net.myriantics.impenduits;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.myriantics.impenduits.datagen.*;
import net.myriantics.impenduits.datagen.loot_table.ImpenduitsBlockInteractionLootTableProvider;
import net.myriantics.impenduits.datagen.loot_table.ImpenduitsBlockLootTableProvider;
import net.myriantics.impenduits.datagen.tag.*;

public class ImpenduitsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // tags
		pack.addProvider(ImpenduitsBlockTagProvider::new);
		pack.addProvider(ImpenduitsItemTagProvider::new);
        pack.addProvider(ImpenduitsEnchantmentTagProvider::new);
        pack.addProvider(ImpenduitsStructureTagProvider::new);
        pack.addProvider(ImpenduitsEntityTypeTagProvider::new);

        // model
        pack.addProvider(ImpenduitsModelProvider::new);

        // loot table
        pack.addProvider(ImpenduitsBlockLootTableProvider::new);
        pack.addProvider(ImpenduitsBlockInteractionLootTableProvider::new);

        // recipe
        pack.addProvider(ImpenduitsRecipeProvider::new);

        // advancement
        pack.addProvider(ImpenduitsAdvancementProvider::new);
	}
}
