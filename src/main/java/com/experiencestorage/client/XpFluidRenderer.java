package com.experiencestorage.client;

import com.experiencestorage.entity.XpFluidEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class XpFluidRenderer extends XpOrbLikeRenderer<XpFluidEntity> {
    public XpFluidRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /** Addon formula: floor(amount * 9 / 160). */
    @Override
    protected int getIcon(XpFluidEntity entity) {
        return Math.min(9, entity.getAmount() * 9 / 160);
    }
}
