package net.myriantics.impenduits.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.registry.block.ImpenduitsBlockStateProperties;
import net.myriantics.impenduits.tag.ImpenduitsBlockTags;
import net.myriantics.impenduits.tag.ImpenduitsEnchantmentTags;

public class ImpenduitFieldBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty FORMED = ImpenduitsBlockStateProperties.FORMED;

    public ImpenduitFieldBlock(Properties settings) {
        super(settings);

        registerDefaultState(this.getStateDefinition().any()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(FORMED, true));
    }

    // you can always f5 through these
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        boolean shouldGetShape = true;

        if (context instanceof EntityCollisionContext entityShapeContext && entityShapeContext.getEntity() instanceof Player player) {

            // note to self - blockviews are SHITE
            try {
                // the outline only shows up if your head isn't in the targeted block
                shouldGetShape = !player.level().getBlockState(BlockPos.containing(player.getEyePosition())).is(this);
            } catch (ArrayIndexOutOfBoundsException wackexception) {
                ImpenduitsCommon.LOGGER.warn("Exception tried to trigger. I tried to fix this, but this janky hack should work to cover my bases.");
            }
        }

        return shouldGetShape ? super.getShape(state, world, pos, context) : Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityShapeContext
                && entityShapeContext.getEntity() instanceof LivingEntity livingEntity) {

            VoxelShape shape = Shapes.block();

            // entities can walk on impenduit pylons if they have frost walker
            if (
                    EnchantmentHelper.hasTag(livingEntity.getItemBySlot(EquipmentSlot.FEET), ImpenduitsEnchantmentTags.IMPENDUIT_FIELD_WALKABLE_ENCHANTMENTS)
                    && entityShapeContext.isAbove(shape, pos, false)
                    // since impenduit fields act as if the player is touching water, this allows for lazy hack to go brr
                    && !livingEntity.isInWaterOrRain()) {
                return shape;
            }
        }

        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState state) {
        return false;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        Direction.Axis axis = state.getValue(AXIS);

        // validate that the field is supported before confirming formation
        for (Direction.AxisDirection axisDirection : Direction.AxisDirection.values()) {
            BlockPos supportingPos = pos.relative(Direction.fromAxisAndDirection(axis, axisDirection));
            BlockState supportingState = world.getBlockState(supportingPos);

            // if either side of the field is unsupported, break field and exit method
            if (!canStateSupportField(state, supportingState)) {
                world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                return;
            }
        }

        // if both sides were supported, we can confirm a successful formation.
        world.setBlockAndUpdate(pos, state.setValue(FORMED, true));
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        Player player = context.getPlayer();

        // you can only replace the field block if your head is inside one
        return player != null && context.getLevel().getBlockState(BlockPos.containing(player.getEyePosition())).is(this);
    }

    public boolean canFieldReplaceBlock(Level world, BlockPos pos, BlockState state) {
        return (
                // if the block is already replaceable, you're good
                state.canBeReplaced()
                        // if a block is specifically tagged as being replaceable, then go ahead.
                        || state.is(ImpenduitsBlockTags.IMPENDUIT_FIELD_BLOCK_REPLACEMENT_ALLOWLIST)
                        // if the block isn't a full block and is an instabreak, impenduits can replace it. this drops the block's loot!
                        || (state.getDestroySpeed(null, null) == 0f && !state.isCollisionShapeFullBlock(world, pos))
        )
                // fields can't replace other fields
                && !(state.getBlock() instanceof ImpenduitFieldBlock)
                // denylist overrides any other conditions
                && !state.is(ImpenduitsBlockTags.IMPENDUIT_FIELD_BLOCK_REPLACEMENT_DENYLIST);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis updatedFieldAxis = state.getValue(AXIS);

        // only update state if field has formed
        // if the update is inline with the field column, run this
        if (state.getValue(FORMED) && !world.isClientSide() && direction.getAxis().equals(updatedFieldAxis) && !canStateSupportField(state, neighborState)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);

            // only play a few sounds, instead of every overwritten block
            if (!neighborState.is(Blocks.AIR)) {
                world.playSound(null, pos, this.soundType.getBreakSound(), SoundSource.BLOCKS);
            }
        }

        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, FORMED);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        return stateFrom.equals(state);
    }

    private boolean areFieldsCompatible(BlockState originField, BlockState otherField) {
        // field blockstates have to be identical (sans forming state) to be compatible - they also are double checked to be field blocks
        return originField.is(this) && otherField.is(this) && originField.getValue(AXIS).equals(otherField.getValue(AXIS));
    }

    private boolean canStateSupportField(BlockState fieldState, BlockState supportState) {
        return areFieldsCompatible(fieldState, supportState)
                || (supportState.getBlock() instanceof ImpenduitPylonBlock pylonBlock && pylonBlock.canSupportField(supportState, fieldState)) ;
    }
}
