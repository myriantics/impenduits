package net.myriantics.impenduits.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.util.ImpenduitsTags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ImpenduitPylonBlock extends Block {
    public static final DirectionProperty FACING = Properties.FACING;
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    public static final BooleanProperty POWER_SOURCE_PRESENT = BooleanProperty.of("power_source_present");
    public static final BooleanProperty POWERED = Properties.POWERED;

    public static final int MAX_IMPENDUIT_FIELD_SIZE = 24;

    public ImpenduitPylonBlock(Settings settings) {
        super(settings);

        setDefaultState(this.getStateManager().getDefaultState()
                .with(FACING, Direction.UP)
                .with(AXIS, Direction.Axis.Y)
                .with(POWER_SOURCE_PRESENT, false)
                .with(POWERED, false));
    }

    private ArrayList<BlockPos> getAffectedPositions(BlockState state, World world, BlockPos pos) {
        ArrayList<BlockPos> affectedPositions = new ArrayList<>();
        int boundedFieldLength = MAX_IMPENDUIT_FIELD_SIZE;

        Direction neighborCheckingDirection = Direction.from(state.get(AXIS), Direction.AxisDirection.NEGATIVE);

        boolean switchedDirection = false;

        for (int axisDirOffset = 0; axisDirOffset < MAX_IMPENDUIT_FIELD_SIZE; axisDirOffset++) {
            BlockPos targetNeighborPos = pos.offset(neighborCheckingDirection, axisDirOffset);
            BlockState targetNeighborState = world.getBlockState(targetNeighborPos);

            // as soon as you hit an incompatible impenduit, flip checking direction around to the other side
            if (!areNeighboringImpenduitsCompatible(state, targetNeighborState)) {
                // don't switch directions more than once
                if (switchedDirection) {break;}

                switchedDirection = true;
                neighborCheckingDirection = neighborCheckingDirection.getOpposite();
                pos = targetNeighborPos;
                continue;
            }

            // if the target pos is a valid impenduit, add it to the activation list
            affectedPositions.add(targetNeighborPos);

            // list used to store blockpos that need to be confirmed as supported before committing to the big list
            ArrayList<BlockPos> unconfirmedBlockPosList = new ArrayList<>();

            int facingDirOffset;

            // check all blocks in front of selected impenduit
            for (facingDirOffset = 1; facingDirOffset <= boundedFieldLength; facingDirOffset++) {
                BlockPos targetPos = targetNeighborPos.offset(state.get(FACING), facingDirOffset);
                BlockState targetState = world.getBlockState(targetPos);

                unconfirmedBlockPosList.add(targetPos);

                // if impenduits can't replace target state, end off the loop
                if (!targetState.isReplaceable()) {
                    // cap off impenduit checking length at that of the origin, so that you dont get uneven bs
                    if (axisDirOffset == 0) {
                        boundedFieldLength = facingDirOffset;
                    }

                    break;
                }
            }

            // don't query the list if it's empty
            BlockPos potentialPylonPos = unconfirmedBlockPosList.isEmpty()
                    ? targetNeighborPos
                    : unconfirmedBlockPosList.get(unconfirmedBlockPosList.size() - 1);

            // check if the last element in the list was a compatible impenduit
            // also checks if impenduits are the same distance away from origin
            if (areOppositeImpenduitsCompatible(state, world.getBlockState(potentialPylonPos))
                    && facingDirOffset == boundedFieldLength) {
                // add uncommitted blockpos to affected positions
                affectedPositions.addAll(unconfirmedBlockPosList);
            } else {
                break;
            }
        }

        return affectedPositions;
    }

    private void spawnForcefield(BlockState state, World world, BlockPos pos) {
        ArrayList<BlockPos> affectedPos = getAffectedPositions(state, world, pos);

        // set impenduits to powered and purge them from list
        // list is copied so i'm not live editing the list i'm iterating through
        for (BlockPos impenduitPos : List.copyOf(affectedPos)) {
            if (world.getBlockState(impenduitPos).isOf(ImpenduitsCommon.IMPENDUIT_PYLON)) {
                affectedPos.remove(impenduitPos);
                world.setBlockState(impenduitPos, world.getBlockState(pos).with(POWERED, true));
            }
        }

        // update replaced blocks with quartz pillars oriented on the axis of the facing direction of origin impenduit
        for (BlockPos replacedPos : affectedPos) {
            world.setBlockState(replacedPos, Blocks.QUARTZ_PILLAR.getDefaultState().with(AXIS, state.get(FACING).getAxis()));
        }
    }

    private boolean areOppositeImpenduitsCompatible(BlockState sourceImpenduit, BlockState targetImpenduit) {
        return targetImpenduit.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)
                && sourceImpenduit.get(AXIS).equals(targetImpenduit.get(AXIS))
                && sourceImpenduit.get(FACING).equals(targetImpenduit.get(FACING).getOpposite());
    }

    private boolean areNeighboringImpenduitsCompatible(BlockState sourceImpenduit, BlockState targetImpenduit) {
        return targetImpenduit.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)
                && sourceImpenduit.get(AXIS).equals(targetImpenduit.get(AXIS))
                && sourceImpenduit.get(FACING).equals(targetImpenduit.get(FACING));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);

        // power source is tag-driven instead of hardcoded in case some modpack wants to rework
        // theyd also have to change the output loot table to match
        if (handStack.isIn(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE)
                && !state.get(POWER_SOURCE_PRESENT)) {

            if (!world.isClient()) {
                spawnForcefield(state, world, pos);
                world.playSound(player, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS);
                world.setBlockState(pos, state.cycle(POWER_SOURCE_PRESENT));
            }
            return ActionResult.SUCCESS;
        } else if (handStack.isIn(ItemTags.PICKAXES)
                && state.get(POWER_SOURCE_PRESENT)) {
            if (!world.isClient()) {
                world.playSound(player, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS);
                world.setBlockState(pos, state.cycle(POWER_SOURCE_PRESENT));
            }
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction.Axis axis = ctx.getSide().getAxis();
        Direction facing = ctx.getPlayerLookDirection().getOpposite();

        // null protection because yeah sure (intellij was complaining so i must oblige)
        return super.getPlacementState(ctx) == null
                ? getDefaultState().with(FACING, facing).with(AXIS, axis)
                : super.getPlacementState(ctx).with(FACING, facing).with(AXIS, axis);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, AXIS, POWER_SOURCE_PRESENT, POWERED);
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(POWER_SOURCE_PRESENT) ? 15 : 0;
    }
}
