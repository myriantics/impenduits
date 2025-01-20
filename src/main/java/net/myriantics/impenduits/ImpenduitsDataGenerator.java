package net.myriantics.impenduits;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.myriantics.impenduits.datagen.*;

public class ImpenduitsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ImpenduitsBlockTagProvider::new);
		pack.addProvider(ImpenduitsItemTagProvider::new);
		pack.addProvider(ImpenduitsModelProvider::new);
		pack.addProvider(ImpenduitsBlockLootTableProvider::new);
		pack.addProvider(ImpenduitsBlockInteractionLootTableProvider::new);
	}
}
