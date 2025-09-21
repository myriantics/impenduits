package net.myriantics.impenduits.blocks;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import net.myriantics.impenduits.datagen.loot_table.ImpenduitsBlockInteractionLootTableProvider;
import net.myriantics.impenduits.registry.ImpenduitsGameRules;
import net.myriantics.impenduits.registry.ImpenduitsStatistics;
import net.myriantics.impenduits.registry.advancement.ImpenduitsAdvancementCriteria;
import net.myriantics.impenduits.registry.advancement.ImpenduitsAdvancementTriggers;
import net.myriantics.impenduits.registry.block.ImpenduitsBlockStateProperties;
import net.myriantics.impenduits.tag.ImpenduitsItemTags;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public class ImpenduitPylonBlock extends Block {
    public static final DirectionProperty FACING = Properties.FACING;
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    public static final BooleanProperty POWER_SOURCE_PRESENT = ImpenduitsBlockStateProperties.POWER_SOURCE_PRESENT;
    public static final BooleanProperty POWERED = ImpenduitsBlockStateProperties.POWERED;

    public final ImpenduitFieldBlock FIELD_BLOCK;

    public ImpenduitPylonBlock(Settings settings, ImpenduitFieldBlock fieldBlock) {
        super(settings);

        // if someone wanted to register a different field block then this goes to them
        this.FIELD_BLOCK = fieldBlock;

        setDefaultState(this.getStateManager().getDefaultState()
                .with(FACING, Direction.UP)
                .with(AXIS, Direction.Axis.Y)
                .with(POWER_SOURCE_PRESENT, false)
                .with(POWERED, false));
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack handStack = player.getStackInHand(hand);

        Direction.Axis interactedAxis = hit.getSide().getAxis();

        // bonk interactions if players are in adventure mode
        if (!player.getAbilities().allowModifyWorld) return super.onUseWithItem(stack, state, world, pos, player, hand, hit);

        // potential other interaction method that requires adventure mode players to interact with core opening sides
        /* if (!player.getAbilities().allowModifyWorld && (interactedAxis.test(state.get(FACING)) || interactedAxis.equals(state.get(AXIS))))
            return super.onUseWithItem(stack, state, world, pos, player, hand, hit); */

        // power source is tag-driven instead of hardcoded in case some modpack wants to rework
        // theyd also have to change the output loot table to match
        if (handStack.isIn(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE) && !state.get(POWER_SOURCE_PRESENT)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {

                // pop field activation advancement if activation was successful
                if (insertPowerCore((ServerWorld) world, pos)) {
                    ImpenduitsAdvancementTriggers.triggerImpenduitFieldActivation(serverPlayer);
                    serverPlayer.incrementStat(ImpenduitsStatistics.IMPENDUIT_FIELDS_ACTIVATED);
                }

                serverPlayer.incrementStat(Stats.USED.getOrCreateStat(handStack.getItem()));
                serverPlayer.incrementStat(ImpenduitsStatistics.IMPENDUIT_PYLON_POWER_CORES_INSERTED);
                // decrement power core stack if player is not creative
                if (!player.isCreative()) {
                    handStack.decrement(1);
                }
            }
            return ItemActionResult.SUCCESS;
        } else if (handStack.isIn(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)
                && state.get(POWER_SOURCE_PRESENT)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                // this needs a custom advancement trigger because the vanilla one is called after onUseWithItem and ignores the original state before interaction
                // it seemed like bad practice to call the vanilla one again right here so in goes the custom one.
                // we can trust this because POWERED is only true in survival play if the pylon is actively supporting an Impenduit Field.
                if (state.get(POWERED)) {
                    ImpenduitsAdvancementCriteria.IMPENDUIT_FIELD_DEACTIVATION.trigger(serverPlayer);
                    serverPlayer.incrementStat(ImpenduitsStatistics.IMPENDUIT_FIELDS_DEACTIVATED);
                }

                serverPlayer.incrementStat(Stats.USED.getOrCreateStat(handStack.getItem()));
                serverPlayer.incrementStat(ImpenduitsStatistics.IMPENDUIT_PYLON_POWER_CORES_REMOVED);
                removePowerCore(world, pos, handStack, player, hit.getPos());
            }
            return ItemActionResult.SUCCESS;
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
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

    private ArrayList<BlockPos> getAffectedPositions(ServerWorld serverWorld, BlockState originPylonState, BlockPos originPylonPos) {
        ArrayList<BlockPos> affectedPositions = new ArrayList<>();

        // these are stored separately because the max pylon column length cannot change
        // while the max field column length can change if the field is smaller than the max
        int maxPylonColumnLength = serverWorld.getGameRules().getInt(ImpenduitsGameRules.RULE_MAX_IMPENDUIT_FIELD_SIDE_LENGTH);
        int maxFieldColumnLength = maxPylonColumnLength;

        final Direction.Axis pylonColumnAxis = originPylonState.get(AXIS);

        // this being not null indicates that a pillar has failed to form
        // selectedPylonPos will now move by 1 block each iteration in this direction.
        Direction formationDirectionAfterFailure = null;
        boolean isOriginPylonSelected = true;
        BlockPos.Mutable selectedPylonPos = originPylonPos.mutableCopy();

        for (
                int offset = 0;
                offset < maxPylonColumnLength;
                offset++
        ) {
            // figure out which direction to move the selected position
            Direction offsetDirection = formationDirectionAfterFailure == null
                    ? Direction.from(pylonColumnAxis, offset % 2 == 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE)
                    : formationDirectionAfterFailure;

            selectedPylonPos.move(
                    offsetDirection,
                    formationDirectionAfterFailure == null
                            // when it hasn't failed, we want to flip back and forth
                            ? offset
                            // when it has failed, we move by 1 block each time in the designated direction
                            : 1
            );

            // check to update origin pylon selection status.
            if (isOriginPylonSelected) {
                isOriginPylonSelected = selectedPylonPos.equals(originPylonPos);
            }

            BlockState selectedPylonState = serverWorld.getBlockState(selectedPylonPos);

            // Make sure the selected pylon is compatible with the origin pylon
            if (!areNeighboringPylonsCompatible(originPylonState, selectedPylonState)) {
                // Break out of the loop if we hit an error on both sides
                if (formationDirectionAfterFailure != null) {
                    break;
                } else {
                    // If we haven't failed before, define new formation direction, reset selected pos, and continue looping
                    formationDirectionAfterFailure = offsetDirection.getOpposite();
                    selectedPylonPos.move(offsetDirection.getOpposite(), offset);
                    // decrement the offset because the failed column shouldn't count towards total
                    offset--;
                    continue;
                }
            }

            // gather field column positions if all previous checks pass
            // null value or empty list indicates failure
            ArrayList<BlockPos> fieldColumnPositions = getFieldColumnPositions(
                    serverWorld,
                    selectedPylonPos.toImmutable(),
                    originPylonState,
                    maxFieldColumnLength,
                    isOriginPylonSelected
            );

            if (fieldColumnPositions != null && !fieldColumnPositions.isEmpty()) {
                // if we have collected positions, update the list to include them
                affectedPositions.addAll(fieldColumnPositions);

                // cap off field column length and update flipped position
                if (isOriginPylonSelected) {
                    maxFieldColumnLength = fieldColumnPositions.size() - 2;
                }
            } else {
                if (isOriginPylonSelected || formationDirectionAfterFailure != null) {
                    // End blockpos collection if column check fails on the origin pylon - or if it's already failed once
                    break;
                } else {
                    // If we haven't failed before, define new formation direction, reset selected pos, and continue looping
                    formationDirectionAfterFailure = offsetDirection.getOpposite();
                    selectedPylonPos.move(offsetDirection.getOpposite(), offset);
                    // decrement the offset because the failed column shouldn't count towards total
                    offset--;
                    continue;
                }
            }
        }

        // Return all the positions to be updated!
        return affectedPositions;
    }

    /*
    private ArrayList<BlockPos> getAffectedPositions(World world, BlockState originState, BlockPos originPos) {
        ArrayList<BlockPos> affectedPositions = new ArrayList<>();
        int maxFieldColumnLength = world.getGameRules().getInt(ImpenduitsGameRules.RULE_MAX_IMPENDUIT_FIELD_SIDE_LENGTH);

        final Direction.Axis originPylonAxis = originState.get(AXIS);
        final boolean isSingleton = originState.get(FACING).getAxis().equals(originPylonAxis);

        Direction checkingDirection = Direction.from(originPylonAxis, Direction.AxisDirection.NEGATIVE);
        BlockPos pylonMutableCheckingSourcePos = originPos.mutableCopy();

        // tracks if the for loop has flipped directions or not - modified on first interruption, triggers loop exit on second.
        boolean flipped = false;
        
        // stop having to recompute if the current pylon is the origin or not
        boolean isOriginPylonSelected = true;

        for (int neighborOffset = 0; neighborOffset < world.getGameRules().getInt(ImpenduitsGameRules.RULE_MAX_IMPENDUIT_FIELD_SIDE_LENGTH); neighborOffset++) {
            BlockPos targetNeighboringPylonPos = pylonMutableCheckingSourcePos.offset(checkingDirection, neighborOffset);
            BlockState targetNeighborState = world.getBlockState(targetNeighboringPylonPos);

            // update origin status with fancy short circuit statement
            isOriginPylonSelected = isOriginPylonSelected && targetNeighboringPylonPos.equals(originPos);

            if (areNeighboringPylonsCompatible(originState, targetNeighborState)) {

                ArrayList<BlockPos> fieldColumnPositions = getFieldColumnPositions(world, targetNeighboringPylonPos, originState, maxFieldColumnLength, isOriginPylonSelected);

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
                    if (isOriginPylonSelected) {
                        maxFieldColumnLength = fieldColumnPositions.size();
                    }

                    // checks succeeded - don't run any of the below code
                    continue;
                }
            }

            // if any checks have failed, run the below processing to switch directions / break

            // don't switch directions more than once
            // also break out of loop if it fails on the origin pylon - this is to prevent adjacent pylons from being powered on
            if (flipped || targetNeighboringPylonPos == originPos) {
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
    }*/

    public void deactivatePylonRow(World world, BlockPos originPos) {
        BlockState originState = world.getBlockState(originPos);

        // only check for pylons to disable if the pylon itself is powered - prevents recursive pointless checking
        if (originState.isOf(this) && originState.get(POWERED)) {

            Direction.Axis originAxis = originState.get(AXIS);
            Direction checkingDirection = Direction.from(originAxis, Direction.AxisDirection.NEGATIVE);

            // tracks if the for loop has flipped directions or not - modified on first interruption, triggers loop exit on second.
            boolean flipped = false;

            // iterate through the row of pylons and unpower each one
            // the fields will deactivate themselves due to the block update
            for (int neighborOffset = 0; neighborOffset <= world.getGameRules().getInt(ImpenduitsGameRules.RULE_MAX_IMPENDUIT_FIELD_SIDE_LENGTH); neighborOffset++) {
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

    private @Nullable ArrayList<BlockPos> getFieldColumnPositions(World world, BlockPos columnOriginPos, BlockState columnOriginState, int boundedFieldLength, boolean isOriginPylonSelected) {
        // list used to store column positions that need to be confirmed as supported before committing to the big list
        ArrayList<BlockPos> columnPositions = new ArrayList<>();

        // add the origin pylon to the list first because we know it's valid already
        columnPositions.add(columnOriginPos);

        // the opposing pylon is checked by this loop, so the field length must be bumped by 1
        for (int offset = 1; offset <= boundedFieldLength + 1; offset++) {
            BlockPos targetPos = columnOriginPos.offset(columnOriginState.get(FACING), offset);
            BlockState targetState = world.getBlockState(targetPos);

            // we always want to add the target block to the list - if this operation fails for any reason it clears the list, so no reason not to
            columnPositions.add(targetPos);

            if (FIELD_BLOCK.canFieldReplaceBlock(world, targetPos, targetState)) {
                // protect against fields stretching out to max length if unbounded
                if (offset > boundedFieldLength) {
                    return null;
                }

            } else {
                // the origin pylon (the one that had the power core inserted) defines the length of the whole field.
                // if any other pylon deviates from that, it's invalid and should be bonked.
                if (!isOriginPylonSelected && offset != boundedFieldLength + 1) {
                    return null;
                }

                // if the block that we've hit isn't replaceable and isn't a valid pylon, bonk the operation
                if (!areOpposingPylonsCompatible(columnOriginState, targetState)) {
                    return null;
                }

                // if this is triggered, we hit a compatible opposing pylon :)
                // return the positions - we won the game
                // contains origin pylon position, field positions, and mirrored pylon position - must differentiate when placing
                return columnPositions;
            }
        }

        // only should trigger if it overflowed or the max length is 0 or something
        return null;
    }

    private boolean spawnForcefield(BlockState state, ServerWorld serverWorld, BlockPos pos) {
        List<BlockPos> affectedPositions = getAffectedPositions(serverWorld, state, pos);

        // if field formation failed, add origin pos so it gets updated
        if (affectedPositions.isEmpty()) {
            affectedPositions = List.of(pos);
        }

        Direction pylonFacing = state.get(FACING);

        // Prepare the field state - we only need to do this once for the whole field
        BlockState fieldState = FIELD_BLOCK.getDefaultState()
                .with(AXIS, pylonFacing.getAxis())
                // This signifies the field is still forming, and temporarily disables field self-destruction on updates.
                .with(ImpenduitFieldBlock.FORMED, false);


        for (BlockPos updatedPos : affectedPositions) {
            BlockState updatedState = serverWorld.getBlockState(updatedPos);

            // filter out impenduit pylons so that they aren't turned to impenduit fields
            // the only blocks that will be in this list are the pylons and replaceable blocks, so this is a fine assumption to make.
            if (updatedState.isOf(this)) {
                // Prepare the pylon state.
                BlockState pylonState = updatedState
                        // only set it to powered if there is more than one element!
                        // if there's only one element, it's the sole source impenduit - which shouldn't be powered on if no field can be generated.
                        .with(POWERED, affectedPositions.size() > 1)
                        // update power source state if you need to, but don't overwrite it if it's already present
                        .with(POWER_SOURCE_PRESENT, updatedPos.equals(pos) || updatedState.get(POWER_SOURCE_PRESENT));

                // Replace the pylon state.
                serverWorld.setBlockState(
                        updatedPos,
                        pylonState
                );

            } else {
                // Drop original block's loot.
                Block.dropStacks(serverWorld.getBlockState(updatedPos), serverWorld, updatedPos);

                // Place the field.
                serverWorld.setBlockState(
                        updatedPos,
                        fieldState
                );

                // Schedule field to be updated so that it changes its state, allowing it to be destroyed on neighbor update
                // This is a safety measure to prevent certain fucky blocks from destroying Impenduit Fields during the formation stage.
                // (ahem. redstone dust, i'm looking at you.)
                serverWorld.scheduleBlockTick(updatedPos, fieldState.getBlock(), 1);
            }
        }

        return affectedPositions.size() > 1;
    }

    public boolean insertPowerCore(ServerWorld serverWorld, BlockPos pos) {
        BlockState pylonState = serverWorld.getBlockState(pos);

        // trip sculk sensors
        serverWorld.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(pylonState));

        serverWorld.updateComparators(pos, this);
        serverWorld.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS);

        // the pylon already being powered indicates it's already part of an active impenduit field
        // thus, do not try to spawn one
        if (pylonState.get(POWERED)) {
            serverWorld.setBlockState(pos, pylonState
                    .with(POWER_SOURCE_PRESENT, true)
            );

            // we did not spawn a forcefield
            return false;
        } else {
            // if we are to spawn a forcefield, return whether it succeeded or not
            // origin pylon state is updated in this method anyways
            return spawnForcefield(pylonState, serverWorld, pos);
        }
    }

    public void removePowerCore(World world, BlockPos pylonPos, ItemStack usedStack, @Nullable PlayerEntity player, @Nullable Vec3d manualInteractionPos) {
        BlockState pylonState = world.getBlockState(pylonPos);

        // trip sculk sensors
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pylonPos, GameEvent.Emitter.of(pylonState));

        world.playSound(null, pylonPos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS);
        world.setBlockState(pylonPos, pylonState.with(POWER_SOURCE_PRESENT, false));
        world.updateComparators(pylonPos, this);

        // make sure we're on the server and that we either don't have a player or the player isn't creative before dropping items
        // this is because the items dropping is annoying when testing in creative
        if (world instanceof ServerWorld serverWorld && (player == null || !player.isCreative())) {
            Vec3d pylonCenterPos = pylonPos.toCenterPos();

            Direction pylonFacing = pylonState.get(FACING);
            Direction.Axis pylonAxis = pylonState.get(AXIS);

            // output direction is null for dispenser-automated interactions
            @Nullable Direction outputDirection = null;

            // output direction is the closest power core slot to the clicked position for manual interactions.
            if (player != null && manualInteractionPos != null) {
                Vector3f relativeInteractionPos = pylonCenterPos.subtract(manualInteractionPos).toVector3f();

                Direction closestDirection = null;
                for (Direction direction : Direction.values()) {
                    // ignore any slots that aren't core output slots
                    if (direction.getAxis().equals(pylonFacing.getAxis()) || direction.getAxis().equals(pylonAxis)) {
                        continue;
                    }

                    BlockPos neighborPos = pylonPos.offset(direction);
                    BlockState neighborState = world.getBlockState(neighborPos);

                    // make sure neighbor position is unobstructed
                    if (neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite())) {
                        continue;
                    }

                    // if the closest direction hasn't been set yet, or it's closer than the last direction's distance, overwrite it
                    if (closestDirection == null || closestDirection.getUnitVector().distance(relativeInteractionPos) < direction.getUnitVector().distance(relativeInteractionPos)) {
                        closestDirection = direction;
                    }
                }

                // once we've found the closest core slot direction to interaction point, set the output direction
                outputDirection = closestDirection;
            }

            Identifier lootTableId = ImpenduitsBlockInteractionLootTableProvider.locatePylonPowerCoreRemovalId(pylonState.getBlock());

            ObjectArrayList<ItemStack> outputStacks = serverWorld.getServer().getReloadableRegistries().getLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableId))
                    .generateLoot(new LootContextParameterSet.Builder(serverWorld)
                            .add(LootContextParameters.TOOL, usedStack)
                            .add(LootContextParameters.BLOCK_STATE, pylonState)
                            .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pylonPos))
                            .build(LootContextTypes.BLOCK));

            // drop all loot table outputs
            for (ItemStack stack : outputStacks) {
                if (outputDirection != null) {
                    dropStack(serverWorld, pylonCenterPos.offset(outputDirection, 0.7), new Vec3d(outputDirection.getUnitVector()).multiply(1.5f / 20), stack);
                } else {
                    ItemScatterer.spawn(serverWorld, pylonCenterPos.getX(), pylonCenterPos.getY(), pylonCenterPos.getZ(), stack);
                }
            }
        }

        deactivatePylonRow(world, pylonPos);
    }

    private void dropStack(ServerWorld serverWorld, Vec3d position, Vec3d velocity, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(serverWorld,
                position.getX(),
                position.getY(),
                position.getZ(),
                stack,
                velocity.getX(),
                velocity.getY(),
                velocity.getZ()
        );

        serverWorld.spawnEntity(itemEntity);
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
    public PistonBehavior getPistonBehaviorFromState(BlockState pylonState) {
        // you're only allowed to push it if it's not part of an active field
        return pylonState.get(POWERED) ? PistonBehavior.BLOCK : PistonBehavior.NORMAL;
    }

    // if it's a pylon, it's powered, and it's facing the right way, it's safe to assume that it can support a field
    public boolean canSupportField(BlockState pylonState, BlockState fieldState) {
        return pylonState.isOf(this) && pylonState.get(POWERED) && fieldState.isOf(FIELD_BLOCK) && pylonState.get(FACING).getAxis().equals(fieldState.get(AXIS));
    }

    protected boolean areNeighboringPylonsCompatible(BlockState sourcePylon, BlockState targetPylon) {
        return targetPylon.isOf(this)
                && sourcePylon.get(AXIS).equals(targetPylon.get(AXIS))
                && sourcePylon.get(FACING).equals(targetPylon.get(FACING));
    }

    protected boolean areOpposingPylonsCompatible(BlockState sourcePylon, BlockState targetPylon) {
        return targetPylon.isOf(this)
                && sourcePylon.get(AXIS).equals(targetPylon.get(AXIS))
                && sourcePylon.get(FACING).equals(targetPylon.get(FACING).getOpposite());
    }
}
