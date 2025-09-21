package net.myriantics.impenduits;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import net.myriantics.impenduits.registry.ImpenduitsGameRules;
import net.myriantics.impenduits.registry.ImpenduitsStatistics;
import net.myriantics.impenduits.registry.advancement.ImpenduitsAdvancementCriteria;
import net.myriantics.impenduits.registry.block.ImpenduitsBlockStateProperties;
import net.myriantics.impenduits.registry.block.ImpenduitsBlocks;
import net.myriantics.impenduits.registry.item.ImpenduitsItemGroups;
import net.myriantics.impenduits.registry.item.ImpenduitsItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpenduitsCommon implements ModInitializer {
	public static final String MOD_ID = "impenduits";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier locate(String name) {
		return Identifier.of(MOD_ID, name);
	}

    public static String locateAlt(String name) {
		return MOD_ID + "." + name;
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Impenduits!");

        // static inits
        ImpenduitsBlockStateProperties.init();

        // registrations
        ImpenduitsBlocks.init();
        ImpenduitsItems.init();
        ImpenduitsAdvancementCriteria.init();
        ImpenduitsGameRules.init();
        ImpenduitsStatistics.init();

        // static code
        ImpenduitsItemGroups.init();

		LOGGER.info("Impenduits has initialized!");
	}
}