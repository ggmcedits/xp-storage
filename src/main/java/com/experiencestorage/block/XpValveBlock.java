package com.experiencestorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Valve clipped onto a face of a pipe. FACING is the face that was clicked (same as Bedrock's
 * minecraft:block_face), so the pipe it controls is at pos.relative(FACING.getOpposite()).
 */
public class XpValveBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** True when closed: the attached pipe stops passing / emitting experience. */
    public static final BooleanProperty BLOCKED = XpPipeBlock.BLOCKED;

    private static final VoxelShape[] OUTLINE = new VoxelShape[6];
    private static final VoxelShape[] COLLISION = new VoxelShape[6];

    static {
        for (Direction dir : Direction.values()) {
            // authored for NORTH (attached to the block on its +Z / south side)
            OUTLINE[dir.ordinal()] = orient(dir, 3, 3, 9, 13, 13, 16);
            COLLISION[dir.ordinal()] = orient(dir, 3, 3, 11, 13, 13, 16);
        }
    }

    public XpValveBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BLOCKED, false));
    }

    private static VoxelShape orient(Direction facing, double x1, double y1, double z1, double x2, double y2, double z2) {
        double[] a = transform(facing, x1, y1, z1);
        double[] b = transform(facing, x2, y2, z2);
        return Block.box(Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2]),
                Math.max(a[0], b[0]), Math.max(a[1], b[1]), Math.max(a[2], b[2]));
    }

    private static double[] transform(Direction facing, double x, double y, double z) {
        return switch (facing) {
            case SOUTH -> new double[]{16 - x, y, 16 - z};
            case WEST -> new double[]{z, y, 16 - x};
            case EAST -> new double[]{16 - z, y, x};
            case UP -> new double[]{x, 16 - z, y};
            case DOWN -> new double[]{x, z, 16 - y};
            default -> new double[]{x, y, z};
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BLOCKED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE[state.getValue(FACING).ordinal()];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION[state.getValue(FACING).ordinal()];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!level.isClientSide) {
            boolean blocked = !state.getValue(BLOCKED);
            level.setBlock(pos, state.setValue(BLOCKED, blocked), Block.UPDATE_ALL);
            setPipeBlocked(level, pos.relative(state.getValue(FACING).getOpposite()), blocked);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, blocked ? 0.5F : 0.6F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            // improvement over the addon: removing a closed valve re-opens its pipe
            setPipeBlocked(level, pos.relative(state.getValue(FACING).getOpposite()), false);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    private static void setPipeBlocked(Level level, BlockPos pipePos, boolean blocked) {
        BlockState pipe = level.getBlockState(pipePos);
        if (pipe.getBlock() instanceof XpPipeBlock && pipe.getValue(XpPipeBlock.BLOCKED) != blocked) {
            level.setBlock(pipePos, pipe.setValue(XpPipeBlock.BLOCKED, blocked), Block.UPDATE_ALL);
        }
    }
}
