package com.experiencestorage.block;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Glass pipe that auto-connects to pipes, tanks, valves attached to it and an extractor on top. */
public class XpPipeBlock extends Block {
    /** True while a closed valve is blocking this pipe (the addon's "pipe:flow" state). */
    public static final BooleanProperty BLOCKED = BooleanProperty.create("blocked");

    public static final Map<Direction, BooleanProperty> CONNECTION;

    static {
        Map<Direction, BooleanProperty> map = new EnumMap<>(Direction.class);
        map.put(Direction.DOWN, BlockStateProperties.DOWN);
        map.put(Direction.UP, BlockStateProperties.UP);
        map.put(Direction.NORTH, BlockStateProperties.NORTH);
        map.put(Direction.SOUTH, BlockStateProperties.SOUTH);
        map.put(Direction.WEST, BlockStateProperties.WEST);
        map.put(Direction.EAST, BlockStateProperties.EAST);
        CONNECTION = Collections.unmodifiableMap(map);
    }

    private static final VoxelShape[] SHAPES = makeShapes();

    public XpPipeBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any().setValue(BLOCKED, false);
        for (BooleanProperty property : CONNECTION.values()) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
    }

    private static VoxelShape[] makeShapes() {
        VoxelShape core = Block.box(4, 4, 4, 12, 12, 12);
        VoxelShape[] arms = new VoxelShape[6];
        arms[Direction.DOWN.ordinal()] = Block.box(5, 0, 5, 11, 4, 11);
        arms[Direction.UP.ordinal()] = Block.box(5, 12, 5, 11, 16, 11);
        arms[Direction.NORTH.ordinal()] = Block.box(5, 5, 0, 11, 11, 4);
        arms[Direction.SOUTH.ordinal()] = Block.box(5, 5, 12, 11, 11, 16);
        arms[Direction.WEST.ordinal()] = Block.box(0, 5, 5, 4, 11, 11);
        arms[Direction.EAST.ordinal()] = Block.box(12, 5, 5, 16, 11, 11);

        VoxelShape[] shapes = new VoxelShape[64];
        for (int i = 0; i < 64; i++) {
            VoxelShape shape = core;
            for (Direction dir : Direction.values()) {
                if ((i & (1 << dir.ordinal())) != 0) {
                    shape = Shapes.or(shape, arms[dir.ordinal()]);
                }
            }
            shapes[i] = shape;
        }
        return shapes;
    }

    private static int shapeIndex(BlockState state) {
        int index = 0;
        for (Direction dir : Direction.values()) {
            if (state.getValue(CONNECTION.get(dir))) {
                index |= 1 << dir.ordinal();
            }
        }
        return index;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BLOCKED);
        builder.add(CONNECTION.values().toArray(new BooleanProperty[0]));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[shapeIndex(state)];
    }

    /** Whether a pipe should draw an arm towards a neighbour that sits in direction {@code dir}. */
    public static boolean canConnect(BlockState neighbor, Direction dir) {
        Block block = neighbor.getBlock();
        if (block instanceof XpPipeBlock || block instanceof XpTankBlock) {
            return true;
        }
        if (block instanceof XpExtractorBlock) {
            return dir == Direction.UP;
        }
        if (block instanceof XpValveBlock) {
            // valve's FACING is the face it was placed on, i.e. the face of this pipe it is attached to
            return neighbor.getValue(XpValveBlock.FACING) == dir;
        }
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction dir : Direction.values()) {
            BlockState neighbor = context.getLevel().getBlockState(context.getClickedPos().relative(dir));
            state = state.setValue(CONNECTION.get(dir), canConnect(neighbor, dir));
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return state.setValue(CONNECTION.get(direction), canConnect(neighborState, direction));
    }
}
