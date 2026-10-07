package com.experiencestorage.block;

import com.experiencestorage.entity.XpFluidEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A 4px plate. Experience dropped / dying mobs / sneaking players on it are fed into the pipe below.
 */
public class XpExtractorBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    public XpExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** True if there is an open pipe directly below that can take experience. */
    public static boolean canFeed(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof XpPipeBlock && !below.getValue(XpPipeBlock.BLOCKED);
    }

    /**
     * Sends {@code amount} points into the pipe below the extractor at {@code pos}.
     *
     * @return false (and does nothing) if there is no open pipe below
     */
    public static boolean feed(Level level, BlockPos pos, int amount) {
        if (level.isClientSide || amount <= 0 || !canFeed(level, pos)) {
            return false;
        }
        XpFluidEntity.spawn(level, pos.below(), amount, Direction.DOWN);
        return true;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ExperienceOrb orb && canFeed(level, pos)) {
            // merged orbs carry a stack count that has no public getter in 1.20.1; read it from NBT
            CompoundTag tag = new CompoundTag();
            orb.saveWithoutId(tag);
            int count = Math.max(1, tag.getInt("Count"));
            if (feed(level, pos, orb.getValue() * count)) {
                orb.discard();
            }
        }
    }
}
