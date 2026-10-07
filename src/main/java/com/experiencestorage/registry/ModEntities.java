package com.experiencestorage.registry;

import com.experiencestorage.ExperienceStorage;
import com.experiencestorage.entity.OrbFairyEntity;
import com.experiencestorage.entity.XpFluidEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ExperienceStorage.MOD_ID);

    public static final RegistryObject<EntityType<OrbFairyEntity>> ORB_FAIRY = ENTITY_TYPES.register("orb_fairy",
            () -> EntityType.Builder.of(OrbFairyEntity::new, MobCategory.AMBIENT)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(8)
                    .build(ExperienceStorage.MOD_ID + ":orb_fairy"));

    public static final RegistryObject<EntityType<XpFluidEntity>> XP_FLUID = ENTITY_TYPES.register("xp_fluid",
            () -> EntityType.Builder.<XpFluidEntity>of(XpFluidEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(6)
                    .updateInterval(1)
                    .fireImmune()
                    .build(ExperienceStorage.MOD_ID + ":xp_fluid"));

    private ModEntities() {
    }
}
