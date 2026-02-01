package io.github.kosianodangoo.chunkregenerator.common.block;

import io.github.kosianodangoo.chunkregenerator.common.block.entity.ChunkRegeneratorBlockEntity;
import io.github.kosianodangoo.chunkregenerator.common.init.ChunkRegeneratorBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChunkRegeneratorBlock extends Block implements EntityBlock {
    public ChunkRegeneratorBlock(Properties pProperties) {
        super(pProperties);
    }

    public ChunkRegeneratorBlock() {
        this(Properties.of().sound(SoundType.METAL).strength(5.0F, 10.0F));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ChunkRegeneratorBlockEntity(pPos, pState);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pChangedPos, boolean pBool) {
        if (!pLevel.isClientSide) {
            pLevel.getBlockEntity(pPos, ChunkRegeneratorBlockEntities.CHUNK_REGENERATOR.get()).ifPresent((blockEntity) -> {
                boolean flag = blockEntity.isWorking();
                if (flag != pLevel.hasNeighborSignal(pPos)) {
                    blockEntity.setWorking(!flag);
                    if (blockEntity.isWorking()) {
                        blockEntity.regenerateChunks(pLevel, pPos);
                    }
                }
            });
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return null;
    }

    @Override
    public InteractionResult use(BlockState pState, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ChunkRegeneratorBlockEntity chunkRegenerator && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, chunkRegenerator, pos);
            }
            return InteractionResult.CONSUME;
        }
    }
}
