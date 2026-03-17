package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;
import net.myriantics.impenduits.registry.block.ImpenduitsBlocks;

public class ImpenduitsModelProvider extends FabricModelProvider {
    public ImpenduitsModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
        registerImpenduitPylonBlock(blockStateModelGenerator);
        registerImpenduitFieldBlock(blockStateModelGenerator);
    }

    private void registerImpenduitFieldBlock(BlockModelGenerators generator) {
        BlockStateGenerator supplier = BlockModelGenerators.createAxisAlignedPillarBlock(ImpenduitsBlocks.IMPENDUIT_FIELD, ImpenduitsCommon.locate("block/impenduit_field"));

        generator.blockStateOutput.accept(supplier);
    }

    private void registerImpenduitPylonBlock(BlockModelGenerators generator) {
        Variant singleton_powered_core = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_singleton_powered_core"));
        Variant singleton_unpowered_core = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_singleton_unpowered_core"));
        Variant singleton_powered_no_core = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_singleton_powered_no_core"));
        Variant singleton_unpowered_no_core = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_singleton_unpowered_no_core"));
        Variant axis_powered_no_core_z = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_axis_powered_no_core_z"));
        Variant axis_unpowered_no_core_z = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_axis_unpowered_no_core_z"));
        Variant axis_powered_core_z = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_axis_powered_core_z"));
        Variant axis_unpowered_core_z = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_axis_unpowered_core_z"));
        Variant axis_powered_no_core_x = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_axis_powered_no_core_x"));
        Variant axis_unpowered_no_core_x = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_axis_unpowered_no_core_x"));
        Variant axis_powered_core_x = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_axis_powered_core_x"));
        Variant axis_unpowered_core_x = Variant.variant().with(VariantProperties.MODEL, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_axis_unpowered_core_x"));

        generator.delegateItemModel(ImpenduitsBlocks.IMPENDUIT_PYLON, getNestedBlockSubModelId(ImpenduitsBlocks.IMPENDUIT_PYLON, "_axis_unpowered_no_core_z"));

        PropertyDispatch.C4<Direction, Direction.Axis, Boolean, Boolean> map = PropertyDispatch.properties(ImpenduitPylonBlock.FACING, ImpenduitPylonBlock.AXIS, ImpenduitPylonBlock.POWERED, ImpenduitPylonBlock.POWER_SOURCE_PRESENT);

        MultiVariantGenerator supplier = MultiVariantGenerator.multiVariant(ImpenduitsBlocks.IMPENDUIT_PYLON);

        // iterate through all facing values
        for (Direction facing : Direction.values()) {
            Direction.Axis facingAxis = facing.getAxis();
            // iterate through all axes
            for (Direction.Axis axis : Direction.Axis.values()) {
                if (axis == facingAxis) {
                    map.select(facing, axis, false, false, Variant.merge(impenduitFromOrientation(facing, axis), singleton_unpowered_no_core))
                            .select(facing, axis, false, true, Variant.merge(impenduitFromOrientation(facing, axis), singleton_unpowered_core))
                            .select(facing, axis, true, false, Variant.merge(impenduitFromOrientation(facing, axis), singleton_powered_no_core))
                            .select(facing, axis, true, true, Variant.merge(impenduitFromOrientation(facing, axis), singleton_powered_core));
                } else {
                    boolean shouldRotate = shouldRotate(facing, axis);
                    map.select(facing, axis, false, false, Variant.merge(impenduitFromOrientation(facing, axis), shouldRotate ? axis_unpowered_no_core_z : axis_unpowered_no_core_x))
                            .select(facing, axis, false, true, Variant.merge(impenduitFromOrientation(facing, axis), shouldRotate ? axis_unpowered_core_z : axis_unpowered_core_x))
                            .select(facing, axis, true, false, Variant.merge(impenduitFromOrientation(facing, axis), shouldRotate ? axis_powered_no_core_z : axis_powered_no_core_x))
                            .select(facing, axis, true, true, Variant.merge(impenduitFromOrientation(facing, axis), shouldRotate ? axis_powered_core_z : axis_powered_core_x));
                }
            }
        }

        supplier.with(map);

        generator.blockStateOutput.accept(supplier);

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {

    }

    private static ResourceLocation getNestedBlockSubModelId(Block block, String suffix) {
        ResourceLocation identifier = BuiltInRegistries.BLOCK.getKey(block);
        return identifier.withPath((path) -> {
            return "block/" + identifier.getPath() + "/" + path + suffix;
        });
    }

    private static Variant impenduitFromOrientation(Direction facing, Direction.Axis axis) {

        Variant base = Variant.variant();

        switch(facing) {
            case UP -> {
                return base.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            }
            case DOWN -> {
                return base.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180);
            }
            case NORTH -> {
                return base.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
            }
            case SOUTH -> {
                return base.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);

            }
            case EAST -> {
                return base.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            }
            case WEST -> {
                return base.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            }
            default -> {
                return base;
            }
        }
    }

    private static boolean shouldRotate(Direction facing, Direction.Axis axis) {
        if (axis.test(facing)) {
            return false;
        }

        // model defaults to north axis so this says that it shouldnt rotate if its already aligned
        switch (facing) {
            case DOWN, UP, WEST, EAST -> {
                return !axis.equals(Direction.Axis.Z);
            }
            case NORTH, SOUTH -> {
                return axis.equals(Direction.Axis.Y);
            }
        }

        // should never happen
        return false;
    }
}
