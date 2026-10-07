package com.experiencestorage.registry;

import com.experiencestorage.ExperienceStorage;
import com.experiencestorage.block.XpExtractorBlock;
import com.experiencestorage.block.XpPipeBlock;
import com.experiencestorage.block.XpTankBlock;
import com.experiencestorage.block.XpValveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ExperienceStorage.MOD_ID);

    public static final RegistryObject<XpTankBlock> XP_TANK = BLOCKS.register("xp_tank",
            () -> new XpTankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.EMERALD)
                    .strength(0.8F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(XpTankBlock.LIT) ? 6 : 0)
                    .isValidSpawn((state, getter, pos, type) -> false)
                    .isSuffocating((state, getter, pos) -> false)
                    .isViewBlocking((state, getter, pos) -> false)));

    public static final RegistryObject<XpPipeBlock> XP_PIPE = BLOCKS.register("xp_pipe",
            () -> new XpPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE)
                    .strength(0.3F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()));

    public static final RegistryObject<XpExtractorBlock> XP_EXTRACTOR = BLOCKS.register("xp_extractor",
            () -> new XpExtractorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.EMERALD)
                    .strength(1.0F)
                    .sound(SoundType.GLASS)
                    .noOcclusion()
                    .lightLevel(state -> 4)));

    public static final RegistryObject<XpValveBlock> XP_VALVE = BLOCKS.register("xp_valve",
            () -> new XpValveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(1.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    private ModBlocks() {
    }
}
