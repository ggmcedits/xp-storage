package com.experiencestorage.client;

import com.experiencestorage.entity.OrbFairyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class OrbFairyRenderer extends XpOrbLikeRenderer<OrbFairyEntity> {
    public OrbFairyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /** Addon formula: floor(min(xp, 55) * 9 / 55). */
    @Override
    protected int getIcon(OrbFairyEntity entity) {
        return Math.min(entity.getXp(), 55) * 9 / 55;
    }

    /** Addon animation: scale pulses 1.0 -> 0.4 -> 1.0 once per second. */
    @Override
    protected float getScale(OrbFairyEntity entity, float partialTick) {
        float t = (entity.tickCount + partialTick) / 20.0F;
        return 0.3F * (0.7F + 0.3F * Mth.cos(t * Mth.TWO_PI));
    }
}
