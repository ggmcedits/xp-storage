package com.experiencestorage.entity;

import com.experiencestorage.util.XpUtil;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Invulnerable floating orb that hoards experience. It nips XP from nearby players, flees from them,
 * and spills its stash when hit. Hitting an empty one in melee makes it vanish. It also vanishes in
 * well-lit areas, so it only exists in the dark.
 */
public class OrbFairyEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_XP =
            SynchedEntityData.defineId(OrbFairyEntity.class, EntityDataSerializers.INT);

    public static final int MAX_XP = 30970;

    public OrbFairyEntity(EntityType<? extends OrbFairyEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.FLYING_SPEED, 0.1D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D);
    }

    /** Natural spawning: only in the dark (addon: brightness 0-7). */
    public static boolean checkSpawnRules(EntityType<OrbFairyEntity> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getMaxLocalRawBrightness(pos) <= 7;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_XP, 0);
    }

    public int getXp() {
        return this.entityData.get(DATA_XP);
    }

    public void setXp(int xp) {
        this.entityData.set(DATA_XP, Mth.clamp(xp, 0, MAX_XP));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        setXp(1 + this.random.nextInt(55));
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        if (this.tickCount % 20 == 0 && !hasCustomName()
                && this.level().getMaxLocalRawBrightness(this.blockPosition()) > 7) {
            vanish();
            return;
        }
        if (this.tickCount % 40 == 0) {
            interactWithNearbyPlayer();
        }
    }

    private void interactWithNearbyPlayer() {
        Player player = this.level().getNearestPlayer(this, 8.0D);
        if (player == null) {
            return;
        }
        Vec3 away = this.position().subtract(player.position());
        double dist = Math.max(away.length(), 0.001D);
        int xp = getXp();
        int playerXp = XpUtil.getTotal(player);

        if (dist <= 4.0D && playerXp > 0 && xp < MAX_XP - 54) {
            int stolen = Math.min(playerXp, 1 + this.random.nextInt(55));
            setXp(xp + stolen);
            XpUtil.drain(player, stolen);
            this.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.5F, 0.9F + this.random.nextFloat() * 0.2F);
        } else if (xp < MAX_XP - 10 && this.random.nextFloat() < 0.1F) {
            setXp(xp + 1 + this.random.nextInt(11));
        }
        // dart away from the player
        this.setDeltaMovement(this.getDeltaMovement().add(away.x / dist * 0.3D, 0.12D, away.z / dist * 0.3D));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount); // /kill and the void still work
        }
        if (!this.level().isClientSide) {
            if (source.getDirectEntity() instanceof Projectile) {
                onStruck(false);
            } else if (source.getDirectEntity() instanceof LivingEntity) {
                onStruck(true);
            }
        }
        return false;
    }

    private void onStruck(boolean melee) {
        int xp = getXp();
        if (xp > 0) {
            int amount = Math.min(1 + this.random.nextInt(5), xp);
            setXp(xp - amount);
            ExperienceOrb.award((ServerLevel) this.level(), this.position(), amount);
            this.setDeltaMovement(this.getDeltaMovement().add((this.random.nextDouble() - 0.5D) * 0.3D, 0.4D,
                    (this.random.nextDouble() - 0.5D) * 0.3D));
        } else if (melee) {
            vanish();
        }
    }

    private void vanish() {
        this.level().playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL,
                1.0F, 2.0F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY() + 0.15D, this.getZ(),
                    8, 0.1D, 0.1D, 0.1D, 0.02D);
        }
        this.discard();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.EXPERIENCE_ORB_PICKUP;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Xp", getXp());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setXp(tag.getInt("Xp"));
    }
}
