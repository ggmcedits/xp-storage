package com.experiencestorage.client;

import com.experiencestorage.ExperienceStorage;
import com.experiencestorage.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExperienceStorage.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.XP_FLUID.get(), XpFluidRenderer::new);
        event.registerEntityRenderer(ModEntities.ORB_FAIRY.get(), OrbFairyRenderer::new);
    }
}
