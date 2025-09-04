package net.myriantics.impenduits.blocks;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.context.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.datagen.ImpenduitsBlockInteractionLootTableProvider;
import net.myriantics.impenduits.registry.ImpenduitsBlockStateProperties;
import net.myriantics.impenduits.util.ImpenduitsTags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ImpenduitPylonBlock extends Block {
    public static final DirectionProperty FACING = Properties.FACING;
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    public static final BooleanProperty POWER_SOURCE_PRESENT = ImpenduitsBlockStateProperties.POWER_SOURCE_PRESENT;
    public static final BooleanProperty POWERED = ImpenduitsBlockStateProperties.POWERED;

    public static final int MAX_IMPENDUIT_FIELD_SIZE = 24;

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
        if (handStack.isIn(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE)
                && !state.get(POWER_SOURCE_PRESENT)) {
            if (!world.isClient()) {
                insertPowerCore(world, pos);
                if (!player.isCreative()) {
                    handStack.decrement(1);
                }
            }
            return ItemActionResult.SUCCESS;
        } else if (handStack.isIn(ImpenduitsTags.IMPENDUIT_PYLON_POWER_SOURCE_REMOVER)
                && state.get(POWER_SOURCE_PRESENT)) {
            if (!world.isClient()) {
                removePowerCore(world, pos, handStack, player, hit.getSide());
            }
            return ItemActionResult.SUCCESS;
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    // todo: add some functionality to this
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return super.rotate(state, rotation);
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

    private ArrayList<BlockPos> getAffectedPositions(World world, BlockState originState, BlockPos originPos) {
        ArrayList<BlockPos> affectedPositions = new ArrayList<>();
        int maxFieldColumnLength = MAX_IMPENDUIT_FIELD_SIZE;

        final Direction.Axis originPylonAxis = originState.get(AXIS);
        final boolean isSingleton = originState.get(FACING).getAxis().equals(originPylonAxis);

        Direction checkingDirection = Direction.from(originPylonAxis, Direction.AxisDirection.NEGATIVE);
        BlockPos pylonMutableCheckingSourcePos = originPos.mutableCopy();

        // tracks if the for loop has flipped directions or not - modified on first interruption, triggers loop exit on second.
        boolean flipped = false;
        
        // stop having to recompute if the current pylon is the origin or not
        boolean isOriginPylonSelected = true;

        for (int neighborOffset = 0; neighborOffset < MAX_IMPENDUIT_FIELD_SIZE; neighborOffset++) {
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
    }

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

    private ArrayList<BlockPos> getFieldColumnPositions(World world, BlockPos targetNeighboringPylonPos, BlockState originState, int boundedFieldLength, boolean isOriginPylonSelected) {
        // list used to store blockpos that need to be confirmed as supported before committing to the big list
        ArrayList<BlockPos> unconfirmedBlockPosList = new ArrayList<>();

        int facingDirOffset;

        // check all blocks in front of selected pylon
        for (facingDirOffset = 1; facingDirOffset <= boundedFieldLength; facingDirOffset++) {
            BlockPos targetPos = targetNeighboringPylonPos.offset(originState.get(FACING), facingDirOffset);
            BlockState targetState = world.getBlockState(targetPos);

            // we always want to add the target block to the list - if this operation fails for any reason it clears the list, so no reason not to
            unconfirmedBlockPosList.add(targetPos);

            if (FIELD_BLOCK.canFieldReplaceBlock(world, targetPos, targetState)) {

                // protect against fields stretching out to max length if unbounded
                if (facingDirOffset == boundedFieldLength) {
                    unconfirmedBlockPosList.clear();
                    break;
                }

            } else {
                // final check to see if field column can actually form - if this fails, clear the whole list to indicate failure
                // if the collided block isn't a compatible pylon, fail
                if (!areOpposingPylonsCompatible(originState, targetState)
                        // trigger this if pylon distance is shorter than set one's distance - prevents uneven field generation
                        // this only runs after the first pylon - as bounded field length hasn't been defined then
                        || (!isOriginPylonSelected && facingDirOffset < boundedFieldLength)) {
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

    private void spawnForcefield(BlockState state, World world, BlockPos pos) {
        ArrayList<BlockPos> affectedPositions = getAffectedPositions(world, state, pos);

        Direction pylonFacing = state.get(FACING);

        // Prepare the field state - we only need to do this once for the whole field
        BlockState fieldState = FIELD_BLOCK.getDefaultState()
                .with(AXIS, pylonFacing.getAxis())
                // This signifies the field is still forming, and temporarily disables field self-destruction on updates.
                .with(ImpenduitFieldBlock.FORMED, false);


        for (BlockPos updatedPos : affectedPositions) {
            BlockState updatedState = world.getBlockState(updatedPos);

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
                world.setBlockState(
                        updatedPos,
                        pylonState
                );

            } else {
                // Drop original block's loot.
                Block.dropStacks(world.getBlockState(updatedPos), world, updatedPos);

                // Place the field.
                world.setBlockState(
                        updatedPos,
                        fieldState
                );

                // Schedule field to be updated so that it changes its state, allowing it to be destroyed on neighbor update
                // This is a safety measure to prevent certain fucky blocks from destroying Impenduit Fields during the formation stage.
                // (ahem. redstone dust, i'm looking at you.)
                world.scheduleBlockTick(updatedPos, fieldState.getBlock(), 1);
            }
        }
    }

    public void insertPowerCore(World world, BlockPos pos) {
        BlockState pylonState = world.getBlockState(pos);

        // trip sculk sensors
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(pylonState));

        world.updateComparators(pos, this);
        world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS);
        world.setBlockState(pos, pylonState.with(POWER_SOURCE_PRESENT, true));
        spawnForcefield(pylonState.cycle(POWER_SOURCE_PRESENT), world, pos);
    }

    public void removePowerCore(World world, BlockPos pylonPos, ItemStack usedStack, @Nullable PlayerEntity player, @Nullable Direction manualInteractionSide) {
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

            Identifier lootTableId = ImpenduitsBlockInteractionLootTableProvider.locatePylonPowerCoreRemovalId(pylonState.getBlock());

            ObjectArrayList<ItemStack> outputStacks = serverWorld.getServer().getReloadableRegistries().getLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableId))
                    .generateLoot(new LootContextParameterSet.Builder(serverWorld)
                            .add(LootContextParameters.TOOL, usedStack)
                            .add(LootContextParameters.BLOCK_STATE, pylonState)
                            .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pylonPos))
                            .build(LootContextTypes.BLOCK));

            // drop all loot table outputs
            for (ItemStack stack : outputStacks) {
                if (manualInteractionSide != null) {
                    dropStack(serverWorld, pylonCenterPos.offset(manualInteractionSide, 0.7), new Vec3d(manualInteractionSide.getUnitVector()).multiply(1.5f / 20), stack);
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
