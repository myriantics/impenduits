package net.myriantics.impenduits.blocks;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.util.ImpenduitsTags;
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
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityShapeContext
                && entityShapeContext.getEntity() instanceof LivingEntity livingEntity) {

            VoxelShape shape = VoxelShapes.fullCube();

            // entities can walk on impenduit pylons if they have frost walker
            if (EnchantmentHelper.hasFrostWalker(livingEntity)
                    && entityShapeContext.isAbove(shape, pos, false)
                    // since impenduit fields act as if the player is touching water, this allows for lazy hack to go brr
                    && !livingEntity.isTouchingWaterOrRain()) {
                return shape;
            }
        }

        return super.getCollisionShape(state, world, pos, context);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
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

    public static boolean canFieldReplaceBlock(World world, BlockPos pos, BlockState state) {
        return (
                // if the block is already replaceable, you're good
                state.isReplaceable()
                        // if a block is specifically tagged as being replaceable, then go ahead.
                        || state.isIn(ImpenduitsTags.IMPENDUIT_FIELD_BLOCK_REPLACEMENT_ALLOWLIST)
                        // if the block isn't a full block and is an instabreak, impenduits can replace it. this drops the block's loot!
                        || (state.getHardness(null, null) == 0f && !state.isFullCube(world, pos))
        )
                // fields can't replace other fields
                && !state.isOf(ImpenduitsCommon.IMPENDUIT_FIELD)
                // denylist overrides any other conditions
                && !state.isIn(ImpenduitsTags.IMPENDUIT_FIELD_BLOCK_REPLACEMENT_DENYLIST);
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis updatedFieldAxis = state.get(AXIS);

        // if the update is inline with the field column, run this
        if (!world.isClient() && direction.getAxis().equals(updatedFieldAxis) && !canStateSupportField(state, neighborState)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), NOTIFY_ALL);

            // only play a few sounds, instead of every overwritten block
            if (!neighborState.isOf(Blocks.AIR)) {
                world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.BLOCKS);
            }
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        super.onBroken(world, pos, state);
    }

    private static boolean areFieldsCompatible(BlockState originField, BlockState otherField) {
        // field blockstates have to be identical to be compatible - they also are double checked to be field blocks
        return originField.isOf(ImpenduitsCommon.IMPENDUIT_FIELD) && originField.equals(otherField);
    }

    private static boolean canStateSupportField(BlockState fieldState, BlockState supportState) {
        return areFieldsCompatible(fieldState, supportState) ||  ImpenduitPylonBlock.canSupportField(supportState, fieldState);
    }
}
