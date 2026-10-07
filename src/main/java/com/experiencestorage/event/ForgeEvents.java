package com.experiencestorage.event;

import com.experiencestorage.ExperienceStorage;
import com.experiencestorage.block.XpExtractorBlock;
import com.experiencestorage.blockentity.XpTankBlockEntity;
import com.experiencestorage.util.XpUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Game-bus events: extractor capture of mob XP, and sneaking players depositing XP. */
@Mod.EventBusSubscriber(modid = ExperienceStorage.MOD_ID)
public final class ForgeEvents {
    /** Points moved per step while a player sneaks on a tank / extractor. */
    private static final int PLAYER_TRANSFER = 100;
    /** Steps happen every N ticks. */
    private static final int PLAYER_INTERVAL = 4;

    private ForgeEvents() {
    }

    /** A mob killed by a player on an extractor: its real XP reward goes into the pipe instead of orbs. */
    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide || event.getDroppedExperience() <= 0) {
            return;
        }
        BlockPos pos = entity.blockPosition();
        if (level.getBlockState(pos).getBlock() instanceof XpExtractorBlock
                && XpExtractorBlock.feed(level, pos, event.getDroppedExperience())) {
            event.setDroppedExperience(0);
        }
    }

    /**
     * Mobs that die on an extractor without a player getting credit (traps, falls, lava...) still yield
     * 1-11 points, like the addon's mob-farm feature. Players, babies, golems and self-inflicted deaths
     * (/kill, cactus, berry bush) are excluded as in the original script.
     */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide || !(entity instanceof Mob) || entity.isBaby()) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.CACTUS)
                || source.is(DamageTypes.SWEET_BERRY_BUSH) || entity.getKillCredit() instanceof Player) {
            return; // player kills are handled via the XP drop event
        }
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (key != null && key.getPath().contains("golem")) {
            return;
        }
        BlockPos pos = entity.blockPosition();
        if (level.getBlockState(pos).getBlock() instanceof XpExtractorBlock) {
            XpExtractorBlock.feed(level, pos, 1 + level.random.nextInt(11));
        }
    }

    /** Sneak on a tank to pour your XP in; sneak on an extractor to push it into the pipe below. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % PLAYER_INTERVAL != 0 || !player.isShiftKeyDown() || player.isSpectator()) {
            return;
        }
        int total = XpUtil.getTotal(player);
        if (total <= 0) {
            return;
        }
        int want = Math.min(total, PLAYER_TRANSFER);
        Level level = player.level();
        BlockPos feet = player.blockPosition();

        if (level.getBlockEntity(feet.below()) instanceof XpTankBlockEntity tank) {
            int accepted = tank.addXp(want);
            if (accepted > 0) {
                XpUtil.drain(player, accepted);
                level.playSound(null, feet, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F,
                        0.9F + level.random.nextFloat() * 0.2F);
            }
        } else if (level.getBlockState(feet).getBlock() instanceof XpExtractorBlock
                && XpExtractorBlock.feed(level, feet, want)) {
            XpUtil.drain(player, want);
        }
    }
}
