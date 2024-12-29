package net.myriantics.impenduits.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.myriantics.impenduits.ImpenduitsCommon;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ImpenduitFieldBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;


    public ImpenduitFieldBlock(Settings settings) {
        super(settings);

        setDefaultState(this.getStateManager().getDefaultState()
                .with(AXIS, Direction.Axis.Y));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        boolean shouldGetShape = false;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        // only do this on the client
        if (player != null) {

            // note to self - blockviews are SHITE
            try {
                // the outline only shows up if your head isn't in the targeted block
                shouldGetShape = !player.getWorld().getBlockState(BlockPos.ofFloored(player.getEyePos())).isOf(ImpenduitsCommon.IMPENDUIT_FIELD);
            } catch (ArrayIndexOutOfBoundsException fuckingshitexception) {
                ImpenduitsCommon.LOGGER.warn("Shitass exception tried to trigger. I tried to fix this, but this janky hack should work to cover my bases.");
            }

        }

        return shouldGetShape ? super.getOutlineShape(state, world, pos, context) : VoxelShapes.empty();
    }

    @Override
    public boolean canMobSpawnInside(BlockState state) {
        return false;
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        PlayerEntity player = context.getPlayer();

        // you can only replace the field block if your head is inside one
        return player != null && context.getWorld().getBlockState(BlockPos.ofFloored(player.getEyePos())).isOf(ImpenduitsCommon.IMPENDUIT_FIELD);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos updaterPos, boolean notify) {

        Direction updateDirection = getDirectionFromAdjacentBlockPos(pos, updaterPos);
        Direction.Axis fieldAxis = state.get(AXIS);

        // only try to validate the field if the update was on the axis of the field
        if (updateDirection != null && fieldAxis.test(updateDirection)) {

            // we invert the update direction here because if we don't they'll criss-cross in the middle.
            validateColumn(world, pos, updateDirection.getOpposite());
        }

        super.neighborUpdate(state, world, pos, sourceBlock, updaterPos, notify);
    }

    public static void validateColumn(World world, BlockPos pos, Direction checkingDirection) {
        @Nullable ArrayList<BlockPos> columnPositions = getFieldColumnFromDirection(world, pos, checkingDirection);

        if (columnPositions != null) {
            // pylon or other interruption is last element in list
            BlockPos pylonPos = columnPositions.get(columnPositions.size() - 1);

            for (BlockPos fieldPos : columnPositions) {
                if (world.getBlockState(fieldPos).isOf(ImpenduitsCommon.IMPENDUIT_FIELD)) {
                    world.setBlockState(fieldPos, Blocks.AIR.getDefaultState());
                }
            }

            // let pylon know shits goin down
            if (world.getBlockState(pylonPos).isOf(ImpenduitsCommon.IMPENDUIT_PYLON)) {
                ImpenduitPylonBlock.deactivatePylonRow(world, pylonPos);
            }
        }
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    private @Nullable Direction getDirectionFromAdjacentBlockPos(BlockPos source, BlockPos satellite) {
        for (Direction direction : Direction.values()) {
            if (source.offset(direction, 1).equals(satellite)) {
                return direction;
            }
        }

        // return null if they're not adjacent
        return null;
    }

    private static @Nullable ArrayList<BlockPos> getFieldColumnFromDirection(World world, BlockPos fieldPos, Direction lookingDirection) {
        ArrayList<BlockPos> columnPositions = new ArrayList<>();

        for (int i = 0; i < ImpenduitPylonBlock.MAX_IMPENDUIT_FIELD_SIZE; i++) {
            BlockPos targetPos = fieldPos.offset(lookingDirection, i);
            BlockState targetState = world.getBlockState(targetPos);

            columnPositions.add(targetPos);

            // break out of the loop once we an unexpected block or incompatible field
            if (!areFieldsCompatible(world.getBlockState(fieldPos), targetState)) {
                break;
            }
        }

        return columnPositions;
    }

    private static boolean areFieldsCompatible(BlockState originField, BlockState otherField) {
        // field blockstates have to be identical to be compatible - they also are double checked to be field blocks
        return originField.isOf(ImpenduitsCommon.IMPENDUIT_FIELD) && originField.equals(otherField);
    }
}
