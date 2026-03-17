package net.myriantics.impenduits.blocks;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty POWER_SOURCE_PRESENT = ImpenduitsBlockStateProperties.POWER_SOURCE_PRESENT;
    public static final BooleanProperty POWERED = ImpenduitsBlockStateProperties.POWERED;

    public final ImpenduitFieldBlock FIELD_BLOCK;

    public ImpenduitPylonBlock(Properties settings, ImpenduitFieldBlock fieldBlock) {
        super(settings);

        // if someone wanted to register a different field block then this goes to them
        this.FIELD_BLOCK = fieldBlock;

        registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.UP)
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(POWER_SOURCE_PRESENT, false)
                .setValue(POWERED, false));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack handStack = player.getItemInHand(hand);

        Direction.Axis interactedAxis = hit.getDirection().getAxis();

        // bonk interactions if players are in adventure mode
        if (!player.getAbilities().mayBuild) return super.useItemOn(stack, state, world, pos, player, hand, hit);

        // potential other interaction method that requires adventure mode players to interact with core opening sides
        /* if (!player.getAbilities().allowModifyWorld && (interactedAxis.test(state.get(FACING)) || interactedAxis.equals(state.get(AXIS))))
            return super.onUseWithItem(stack, state, world, pos, player, hand, hit); */

        // power source is tag-driven instead of hardcoded in case some modpack wants to rework
        // theyd also have to change the output loot table to match
        if (handStack.is(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE) && !state.getValue(POWER_SOURCE_PRESENT)) {
            if (player instanceof ServerPlayer serverPlayer) {

                // pop field activation advancement if activation was successful
                if (insertPowerCore((ServerLevel) world, pos)) {
                    ImpenduitsAdvancementTriggers.triggerImpenduitFieldActivation(serverPlayer);
                    serverPlayer.awardStat(ImpenduitsStatistics.IMPENDUIT_FIELDS_ACTIVATED);
                }

                serverPlayer.awardStat(Stats.ITEM_USED.get(handStack.getItem()));
                serverPlayer.awardStat(ImpenduitsStatistics.IMPENDUIT_PYLON_POWER_CORES_INSERTED);
                // decrement power core stack if player is not creative
                if (!player.isCreative()) {
                    handStack.shrink(1);
                }
            }
            return ItemInteractionResult.SUCCESS;
        } else if (handStack.is(ImpenduitsItemTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)
                && state.getValue(POWER_SOURCE_PRESENT)) {
            if (player instanceof ServerPlayer serverPlayer) {
                // this needs a custom advancement trigger because the vanilla one is called after onUseWithItem and ignores the original state before interaction
                // it seemed like bad practice to call the vanilla one again right here so in goes the custom one.
                // we can trust this because POWERED is only true in survival play if the pylon is actively supporting an Impenduit Field.
                if (state.getValue(POWERED)) {
                    ImpenduitsAdvancementCriteria.IMPENDUIT_FIELD_DEACTIVATION.trigger(serverPlayer);
                    serverPlayer.awardStat(ImpenduitsStatistics.IMPENDUIT_FIELDS_DEACTIVATED);
                }

                serverPlayer.awardStat(Stats.ITEM_USED.get(handStack.getItem()));
                serverPlayer.awardStat(ImpenduitsStatistics.IMPENDUIT_PYLON_POWER_CORES_REMOVED);
                removePowerCore(world, pos, handStack, player, hit.getLocation());
            }
            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    public BlockState updateShape(BlockState pylonState, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pylonPos, BlockPos neighborPos) {
        Direction pylonFacing = pylonState.getValue(FACING);

        // only run this if the update comes from the pylon's emitter face
        if (!world.isClientSide() && pylonFacing.equals(direction) && !canSupportField(pylonState, neighborState)) {
            // turn off the entire pylon row, because it's in an invalid state
            deactivatePylonRow((Level) world, pylonPos);
        }

        return super.updateShape(pylonState, direction, neighborState, world, pylonPos, neighborPos);
    }

    private ArrayList<BlockPos> getAffectedPositions(ServerLevel serverWorld, BlockState originPylonState, BlockPos originPylonPos) {
        ArrayList<BlockPos> affectedPositions = new ArrayList<>();

        // these are stored separately because the max pylon column length cannot change
        // while the max field column length can change if the field is smaller than the max
        int maxPylonColumnLength = serverWorld.getGameRules().getInt(ImpenduitsGameRules.RULE_MAX_IMPENDUIT_FIELD_SIDE_LENGTH);
        int maxFieldColumnLength = maxPylonColumnLength;

        final Direction.Axis pylonColumnAxis = originPylonState.getValue(AXIS);

        // this being not null indicates that a pillar has failed to form
        // selectedPylonPos will now move by 1 block each iteration in this direction.
        Direction formationDirectionAfterFailure = null;
        boolean isOriginPylonSelected = true;
        BlockPos.MutableBlockPos selectedPylonPos = originPylonPos.mutable();

        for (
                int offset = 0;
                offset < maxPylonColumnLength;
                offset++
        ) {
            // figure out which direction to move the selected position
            Direction offsetDirection = formationDirectionAfterFailure == null
                    ? Direction.fromAxisAndDirection(pylonColumnAxis, offset % 2 == 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE)
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
                    selectedPylonPos.immutable(),
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

    public void deactivatePylonRow(Level world, BlockPos originPos) {
        BlockState originState = world.getBlockState(originPos);

        // only check for pylons to disable if the pylon itself is powered - prevents recursive pointless checking
        if (originState.is(this) && originState.getValue(POWERED)) {

            Direction.Axis originAxis = originState.getValue(AXIS);
            Direction checkingDirection = Direction.fromAxisAndDirection(originAxis, Direction.AxisDirection.NEGATIVE);

            // tracks if the for loop has flipped directions or not - modified on first interruption, triggers loop exit on second.
            boolean flipped = false;

            // iterate through the row of pylons and unpower each one
            // the fields will deactivate themselves due to the block update
            for (int neighborOffset = 0; neighborOffset <= world.getGameRules().getInt(ImpenduitsGameRules.RULE_MAX_IMPENDUIT_FIELD_SIDE_LENGTH); neighborOffset++) {
                BlockPos targetNeighborPos = originPos.relative(checkingDirection, neighborOffset);
                BlockState targetNeighborState = world.getBlockState(targetNeighborPos);

                // if pylon passes compatibility checks, set its state to powered
                if (areNeighboringPylonsCompatible(originState, targetNeighborState)
                        // explicitly check for powered pylons to prevent separate fields that share a pylon row from deactivating each other
                        && targetNeighborState.getValue(POWERED)) {
                    world.setBlockAndUpdate(targetNeighborPos, world.getBlockState(targetNeighborPos).setValue(POWERED, false));
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
                originPos = targetNeighborPos.relative(checkingDirection);
                neighborOffset--;
            }
        }
    }

    private @Nullable ArrayList<BlockPos> getFieldColumnPositions(Level world, BlockPos columnOriginPos, BlockState columnOriginState, int boundedFieldLength, boolean isOriginPylonSelected) {
        // list used to store column positions that need to be confirmed as supported before committing to the big list
        ArrayList<BlockPos> columnPositions = new ArrayList<>();

        // add the origin pylon to the list first because we know it's valid already
        columnPositions.add(columnOriginPos);

        // the opposing pylon is checked by this loop, so the field length must be bumped by 1
        for (int offset = 1; offset <= boundedFieldLength + 1; offset++) {
            BlockPos targetPos = columnOriginPos.relative(columnOriginState.getValue(FACING), offset);
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

    private boolean spawnForcefield(BlockState state, ServerLevel serverWorld, BlockPos pos) {
        List<BlockPos> affectedPositions = getAffectedPositions(serverWorld, state, pos);

        // if field formation failed, add origin pos so it gets updated
        if (affectedPositions.isEmpty()) {
            affectedPositions = List.of(pos);
        }

        Direction pylonFacing = state.getValue(FACING);

        // Prepare the field state - we only need to do this once for the whole field
        BlockState fieldState = FIELD_BLOCK.defaultBlockState()
                .setValue(AXIS, pylonFacing.getAxis())
                // This signifies the field is still forming, and temporarily disables field self-destruction on updates.
                .setValue(ImpenduitFieldBlock.FORMED, false);


        for (BlockPos updatedPos : affectedPositions) {
            BlockState updatedState = serverWorld.getBlockState(updatedPos);

            // filter out impenduit pylons so that they aren't turned to impenduit fields
            // the only blocks that will be in this list are the pylons and replaceable blocks, so this is a fine assumption to make.
            if (updatedState.is(this)) {
                // Prepare the pylon state.
                BlockState pylonState = updatedState
                        // only set it to powered if there is more than one element!
                        // if there's only one element, it's the sole source impenduit - which shouldn't be powered on if no field can be generated.
                        .setValue(POWERED, affectedPositions.size() > 1)
                        // update power source state if you need to, but don't overwrite it if it's already present
                        .setValue(POWER_SOURCE_PRESENT, updatedPos.equals(pos) || updatedState.getValue(POWER_SOURCE_PRESENT));

                // Replace the pylon state.
                serverWorld.setBlockAndUpdate(
                        updatedPos,
                        pylonState
                );

            } else {
                // Drop original block's loot.
                Block.dropResources(serverWorld.getBlockState(updatedPos), serverWorld, updatedPos);

                // Place the field.
                serverWorld.setBlockAndUpdate(
                        updatedPos,
                        fieldState
                );

                // Schedule field to be updated so that it changes its state, allowing it to be destroyed on neighbor update
                // This is a safety measure to prevent certain fucky blocks from destroying Impenduit Fields during the formation stage.
                // (ahem. redstone dust, i'm looking at you.)
                serverWorld.scheduleTick(updatedPos, fieldState.getBlock(), 1);
            }
        }

        return affectedPositions.size() > 1;
    }

    public boolean insertPowerCore(ServerLevel serverWorld, BlockPos pos) {
        BlockState pylonState = serverWorld.getBlockState(pos);

        // trip sculk sensors
        serverWorld.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(pylonState));

        serverWorld.updateNeighbourForOutputSignal(pos, this);
        serverWorld.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS);

        // the pylon already being powered indicates it's already part of an active impenduit field
        // thus, do not try to spawn one
        if (pylonState.getValue(POWERED)) {
            serverWorld.setBlockAndUpdate(pos, pylonState
                    .setValue(POWER_SOURCE_PRESENT, true)
            );

            // we did not spawn a forcefield
            return false;
        } else {
            // if we are to spawn a forcefield, return whether it succeeded or not
            // origin pylon state is updated in this method anyways
            return spawnForcefield(pylonState, serverWorld, pos);
        }
    }

    public void removePowerCore(Level world, BlockPos pylonPos, ItemStack usedStack, @Nullable Player player, @Nullable Vec3 manualInteractionPos) {
        BlockState pylonState = world.getBlockState(pylonPos);

        // trip sculk sensors
        world.gameEvent(GameEvent.BLOCK_CHANGE, pylonPos, GameEvent.Context.of(pylonState));

        world.playSound(null, pylonPos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS);
        world.setBlockAndUpdate(pylonPos, pylonState.setValue(POWER_SOURCE_PRESENT, false));
        world.updateNeighbourForOutputSignal(pylonPos, this);

        // make sure we're on the server and that we either don't have a player or the player isn't creative before dropping items
        // this is because the items dropping is annoying when testing in creative
        if (world instanceof ServerLevel serverWorld && (player == null || !player.isCreative())) {
            Vec3 pylonCenterPos = pylonPos.getCenter();

            Direction pylonFacing = pylonState.getValue(FACING);
            Direction.Axis pylonAxis = pylonState.getValue(AXIS);

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

                    BlockPos neighborPos = pylonPos.relative(direction);
                    BlockState neighborState = world.getBlockState(neighborPos);

                    // make sure neighbor position is unobstructed
                    if (neighborState.isFaceSturdy(world, neighborPos, direction.getOpposite())) {
                        continue;
                    }

                    // if the closest direction hasn't been set yet, or it's closer than the last direction's distance, overwrite it
                    if (closestDirection == null || closestDirection.step().distance(relativeInteractionPos) < direction.step().distance(relativeInteractionPos)) {
                        closestDirection = direction;
                    }
                }

                // once we've found the closest core slot direction to interaction point, set the output direction
                outputDirection = closestDirection;
            }

            ResourceLocation lootTableId = ImpenduitsBlockInteractionLootTableProvider.locatePylonPowerCoreRemovalId(pylonState.getBlock());

            ObjectArrayList<ItemStack> outputStacks = serverWorld.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableId))
                    .getRandomItems(new LootParams.Builder(serverWorld)
                            .withParameter(LootContextParams.TOOL, usedStack)
                            .withParameter(LootContextParams.BLOCK_STATE, pylonState)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pylonPos))
                            .create(LootContextParamSets.BLOCK));

            // drop all loot table outputs
            for (ItemStack stack : outputStacks) {
                if (outputDirection != null) {
                    dropStack(serverWorld, pylonCenterPos.relative(outputDirection, 0.7), new Vec3(outputDirection.step()).scale(1.5f / 20), stack);
                } else {
                    Containers.dropItemStack(serverWorld, pylonCenterPos.x(), pylonCenterPos.y(), pylonCenterPos.z(), stack);
                }
            }
        }

        deactivatePylonRow(world, pylonPos);
    }

    private void dropStack(ServerLevel serverWorld, Vec3 position, Vec3 velocity, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(serverWorld,
                position.x(),
                position.y(),
                position.z(),
                stack,
                velocity.x(),
                velocity.y(),
                velocity.z()
        );

        serverWorld.addFreshEntity(itemEntity);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction.Axis axis = ctx.getClickedFace().getAxis();
        Direction facing = ctx.getNearestLookingDirection().getOpposite();

        // null protection because yeah sure (intellij was complaining so i must oblige)
        return super.getStateForPlacement(ctx) == null
                ? defaultBlockState().setValue(FACING, facing).setValue(AXIS, axis)
                : super.getStateForPlacement(ctx).setValue(FACING, facing).setValue(AXIS, axis);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AXIS, POWER_SOURCE_PRESENT, POWERED);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return state.getValue(POWER_SOURCE_PRESENT) ? 15 : 0;
    }


    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    // called in PistonBlockMixin
    public PushReaction getPistonBehaviorFromState(BlockState pylonState) {
        // you're only allowed to push it if it's not part of an active field
        return pylonState.getValue(POWERED) ? PushReaction.BLOCK : PushReaction.NORMAL;
    }

    // if it's a pylon, it's powered, and it's facing the right way, it's safe to assume that it can support a field
    public boolean canSupportField(BlockState pylonState, BlockState fieldState) {
        return pylonState.is(this) && pylonState.getValue(POWERED) && fieldState.is(FIELD_BLOCK) && pylonState.getValue(FACING).getAxis().equals(fieldState.getValue(AXIS));
    }

    protected boolean areNeighboringPylonsCompatible(BlockState sourcePylon, BlockState targetPylon) {
        return targetPylon.is(this)
                && sourcePylon.getValue(AXIS).equals(targetPylon.getValue(AXIS))
                && sourcePylon.getValue(FACING).equals(targetPylon.getValue(FACING));
    }

    protected boolean areOpposingPylonsCompatible(BlockState sourcePylon, BlockState targetPylon) {
        return targetPylon.is(this)
                && sourcePylon.getValue(AXIS).equals(targetPylon.getValue(AXIS))
                && sourcePylon.getValue(FACING).equals(targetPylon.getValue(FACING).getOpposite());
    }
}
