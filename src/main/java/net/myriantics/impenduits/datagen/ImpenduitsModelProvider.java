package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.blocks.ImpenduitPylonBlock;

public class ImpenduitsModelProvider extends FabricModelProvider {
    public ImpenduitsModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        registerImpenduitPylonBlock(blockStateModelGenerator);
        registerImpenduitFieldBlock(blockStateModelGenerator);
    }

    private void registerImpenduitFieldBlock(BlockStateModelGenerator generator) {
        BlockStateSupplier supplier = BlockStateModelGenerator.createAxisRotatedBlockState(ImpenduitsCommon.IMPENDUIT_FIELD, ImpenduitsCommon.locate("block/impenduit_field"));

        generator.blockStateCollector.accept(supplier);
    }

    private void registerImpenduitPylonBlock(BlockStateModelGenerator generator) {
        BlockStateVariant singleton_powered_core = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_singleton_powered_core"));
        BlockStateVariant singleton_unpowered_core = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_singleton_unpowered_core"));
        BlockStateVariant singleton_powered_no_core = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_singleton_powered_no_core"));
        BlockStateVariant singleton_unpowered_no_core = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_singleton_unpowered_no_core"));
        BlockStateVariant axis_powered_no_core_z = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_axis_powered_no_core_z"));
        BlockStateVariant axis_unpowered_no_core_z = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_axis_unpowered_no_core_z"));
        BlockStateVariant axis_powered_core_z = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_axis_powered_core_z"));
        BlockStateVariant axis_unpowered_core_z = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_axis_unpowered_core_z"));
        BlockStateVariant axis_powered_no_core_x = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_axis_powered_no_core_x"));
        BlockStateVariant axis_unpowered_no_core_x = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_axis_unpowered_no_core_x"));
        BlockStateVariant axis_powered_core_x = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_axis_powered_core_x"));
        BlockStateVariant axis_unpowered_core_x = BlockStateVariant.create().put(VariantSettings.MODEL, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_axis_unpowered_core_x"));

        generator.registerParentedItemModel(ImpenduitsCommon.IMPENDUIT_PYLON, getNestedBlockSubModelId(ImpenduitsCommon.IMPENDUIT_PYLON, "_axis_unpowered_no_core_z"));

        BlockStateVariantMap.QuadrupleProperty<Direction, Direction.Axis, Boolean, Boolean> map = BlockStateVariantMap.create(ImpenduitPylonBlock.FACING, ImpenduitPylonBlock.AXIS, ImpenduitPylonBlock.POWERED, ImpenduitPylonBlock.POWER_SOURCE_PRESENT);

        VariantsBlockStateSupplier supplier = VariantsBlockStateSupplier.create(ImpenduitsCommon.IMPENDUIT_PYLON);

        // iterate through all facing values
        for (Direction facing : Direction.values()) {
            Direction.Axis facingAxis = facing.getAxis();
            // iterate through all axes
            for (Direction.Axis axis : Direction.Axis.values()) {
                if (axis == facingAxis) {
                    map.register(facing, axis, false, false, BlockStateVariant.union(impenduitFromOrientation(facing, axis), singleton_unpowered_no_core))
                            .register(facing, axis, false, true, BlockStateVariant.union(impenduitFromOrientation(facing, axis), singleton_unpowered_core))
                            .register(facing, axis, true, false, BlockStateVariant.union(impenduitFromOrientation(facing, axis), singleton_powered_no_core))
                            .register(facing, axis, true, true, BlockStateVariant.union(impenduitFromOrientation(facing, axis), singleton_powered_core));
                } else {
                    boolean shouldRotate = shouldRotate(facing, axis);
                    map.register(facing, axis, false, false, BlockStateVariant.union(impenduitFromOrientation(facing, axis), shouldRotate ? axis_unpowered_no_core_z : axis_unpowered_no_core_x))
                            .register(facing, axis, false, true, BlockStateVariant.union(impenduitFromOrientation(facing, axis), shouldRotate ? axis_unpowered_core_z : axis_unpowered_core_x))
                            .register(facing, axis, true, false, BlockStateVariant.union(impenduitFromOrientation(facing, axis), shouldRotate ? axis_powered_no_core_z : axis_powered_no_core_x))
                            .register(facing, axis, true, true, BlockStateVariant.union(impenduitFromOrientation(facing, axis), shouldRotate ? axis_powered_core_z : axis_powered_core_x));
                }
            }
        }

        supplier.coordinate(map);

        generator.blockStateCollector.accept(supplier);

    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

    }

    private static Identifier getNestedBlockSubModelId(Block block, String suffix) {
        Identifier identifier = Registries.BLOCK.getId(block);
        return identifier.withPath((path) -> {
            return "block/" + identifier.getPath() + "/" + path + suffix;
        });
    }

    private static BlockStateVariant impenduitFromOrientation(Direction facing, Direction.Axis axis) {

        BlockStateVariant base = BlockStateVariant.create();

        switch(facing) {
            case UP -> {
                return base.put(VariantSettings.Y, VariantSettings.Rotation.R90);
            }
            case DOWN -> {
                return base.put(VariantSettings.Y, VariantSettings.Rotation.R90).put(VariantSettings.X, VariantSettings.Rotation.R180);
            }
            case NORTH -> {
                return base.put(VariantSettings.X, VariantSettings.Rotation.R90);
            }
            case SOUTH -> {
                return base.put(VariantSettings.X, VariantSettings.Rotation.R270);

            }
            case EAST -> {
                return base.put(VariantSettings.X, VariantSettings.Rotation.R90).put(VariantSettings.Y, VariantSettings.Rotation.R90);
            }
            case WEST -> {
                return base.put(VariantSettings.X, VariantSettings.Rotation.R90).put(VariantSettings.Y, VariantSettings.Rotation.R270);
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
