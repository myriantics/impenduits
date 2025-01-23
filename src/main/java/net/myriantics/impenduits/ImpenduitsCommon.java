package net.myriantics.impenduits;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import net.myriantics.impenduits.datagen.ImpenduitsBlockInteractionLootTableProvider;
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

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(new ImpenduitsDispenserBehaviors());
		ServerLifecycleEvents.SERVER_STARTED.register(new ImpenduitsDispenserBehaviors());

		// pretty sure you can just register new pylon and field blocks and they'll work together

		IMPENDUIT_FIELD = Registry.register(Registries.BLOCK,
				locate("impenduit_field"),
				new ImpenduitFieldBlock(
						FabricBlockSettings.copyOf(Blocks.GLASS)
								.slipperiness(0.98f)
								.replaceable()
								.noCollision()
								.dropsNothing()
								.hardness(-1.0f)
								.pistonBehavior(PistonBehavior.BLOCK)
								.sounds(BlockSoundGroup.AMETHYST_BLOCK)
								.luminance(8)));
		IMPENDUIT_PYLON = Registry.register(Registries.BLOCK,
				locate("impenduit_pylon"),
				new ImpenduitPylonBlock(FabricBlockSettings
						.copyOf(Blocks.DARK_PRISMARINE)
						.solid()
						.sounds(BlockSoundGroup.STONE)
						.luminance((state) -> state.get(ImpenduitPylonBlock.POWERED) ? 4 : 0)
						,(ImpenduitFieldBlock) IMPENDUIT_FIELD));
		IMPENDUIT_PYLON_BLOCKITEM = Registry.register(Registries.ITEM,
				locate("impenduit_pylon"),
				new BlockItem(IMPENDUIT_PYLON, new FabricItemSettings()));

		// add pylon to creative tab groups
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register((fabricItemGroupEntries -> fabricItemGroupEntries.add(IMPENDUIT_PYLON)));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((fabricItemGroupEntries -> fabricItemGroupEntries.add(IMPENDUIT_PYLON)));

		// example of how to add new variants

		/*
		SCHMENDUIT_FIELD = Registry.register(Registries.BLOCK,
				locate("schmenduit_field"),
				new ImpenduitFieldBlock(FabricBlockSettings.copyOf(IMPENDUIT_FIELD)));
		SCHMENDUIT_PYLON = Registry.register(Registries.BLOCK,
				locate("schmenduit_pylon"),
				new ImpenduitPylonBlock(FabricBlockSettings.copyOf(IMPENDUIT_PYLON), (ImpenduitFieldBlock) SCHMENDUIT_FIELD));
		SCHMENDUIT_PYLON_BLOCKITEM = Registry.register(Registries.ITEM,
				locate("schmenduit_pylon"),
				new BlockItem(SCHMENDUIT_PYLON, new FabricItemSettings()));
		 */
		LOGGER.info("Impenduits has initialized!");
	}
}