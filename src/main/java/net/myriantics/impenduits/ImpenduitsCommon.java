package net.myriantics.impenduits;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import net.myriantics.impenduits.util.ImpenduitsDispenserBehaviors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpenduitsCommon implements ModInitializer {
	public static final String MOD_ID = "impenduits";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Block IMPENDUIT_PYLON = null;
	public static Block IMPENDUIT_FIELD = null;
	public static Item IMPENDUIT_PYLON_BLOCKITEM = null;

	public static Identifier locate(String name) {
		return Identifier.of(MOD_ID, name);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Impenduits!");

		IMPENDUIT_PYLON = Registry.register(Registries.BLOCK,
				locate("impenduit_pylon"),
				new ImpenduitPylonBlock(FabricBlockSettings.copyOf(Blocks.BLUE_ICE).solid()));
		IMPENDUIT_FIELD = Registry.register(Registries.BLOCK,
				locate("impenduit_field"),
				new ImpenduitFieldBlock(FabricBlockSettings.copyOf(Blocks.GLASS).replaceable().noCollision().dropsNothing().hardness(-1.0f).pistonBehavior(PistonBehavior.BLOCK)));
		IMPENDUIT_PYLON_BLOCKITEM = Registry.register(Registries.ITEM,
				locate("impenduit_pylon"),
				new BlockItem(IMPENDUIT_PYLON, new FabricItemSettings()));

		ImpenduitsDispenserBehaviors.registerDispenserBehaviors();

		LOGGER.info("Impenduits has initialized!");
	}
}