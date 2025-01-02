package net.myriantics.impenduits.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
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

    private static ArrayList<BlockPos> getAffectedPositions(BlockState state, World world, BlockPos originPos) {
        ArrayList<BlockPos> affectedPositions = new ArrayList<>();
        int boundedFieldLength = MAX_IMPENDUIT_FIELD_SIZE;

        final ArrayList<BlockPos> pylonRowPositions = getPylonRowPositions(world, originPos);
        final Direction.Axis pylonAxis = state.get(AXIS);

        boolean obstructed = false;


        for (BlockPos targetNeighborPos : pylonRowPositions) {

            // if the forcefield spawning has been interrupted and the current element isn't the first flipped element, skip the current element
            if (obstructed && !targetNeighborPos.equals(originPos.offset(pylonAxis, 1))) {
                continue;
            } else if (targetNeighborPos.equals(originPos.offset(pylonAxis, 1))) {
                // don't skip any future elements after first flipped element is encountered - that's bad
                obstructed = false;
            }

            // list used to store blockpos that need to be confirmed as supported before committing to the big list
            ArrayList<BlockPos> unconfirmedBlockPosList = new ArrayList<>();

            int facingDirOffset;

            // check all blocks in front of selected impenduit
            for (facingDirOffset = 1; facingDirOffset <= boundedFieldLength; facingDirOffset++) {
                BlockPos targetPos = targetNeighborPos.offset(state.get(FACING), facingDirOffset);
                BlockState targetState = world.getBlockState(targetPos);

                unconfirmedBlockPosList.add(targetPos);

                // if impenduits can't replace target state, end off the loop
                if (!ImpenduitFieldBlock.canFieldReplaceBlock(world, targetPos, targetState)) {
                    // cap off impenduit checking length at that of the origin, so that you dont get uneven bs
                    if (targetNeighborPos.equals(originPos)) {
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
            if (areParallelPylonsCompatible(state, world.getBlockState(potentialPylonPos))
                    && facingDirOffset == boundedFieldLength) {

                // add uncommitted blockpos to affected positions
                affectedPositions.addAll(unconfirmedBlockPosList);

                // if the currently selected axis offset pos is a valid impenduit, add it to the affected pos list
                affectedPositions.add(targetNeighborPos);
            } else {
                // checks to see if the target pos comes before direction flip
                // this protects against weird buggy looking field placement
                if (targetNeighborPos.getComponentAlongAxis(pylonAxis) < originPos.offset(pylonAxis, 1).getComponentAlongAxis(pylonAxis)) {
                    obstructed = true;
                    continue;
                }

                break;
            }
        }

        return affectedPositions;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState pylonState, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pylonPos, BlockPos neighborPos) {
        Direction pylonFacing = pylonState.get(FACING);

        // only run this if the update comes from the pylon's field face
        if (!world.isClient() && pylonFacing.equals(direction) && !canSupportField(pylonState, neighborState)) {
            // turn off the entire pylon row, because it's in an invalid state
            deactivatePylonRow((World) world, pylonPos);
        }

        return super.getStateForNeighborUpdate(pylonState, direction, neighborState, world, pylonPos, neighborPos);
    }

    public static void deactivatePylonRow(World world, BlockPos originPos) {
        BlockState originState = world.getBlockState(originPos);

        // only check for pylons to disable if the pylon itself is powered - prevents recursive pointless checking
        if (originState.isOf(ImpenduitsCommon.IMPENDUIT_PYLON) && originState.get(POWERED)) {
            // iterate through the row of pylons and unpower each one
            // the fields will deactivate themselves due to the block update
            for (BlockPos neighborPos : getPylonRowPositions(world, originPos)) {
                world.setBlockState(neighborPos, world.getBlockState(neighborPos).with(POWERED, false));
            }
        }
    }

    private static ArrayList<BlockPos> getPylonRowPositions(World world, BlockPos originPos) {
        ArrayList<BlockPos> neighboringPylonList = new ArrayList<>();

        BlockState originState = world.getBlockState(originPos);
        if (originState.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)) {

            Direction.Axis originAxis = originState.get(AXIS);
            Direction checkingDirection = Direction.from(originAxis, Direction.AxisDirection.NEGATIVE);

            boolean flipped = false;

            for (int neighborOffset = 0; neighborOffset < MAX_IMPENDUIT_FIELD_SIZE; neighborOffset++) {
                BlockPos targetNeighborPos = originPos.offset(checkingDirection, neighborOffset);
                BlockState targetNeighborState = world.getBlockState(targetNeighborPos);

                // as soon as you hit an incompatible impenduit, flip checking direction around to the other side
                if (!areNeighboringPylonsCompatible(originState, targetNeighborState)) {
                    // don't switch directions more than once
                    if (flipped) {
                        break;
                    }

                    flipped = true;
                    checkingDirection = checkingDirection.getOpposite();
                    originPos = targetNeighborPos;
                    continue;
                }

                // add pylon to list if it passes checks
                neighboringPylonList.add(targetNeighborPos);
            }
        }

        return neighboringPylonList;
    }

    private static boolean spawnForcefield(BlockState state, World world, BlockPos pos) {
        ArrayList<BlockPos> affectedPositions = getAffectedPositions(state, world, pos);

        Direction pylonFacing = state.get(FACING);

        boolean hasSpawnedForcefield = false;

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
                                .with(POWER_SOURCE_PRESENT, updatedPos.equals(pos) || updatedState.get(POWER_SOURCE_PRESENT)),
                        Block.NOTIFY_LISTENERS | FORCE_STATE);

            } else {
                // an impenduit field block was spawned, so update this to true to signify that an action was completed.
                hasSpawnedForcefield = true;

                // update replaced blocks with impenduit fields oriented on the axis of the facing direction of origin impenduit
                // this only notifies listeners to prevent field blocks from updating themselves while they're being placed
                Block.dropStacks(world.getBlockState(updatedPos), world, updatedPos);
                world.setBlockState(updatedPos, ImpenduitsCommon.IMPENDUIT_FIELD.getDefaultState().with(AXIS, pylonFacing.getAxis()),
                        Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                // don't update blocks ahead of the current field in the line - this prevents fucky bullshit with stuff like redstone wire
                world.updateNeighborsExcept(updatedPos, ImpenduitsCommon.IMPENDUIT_PYLON, pylonFacing);
            }
        }

        // if actions were actually performed, it was a success! return true
        return hasSpawnedForcefield;
    }

    private static boolean areParallelPylonsCompatible(BlockState sourceImpenduit, BlockState targetImpenduit) {
        return targetImpenduit.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)
                && sourceImpenduit.get(AXIS).equals(targetImpenduit.get(AXIS))
                && sourceImpenduit.get(FACING).equals(targetImpenduit.get(FACING).getOpposite());
    }

    private static boolean areNeighboringPylonsCompatible(BlockState sourceImpenduit, BlockState targetImpenduit) {
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
                insertPowerCore(world, pos);
            }
            return ActionResult.SUCCESS;
        } else if (handStack.isIn(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)
                && state.get(POWER_SOURCE_PRESENT)) {
            if (!world.isClient()) {
                removePowerCore(world, pos);
                handStack.damage(1, player, (e) -> player.sendEquipmentBreakStatus(hand.equals(Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
            }
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    public static void insertPowerCore(World world, BlockPos pos) {
        BlockState pylonState = world.getBlockState(pos);

        world.updateComparators(pos, ImpenduitsCommon.IMPENDUIT_PYLON);
        world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS);
        world.setBlockState(pos, pylonState.with(POWER_SOURCE_PRESENT, true));
        spawnForcefield(pylonState.cycle(POWER_SOURCE_PRESENT), world, pos);
    }

    public static void removePowerCore(World world, BlockPos pos) {
        BlockState pylonState = world.getBlockState(pos);

        world.updateComparators(pos, ImpenduitsCommon.IMPENDUIT_PYLON);
        world.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS);
        world.setBlockState(pos, pylonState.with(POWER_SOURCE_PRESENT, false));
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.HEART_OF_THE_SEA));
        deactivatePylonRow(world, pos);
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

    // called in PistonBlockMixin
    public static PistonBehavior getPistonBehaviorFromState(BlockState pylonState) {
        // you're only allowed to push it if it's not part of an active field
        return pylonState.get(POWERED) ? PistonBehavior.BLOCK : PistonBehavior.NORMAL;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    // if it's a pylon, it's powered, and it's facing the right way, it's safe to assume that it can support a field
    public static boolean canSupportField(BlockState state, BlockState fieldState) {
        return state.isOf(ImpenduitsCommon.IMPENDUIT_PYLON) && state.get(POWERED) && fieldState.isOf(ImpenduitsCommon.IMPENDUIT_FIELD) && state.get(FACING).getAxis().equals(fieldState.get(AXIS));
    }
}
