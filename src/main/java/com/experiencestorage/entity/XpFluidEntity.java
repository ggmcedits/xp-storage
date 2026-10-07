package com.experiencestorage.entity;

import com.experiencestorage.block.XpPipeBlock;
import com.experiencestorage.blockentity.XpTankBlockEntity;
import com.experiencestorage.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A packet of experience travelling through the pipe network. Replaces the addon's script-driven
 * "xp_fluid" entity: it walks from pipe centre to pipe centre, picks a random onward pipe at every
 * junction (never straight back unless it is a dead end), and empties itself into any tank that a
 * pipe touches on a side or bottom face. If its pipe disappears it turns into ordinary XP orbs.
 */
public class XpFluidEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_AMOUNT =
            SynchedEntityData.defineId(XpFluidEntity.class, EntityDataSerializers.INT);

    /** Blocks per tick. */
    private static final double SPEED = 0.15D;

    private BlockPos target;
    private Direction heading = Direction.DOWN;

    public XpFluidEntity(EntityType<? extends XpFluidEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /** Creates a packet that enters the pipe cell at {@code pipePos} while travelling in {@code heading}. */
    public static void spawn(Level level, BlockPos pipePos, int amount, Direction heading) {
        XpFluidEntity entity = ModEntities.XP_FLUID.get().create(level);
        if (entity == null) {
            return;
        }
        Vec3 start = Vec3.atCenterOf(pipePos).relative(heading.getOpposite(), 0.45D);
        entity.moveTo(start.x, start.y, start.z, 0.0F, 0.0F);
        entity.setAmount(amount);
        entity.target = pipePos.immutable();
        entity.heading = heading;
        level.addFreshEntity(entity);
    }

    public int getAmount() {
        return this.entityData.get(DATA_AMOUNT);
    }

    public void setAmount(int amount) {
        this.entityData.set(DATA_AMOUNT, Math.max(0, amount));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_AMOUNT, 0);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        this.setDeltaMovement(Vec3.ZERO);
        ServerLevel level = (ServerLevel) this.level();

        if (getAmount() <= 0) {
            discard();
            return;
        }
        if (target == null) {
            eject(level);
            return;
        }
        if (!level.isLoaded(target)) {
            return; // wait for the chunk
        }
        BlockState state = level.getBlockState(target);
        if (!(state.getBlock() instanceof XpPipeBlock)) {
            eject(level);
            return;
        }

        Vec3 center = Vec3.atCenterOf(target);
        Vec3 diff = center.subtract(this.position());
        double dist = diff.length();
        if (dist > SPEED) {
            Vec3 next = this.position().add(diff.scale(SPEED / dist));
            this.setPos(next.x, next.y, next.z);
            return;
        }
        this.setPos(center.x, center.y, center.z);
        arrive(level, target, state);
    }

    private void arrive(ServerLevel level, BlockPos pos, BlockState state) {
        // 1. Empty into adjacent tanks (not into one sitting on top of the pipe: that one is the source).
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || !state.getValue(XpPipeBlock.CONNECTION.get(dir))) {
                continue;
            }
            BlockPos neighborPos = pos.relative(dir);
            if (!level.isLoaded(neighborPos)) {
                continue;
            }
            if (level.getBlockEntity(neighborPos) instanceof XpTankBlockEntity tank) {
                int accepted = tank.addXp(getAmount());
                if (accepted > 0) {
                    setAmount(getAmount() - accepted);
                    if (this.random.nextInt(4) == 0) {
                        level.playSound(null, neighborPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS,
                                0.3F, 0.9F + this.random.nextFloat() * 0.2F);
                    }
                }
                if (getAmount() <= 0) {
                    discard();
                    return;
                }
            }
        }

        // 2. A closed valve reflects whatever is inside the pipe.
        if (state.getValue(XpPipeBlock.BLOCKED)) {
            bounce(level, pos, state);
            return;
        }

        // 3. Pick the next pipe.
        List<Direction> options = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (!state.getValue(XpPipeBlock.CONNECTION.get(dir))) {
                continue;
            }
            BlockPos neighborPos = pos.relative(dir);
            if (!level.isLoaded(neighborPos)) {
                continue;
            }
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.getBlock() instanceof XpPipeBlock && !neighbor.getValue(XpPipeBlock.BLOCKED)) {
                options.add(dir);
            }
        }
        if (options.size() > 1) {
            options.remove(heading.getOpposite());
        }
        if (options.isEmpty()) {
            bounce(level, pos, state);
            return;
        }
        Direction pick = options.get(this.random.nextInt(options.size()));
        this.heading = pick;
        this.target = pos.relative(pick);
    }

    /** Turn around if possible, otherwise sit tight and retry on the next tick. */
    private void bounce(ServerLevel level, BlockPos pos, BlockState state) {
        Direction back = heading.getOpposite();
        if (state.getValue(XpPipeBlock.CONNECTION.get(back))) {
            BlockPos backPos = pos.relative(back);
            if (level.isLoaded(backPos)) {
                BlockState backState = level.getBlockState(backPos);
                if (backState.getBlock() instanceof XpPipeBlock && !backState.getValue(XpPipeBlock.BLOCKED)) {
                    this.heading = back;
                    this.target = backPos;
                }
            }
        }
    }

    /** The pipe is gone: hand the experience back as regular orbs. */
    private void eject(ServerLevel level) {
        ExperienceOrb.award(level, this.position(), getAmount());
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setAmount(tag.getInt("Amount"));
        if (tag.contains("Target")) {
            this.target = BlockPos.of(tag.getLong("Target"));
        }
        this.heading = Direction.from3DDataValue(tag.getByte("Heading"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Amount", getAmount());
        if (target != null) {
            tag.putLong("Target", target.asLong());
        }
        tag.putByte("Heading", (byte) heading.get3DDataValue());
    }
}
