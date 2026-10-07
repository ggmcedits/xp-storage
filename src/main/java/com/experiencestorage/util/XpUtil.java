package com.experiencestorage.util;

import net.minecraft.world.entity.player.Player;

/** Helpers for converting between player levels and raw experience points. */
public final class XpUtil {
    private XpUtil() {
    }

    /** Total experience points needed to reach the given level from level 0. */
    public static int totalXpAtLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5D * level * level - 40.5D * level + 360.0D);
        }
        return (int) (4.5D * level * level - 162.5D * level + 2220.0D);
    }

    /** Total experience points the player currently holds (levels + bar progress). */
    public static int getTotal(Player player) {
        return totalXpAtLevel(player.experienceLevel)
                + Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
    }

    /** Removes experience points from the player, correctly rolling back across levels. */
    public static void drain(Player player, int amount) {
        if (amount > 0) {
            player.giveExperiencePoints(-amount);
        }
    }
}
