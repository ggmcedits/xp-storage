package com.experiencestorage;

import com.experiencestorage.registry.ModBlockEntities;
import com.experiencestorage.registry.ModBlocks;
import com.experiencestorage.registry.ModCreativeTabs;
import com.experiencestorage.registry.ModEntities;
import com.experiencestorage.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge port of the "Experience Storage" Bedrock addon by Effect99.
 */
@Mod(ExperienceStorage.MOD_ID)
public class ExperienceStorage {
    public static final String MOD_ID = "experience_storage";

    public ExperienceStorage() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);
    }
}
