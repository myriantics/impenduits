package net.myriantics.impenduits.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
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

    private static ArrayList<BlockPos> getAffectedPositions(BlockState state, World world, BlockPos pos) {
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

            // if the currently selected axis offset pos is a valid impenduit, add it to the affected pos list
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
            if (areParallelImpenduitsCompatible(state, world.getBlockState(potentialPylonPos))
                    && facingDirOffset == boundedFieldLength) {

                // add uncommitted blockpos to affected positions
                affectedPositions.addAll(unconfirmedBlockPosList);
            } else {
                break;
            }
        }

        return affectedPositions;
    }

    private boolean spawnForcefield(BlockState state, World world, BlockPos pos) {
        ArrayList<BlockPos> affectedPositions = getAffectedPositions(state, world, pos);

        ImpenduitsCommon.LOGGER.info("Affected Positions List Length: " + affectedPositions.size());

        boolean hasSpawnedForcefield = false;

        // update all blocks in the list accordingly
        for (BlockPos updatedPos : affectedPositions) {
            BlockState updatedState = world.getBlockState(updatedPos);

            // filter out impenduit pylons so that they aren't turned to impenduit fields
            // the only blocks that will be in this list are the pylons and replaceable blocks, so this is a fine assumption to make.
            if (updatedState.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)) {
                // update impenduit state to reflect it being powered
                world.setBlockState(updatedPos, updatedState
                        // only set it to powered if there is more than one element!
                        // if there's only one element, it's the sole source impenduit - which shouldn't be powered on if no field can be generated.
                        .with(POWERED, affectedPositions.size() > 1)
                        // update power source state if you need to, but don't overwrite it if it's already present
                        .with(POWER_SOURCE_PRESENT, updatedPos.equals(pos) || updatedState.get(POWER_SOURCE_PRESENT)));
            } else {
                // an impenduit field block was spawned, so update this to true to signify that an action was completed.
                hasSpawnedForcefield = true;

                // update replaced blocks with quartz pillars oriented on the axis of the facing direction of origin impenduit
                world.setBlockState(updatedPos, ImpenduitsCommon.IMPENDUIT_FIELD.getDefaultState().with(AXIS, state.get(FACING).getAxis()));
            }
        }

        // if actions were actually performed, it was a success! return true
        return hasSpawnedForcefield;
    }

    public static void validate(World world, BlockPos pos, @Nullable ArrayList<BlockPos> alertingColumnPositions) {
        // if the origin point isn't a pylon, feck off
        if (!world.getBlockState(pos).isOf(ImpenduitsCommon.IMPENDUIT_PYLON)) {
            return;
        }

        BlockState originState = world.getBlockState(pos);

        ArrayList<BlockPos> affectedPositions = getAffectedPositions(originState, world, pos);

        if (alertingColumnPositions != null) {
            affectedPositions.addAll(alertingColumnPositions);
        }

    }

    private static boolean areParallelImpenduitsCompatible(BlockState sourceImpenduit, BlockState targetImpenduit) {
        return targetImpenduit.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)
                && sourceImpenduit.get(AXIS).equals(targetImpenduit.get(AXIS))
                && sourceImpenduit.get(FACING).equals(targetImpenduit.get(FACING).getOpposite());
    }

    private static boolean areNeighboringImpenduitsCompatible(BlockState sourceImpenduit, BlockState targetImpenduit) {
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
                spawnForcefield(state.cycle(POWER_SOURCE_PRESENT), world, pos);
                world.playSound(player, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS);
                //world.setBlockState(pos, state.cycle(POWER_SOURCE_PRESENT));
            }
            return ActionResult.SUCCESS;
        } else if (handStack.isIn(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)
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

    // if it's a pylon, it's powered, and it's facing the right way, it's safe to assume that it can support a field
    public static boolean canSupportField(BlockState state, Direction lookingDirection) {
        return state.isOf(ImpenduitsCommon.IMPENDUIT_PYLON) && state.get(POWERED) && state.get(FACING).equals(lookingDirection.getOpposite());
    }
}
