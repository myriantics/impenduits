package net.myriantics.impenduits.blocks;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.datagen.ImpenduitsBlockInteractionLootTableProvider;
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

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);

        // power source is tag-driven instead of hardcoded in case some modpack wants to rework
        // theyd also have to change the output loot table to match
        if (handStack.isIn(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE)
                && !state.get(POWER_SOURCE_PRESENT)) {
            if (!world.isClient()) {
                insertPowerCore(world, pos);
                if (!player.isCreative()) {
                    handStack.decrement(1);
                }
            }
            return ActionResult.SUCCESS;
        } else if (handStack.isIn(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)
                && state.get(POWER_SOURCE_PRESENT)) {
            if (!world.isClient()) {
                removePowerCore(world, pos, handStack);
            }
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState pylonState, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pylonPos, BlockPos neighborPos) {
        Direction pylonFacing = pylonState.get(FACING);

        // only run this if the update comes from the pylon's emitter face
        if (!world.isClient() && pylonFacing.equals(direction) && !canSupportField(pylonState, neighborState)) {
            // turn off the entire pylon row, because it's in an invalid state
            deactivatePylonRow((World) world, pylonPos);
        }

        return super.getStateForNeighborUpdate(pylonState, direction, neighborState, world, pylonPos, neighborPos);
    }

    private static ArrayList<BlockPos> getAffectedPositions(World world, BlockState originState, BlockPos originPos) {
        ArrayList<BlockPos> affectedPositions = new ArrayList<>();
        int maxFieldColumnLength = MAX_IMPENDUIT_FIELD_SIZE;

        final Direction.Axis originPylonAxis = originState.get(AXIS);
        final boolean isSingleton = originState.get(FACING).getAxis().equals(originPylonAxis);

        Direction checkingDirection = Direction.from(originPylonAxis, Direction.AxisDirection.NEGATIVE);
        BlockPos pylonMutableCheckingSourcePos = originPos.mutableCopy();

        // tracks if the for loop has flipped directions or not - modified on first interruption, triggers loop exit on second.
        boolean flipped = false;

        for (int neighborOffset = 0; neighborOffset < MAX_IMPENDUIT_FIELD_SIZE; neighborOffset++) {
            BlockPos targetNeighboringPylonPos = pylonMutableCheckingSourcePos.offset(checkingDirection, neighborOffset);
            BlockState targetNeighborState = world.getBlockState(targetNeighboringPylonPos);

            if (areNeighboringPylonsCompatible(originState, targetNeighborState)) {

                ArrayList<BlockPos> fieldColumnPositions = getFieldColumnPositions(world, targetNeighboringPylonPos, originState, originPos, maxFieldColumnLength);

                // if we get an empty field column here, don't add anything else to the list
                if (!fieldColumnPositions.isEmpty()) {

                    // once we're sure that the target pylon is compatible and can spawn fields, we can add it to the list
                    affectedPositions.add(targetNeighboringPylonPos);

                    // add in the field and opposing pylon positions so they get updated as well - totally didn't forget to do this
                    affectedPositions.addAll(fieldColumnPositions);

                    // don't bother checking for adjacent pylons if the origin one is a singleton - slightly more performant
                    if (isSingleton) {
                        break;
                    }

                    // set the bounds that all field columns following the origin one must follow
                    // don't run this on anything but the origin column!
                    if (maxFieldColumnLength != fieldColumnPositions.size() - 1 && targetNeighboringPylonPos == originPos) {
                        maxFieldColumnLength = fieldColumnPositions.size() - 1;
                    }

                    // checks succeeded - don't run any of the below code
                    continue;
                }
            }

            // if any checks have failed, run the below processing to switch directions / break

            // don't switch directions more than once
            if (flipped) {
                break;
            }

            flipped = true;

            // as soon as you hit an incompatible pylon, flip checking direction around to the other side
            checkingDirection = checkingDirection.getOpposite();

            // walk target neighbor pos back towards the origin before resetting origin - also decrement neighbor offset
            // this serves to stop the deactivation from missing pylons that still need to get depowered
            pylonMutableCheckingSourcePos = targetNeighboringPylonPos.offset(checkingDirection, 1);
            neighborOffset--;

            // don't process further - will cause field to not form because it will try to form field off of invalid block - not good
        }

        return affectedPositions;
    }

    public static void deactivatePylonRow(World world, BlockPos originPos) {
        BlockState originState = world.getBlockState(originPos);

        // only check for pylons to disable if the pylon itself is powered - prevents recursive pointless checking
        if (originState.isOf(ImpenduitsCommon.IMPENDUIT_PYLON) && originState.get(POWERED)) {

            Direction.Axis originAxis = originState.get(AXIS);
            Direction checkingDirection = Direction.from(originAxis, Direction.AxisDirection.NEGATIVE);

            // tracks if the for loop has flipped directions or not - modified on first interruption, triggers loop exit on second.
            boolean flipped = false;

            // iterate through the row of pylons and unpower each one
            // the fields will deactivate themselves due to the block update
            for (int neighborOffset = 0; neighborOffset < MAX_IMPENDUIT_FIELD_SIZE; neighborOffset++) {
                BlockPos targetNeighborPos = originPos.offset(checkingDirection, neighborOffset);
                BlockState targetNeighborState = world.getBlockState(targetNeighborPos);

                // if pylon passes compatibility checks, set its state to powered
                if (areNeighboringPylonsCompatible(originState, targetNeighborState)
                        // explicitly check for powered pylons to prevent separate fields that share a pylon row from deactivating each other
                        && targetNeighborState.get(POWERED)) {
                    world.setBlockState(targetNeighborPos, world.getBlockState(targetNeighborPos).with(POWERED, false));
                    continue;
                }


                // don't switch directions more than once
                if (flipped) {
                    break;
                }

                flipped = true;

                // as soon as you hit an incompatible pylon, flip checking direction around to the other side
                checkingDirection = checkingDirection.getOpposite();

                // walk target neighbor pos back towards the origin before resetting origin - also decrement neighbor offset
                // this serves to stop the deactivation from missing pylons that still need to get depowered
                originPos = targetNeighborPos.offset(checkingDirection);
                neighborOffset--;
            }
        }
    }

    private static ArrayList<BlockPos> getFieldColumnPositions(World world, BlockPos targetNeighboringPylonPos, BlockState originState, BlockPos originPos, int boundedFieldLength) {
        // list used to store blockpos that need to be confirmed as supported before committing to the big list
        ArrayList<BlockPos> unconfirmedBlockPosList = new ArrayList<>();

        int facingDirOffset;

        // check all blocks in front of selected pylon
        for (facingDirOffset = 1; facingDirOffset <= boundedFieldLength; facingDirOffset++) {
            BlockPos targetPos = targetNeighboringPylonPos.offset(originState.get(FACING), facingDirOffset);
            BlockState targetState = world.getBlockState(targetPos);

            // we always want to add the target block to the list - if this operation fails for any reason it clears the list, so no reason not to
            unconfirmedBlockPosList.add(targetPos);

            if (ImpenduitFieldBlock.canFieldReplaceBlock(world, targetPos, targetState)) {

                // protect against fields stretching out to max length if unbounded
                if (facingDirOffset == boundedFieldLength) {
                    unconfirmedBlockPosList.clear();
                    break;
                }

            } else {

                // final check to see if field can actually form - if this fails, clear the whole list to indicate failure
                if (!areOpposingPylonsCompatible(originState, targetState)) {
                    unconfirmedBlockPosList.clear();
                }

                // if fields can't replace target state, end off the loop no matter what
                break;
            }
        }

        // if this returns an empty list, it is indicative of placement failure
        // contains both placed field positions and recieving pylon position - must differentiate when placing
        return unconfirmedBlockPosList;
    }

    private static void spawnForcefield(BlockState state, World world, BlockPos pos) {
        ArrayList<BlockPos> affectedPositions = getAffectedPositions(world, state, pos);

        Direction pylonFacing = state.get(FACING);

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
                // update replaced blocks with impenduit fields oriented on the axis of the facing direction of origin impenduit
                // this forces state to prevent field blocks from updating themselves while they're being placed  - i'm looking at you, redstone dust
                Block.dropStacks(world.getBlockState(updatedPos), world, updatedPos);
                world.setBlockState(updatedPos, ImpenduitsCommon.IMPENDUIT_FIELD.getDefaultState().with(AXIS, pylonFacing.getAxis())
                                .with(ImpenduitFieldBlock.FORMED, false),
                        Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
                // schedule field to be updated so that it changes its state - this breaks blocks such as sugarcane by letting them know they're in an invalid state
                world.scheduleBlockTick(updatedPos, ImpenduitsCommon.IMPENDUIT_FIELD, 1);
            }
        }
    }

    public static void insertPowerCore(World world, BlockPos pos) {
        BlockState pylonState = world.getBlockState(pos);

        world.updateComparators(pos, ImpenduitsCommon.IMPENDUIT_PYLON);
        world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS);
        world.setBlockState(pos, pylonState.with(POWER_SOURCE_PRESENT, true));
        spawnForcefield(pylonState.cycle(POWER_SOURCE_PRESENT), world, pos);
    }

    public static void removePowerCore(World world, BlockPos pos, ItemStack usedStack) {
        BlockState pylonState = world.getBlockState(pos);

        world.updateComparators(pos, ImpenduitsCommon.IMPENDUIT_PYLON);
        world.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS);
        world.setBlockState(pos, pylonState.with(POWER_SOURCE_PRESENT, false));

        if (world instanceof ServerWorld serverWorld) {
            Identifier lootTableId = ImpenduitsBlockInteractionLootTableProvider.locatePylonPowerCoreRemovalId(pylonState.getBlock());

            ObjectArrayList<ItemStack> outputStacks = serverWorld.getServer().getLootManager().getLootTable(lootTableId)
                    .generateLoot(new LootContextParameterSet.Builder(serverWorld)
                            .add(LootContextParameters.TOOL, usedStack)
                            .add(LootContextParameters.BLOCK_STATE, pylonState)
                            .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                            .build(LootContextTypes.BLOCK));

            for (ItemStack stack : outputStacks) {
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }

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


    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    // called in PistonBlockMixin
    public static PistonBehavior getPistonBehaviorFromState(BlockState pylonState) {
        // you're only allowed to push it if it's not part of an active field
        return pylonState.get(POWERED) ? PistonBehavior.BLOCK : PistonBehavior.NORMAL;
    }

    // if it's a pylon, it's powered, and it's facing the right way, it's safe to assume that it can support a field
    public static boolean canSupportField(BlockState state, BlockState fieldState) {
        return state.isOf(ImpenduitsCommon.IMPENDUIT_PYLON) && state.get(POWERED) && fieldState.isOf(ImpenduitsCommon.IMPENDUIT_FIELD) && state.get(FACING).getAxis().equals(fieldState.get(AXIS));
    }

    private static boolean areNeighboringPylonsCompatible(BlockState sourcePylon, BlockState targetPylon) {
        return targetPylon.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)
                && sourcePylon.get(AXIS).equals(targetPylon.get(AXIS))
                && sourcePylon.get(FACING).equals(targetPylon.get(FACING));
    }

    private static boolean areOpposingPylonsCompatible(BlockState sourcePylon, BlockState targetPylon) {
        return targetPylon.isOf(ImpenduitsCommon.IMPENDUIT_PYLON)
                && sourcePylon.get(AXIS).equals(targetPylon.get(AXIS))
                && sourcePylon.get(FACING).equals(targetPylon.get(FACING).getOpposite());
    }
}
