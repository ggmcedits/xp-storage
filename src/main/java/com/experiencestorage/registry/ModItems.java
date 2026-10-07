package com.experiencestorage.registry;

import com.experiencestorage.ExperienceStorage;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ExperienceStorage.MOD_ID);

    public static final RegistryObject<Item> XP_TANK = ITEMS.register("xp_tank",
            () -> new BlockItem(ModBlocks.XP_TANK.get(), new Item.Properties()));
    public static final RegistryObject<Item> XP_PIPE = ITEMS.register("xp_pipe",
            () -> new BlockItem(ModBlocks.XP_PIPE.get(), new Item.Properties()));
    public static final RegistryObject<Item> XP_EXTRACTOR = ITEMS.register("xp_extractor",
            () -> new BlockItem(ModBlocks.XP_EXTRACTOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> XP_VALVE = ITEMS.register("xp_valve",
            () -> new BlockItem(ModBlocks.XP_VALVE.get(), new Item.Properties()));

    // White colours: the item model uses the addon's own orb icon, so no egg tint is wanted.
    public static final RegistryObject<Item> ORB_FAIRY_SPAWN_EGG = ITEMS.register("orb_fairy_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ORB_FAIRY, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    private ModItems() {
    }
}
