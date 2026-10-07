package com.experiencestorage.block;

import com.experiencestorage.blockentity.XpTankBlockEntity;
import com.experiencestorage.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

public class XpTankBlock extends BaseEntityBlock {
    /** Visual fill level 0..14 (matches the addon's 14 liquid layers). */
    public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, 14);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public XpTankBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FILL, 0).setValue(LIT, false));
    }

    /** Same formula as the addon's render controller. */
    public static int fillFor(int xp) {
        if (xp <= 0) {
            return 0;
        }
        if (xp < 209) {
            return 1;
        }
        return Mth.clamp(xp * 14 / XpTankBlockEntity.CAPACITY, 1, 14);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FILL, LIT);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new XpTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.XP_TANK.get(), XpTankBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (held.is(Items.GLASS_BOTTLE)) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (level.getBlockEntity(pos) instanceof XpTankBlockEntity tank && tank.getXp() > 10) {
                ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                if (!player.getInventory().add(bottle)) {
                    player.drop(bottle, false);
                }
                tank.removeXp(7 + level.random.nextInt(5));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        if (held.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof XpTankBlockEntity tank) {
                player.displayClientMessage(Component.translatable("message.experience_storage.tank_contents",
                        tank.getXp(), XpTankBlockEntity.CAPACITY), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof XpTankBlockEntity tank
                    && tank.getXp() > 0) {
                ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), tank.getXp());
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
