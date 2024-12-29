package net.myriantics.impenduits.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.myriantics.impenduits.ImpenduitsCommon;
import org.jetbrains.annotations.Nullable;

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

        // only do this on the client - also fixes wack ass crash that happened when player tried to load into world while looking at impenduit field
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().worldRenderer.getChunkBuilder() != null) {
            PlayerEntity player = MinecraftClient.getInstance().player;

            // the outline only shows up if your head is not in  an impenduit field block
            shouldGetShape = player != null && !world.getBlockState(BlockPos.ofFloored(player.getEyePos())).isOf(ImpenduitsCommon.IMPENDUIT_FIELD);
        } else  {
            ImpenduitsCommon.LOGGER.info("Ruh roh - this shouldn't be called on the server!");
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
        ImpenduitsCommon.LOGGER.info("State replaced!");
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos updaterPos, boolean notify) {

        // check for if the source block is a field serves to prevent against updates during field formation fucking with things.
        if (sourceBlock.equals(ImpenduitsCommon.IMPENDUIT_FIELD)) {
            Direction updateDirection = getDirectionFromAdjacentBlockPos(pos, updaterPos);
            Direction.Axis fieldAxis = state.get(AXIS);

            // only try to validate the field if the update was on the axis of the field
            if (updateDirection != null && fieldAxis.test(updateDirection)) {
                ImpenduitsCommon.LOGGER.info("Source Block: " + sourceBlock.getName());

                // we invert the update direction here because if we don't they'll criss-cross in the middle.
                BlockPos pylonPos = findSupportingPylonFromDirection(world, pos, updateDirection.getOpposite());

                /*if (pylonPos != null) {
                    boolean blue = updateDirection.getDirection().equals(Direction.AxisDirection.POSITIVE);
                    BlockState test = blue ? Blocks.CYAN_CONCRETE.getDefaultState() : Blocks.RED_CONCRETE.getDefaultState();


                    world.setBlockState(pylonPos, test);
                }*/
            }
        }

        ImpenduitsCommon.LOGGER.info("Direction: " + getDirectionFromAdjacentBlockPos(pos, updaterPos));
        super.neighborUpdate(state, world, pos, sourceBlock, updaterPos, notify);
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

    private @Nullable BlockPos findSupportingPylonFromDirection(World world, BlockPos fieldPos, Direction lookingDirection) {
        for (int i = 0; i < ImpenduitPylonBlock.MAX_IMPENDUIT_FIELD_SIZE; i++) {
            BlockPos targetPos = fieldPos.offset(lookingDirection, i);
            BlockState targetState = world.getBlockState(targetPos);

            // if it's another impenduit field, we don't care - only run this code when we've reached the end or an unexpected thing
            if (!targetState.isOf(ImpenduitsCommon.IMPENDUIT_FIELD)) {
                return ImpenduitPylonBlock.canSupportField(targetState) ? targetPos : null;
            }
        }

        return null;
    }
}
