package com.experiencestorage.registry;

import com.experiencestorage.ExperienceStorage;
import com.experiencestorage.blockentity.XpTankBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ExperienceStorage.MOD_ID);

    public static final RegistryObject<BlockEntityType<XpTankBlockEntity>> XP_TANK = BLOCK_ENTITIES.register("xp_tank",
            () -> BlockEntityType.Builder.of(XpTankBlockEntity::new, ModBlocks.XP_TANK.get()).build(null));

    private ModBlockEntities() {
    }
}
