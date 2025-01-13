package net.myriantics.impenduits;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
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

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(new ImpenduitsDispenserBehaviors());
		ServerLifecycleEvents.SERVER_STARTED.register(new ImpenduitsDispenserBehaviors());

		IMPENDUIT_PYLON = Registry.register(Registries.BLOCK,
				locate("impenduit_pylon"),
				new ImpenduitPylonBlock(FabricBlockSettings
						.copyOf(Blocks.DARK_PRISMARINE)
						.solid()
						.sounds(BlockSoundGroup.STONE)
						.luminance((state) -> state.get(ImpenduitPylonBlock.POWERED) ? 4 : 0)));
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
		IMPENDUIT_PYLON_BLOCKITEM = Registry.register(Registries.ITEM,
				locate("impenduit_pylon"),
				new BlockItem(IMPENDUIT_PYLON, new FabricItemSettings()));

		LOGGER.info("Impenduits has initialized!");
	}
}