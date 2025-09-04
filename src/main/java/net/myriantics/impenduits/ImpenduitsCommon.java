package net.myriantics.impenduits;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import net.myriantics.impenduits.registry.ImpenduitsBlocks;
import net.myriantics.impenduits.registry.ImpenduitsItemGroups;
import net.myriantics.impenduits.registry.ImpenduitsItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpenduitsCommon implements ModInitializer {
	public static final String MOD_ID = "impenduits";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier locate(String name) {
		return Identifier.of(MOD_ID, name);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Impenduits!");

        ImpenduitsBlocks.init();
        ImpenduitsItems.init();
        ImpenduitsItemGroups.init();

		LOGGER.info("Impenduits has initialized!");
	}
}