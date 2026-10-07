package com.experiencestorage.registry;

import com.experiencestorage.ExperienceStorage;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExperienceStorage.MOD_ID);

    public static final RegistryObject<CreativeModeTab> EXPERIENCE_FLOW = CREATIVE_TABS.register("experience_flow",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.experience_storage.experience_flow"))
                    .icon(() -> new ItemStack(ModItems.XP_TANK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.XP_TANK.get());
                        output.accept(ModItems.XP_PIPE.get());
                        output.accept(ModItems.XP_EXTRACTOR.get());
                        output.accept(ModItems.XP_VALVE.get());
                        output.accept(ModItems.ORB_FAIRY_SPAWN_EGG.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
