package net.myriantics.impenduits;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.myriantics.impenduits.datagen.ImpenduitsBlockTagProvider;
import net.myriantics.impenduits.datagen.ImpenduitsItemTagProvider;
import net.myriantics.impenduits.datagen.ImpenduitsModelProvider;

public class ImpenduitsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ImpenduitsBlockTagProvider::new);
		pack.addProvider(ImpenduitsItemTagProvider::new);
		pack.addProvider(ImpenduitsModelProvider::new);
	}
}
