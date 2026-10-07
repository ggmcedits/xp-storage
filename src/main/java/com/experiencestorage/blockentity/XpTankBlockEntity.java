package com.experiencestorage.blockentity;

import com.experiencestorage.block.XpPipeBlock;
import com.experiencestorage.block.XpTankBlock;
import com.experiencestorage.entity.XpFluidEntity;
import com.experiencestorage.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Stores experience points (the addon's hidden "xp_storage" entity, now a proper block entity). */
public class XpTankBlockEntity extends BlockEntity {
    /** 2920 points == level 40, same as the addon. */
    public static final int CAPACITY = 2920;
    /** Points moved per output step (same as the addon). */
    public static final int TRANSFER = 160;

    private int xp;

    public XpTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.XP_TANK.get(), pos, state);
    }

    public int getXp() {
        return xp;
    }

    /** @return how many points were actually accepted */
    public int addXp(int amount) {
        int accepted = Math.max(0, Math.min(amount, CAPACITY - xp));
        if (accepted > 0) {
            xp += accepted;
            onChanged();
        }
        return accepted;
    }

    /** @return how many points were actually removed */
    public int removeXp(int amount) {
        int removed = Math.max(0, Math.min(amount, xp));
        if (removed > 0) {
            xp -= removed;
            onChanged();
        }
        return removed;
    }

    private void onChanged() {
        setChanged();
        Level lvl = this.level;
        if (lvl != null && !lvl.isClientSide) {
            BlockState state = getBlockState();
            int fill = XpTankBlock.fillFor(xp);
            boolean lit = xp > 0;
            if (state.getValue(XpTankBlock.FILL) != fill || state.getValue(XpTankBlock.LIT) != lit) {
                lvl.setBlock(worldPosition, state.setValue(XpTankBlock.FILL, fill).setValue(XpTankBlock.LIT, lit),
                        Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Xp", xp);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        xp = Mth.clamp(tag.getInt("Xp"), 0, CAPACITY);
    }

    /** Pushes experience out of the bottom: into a pipe below, or into a tank stacked below. */
    public static void serverTick(Level level, BlockPos pos, BlockState state, XpTankBlockEntity tank) {
        if (tank.xp <= 0 || level.getGameTime() % 2 != 0 || level.hasNeighborSignal(pos)) {
            return;
        }
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);

        if (below.getBlock() instanceof XpPipeBlock) {
            if (below.getValue(XpPipeBlock.BLOCKED)) {
                return;
            }
            if (level.getEntitiesOfClass(XpFluidEntity.class, new AABB(belowPos)).size() > 5) {
                return;
            }
            int amount = tank.removeXp(Math.min(tank.xp, TRANSFER));
            if (amount > 0) {
                XpFluidEntity.spawn(level, belowPos, amount, Direction.DOWN);
            }
        } else if (level.getBlockEntity(belowPos) instanceof XpTankBlockEntity other) {
            int moved = other.addXp(Math.min(tank.xp, TRANSFER));
            if (moved > 0) {
                tank.removeXp(moved);
                if (level.random.nextInt(4) == 0) {
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.3F,
                            0.9F + level.random.nextFloat() * 0.2F);
                }
            }
        }
    }
}
