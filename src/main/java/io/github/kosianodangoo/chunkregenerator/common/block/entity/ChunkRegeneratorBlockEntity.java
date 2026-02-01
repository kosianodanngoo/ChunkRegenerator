package io.github.kosianodangoo.chunkregenerator.common.block.entity;

import io.github.kosianodangoo.chunkregenerator.ChunkRegenerator;
import io.github.kosianodangoo.chunkregenerator.ChunkRegeneratorConfig;
import io.github.kosianodangoo.chunkregenerator.common.init.ChunkRegeneratorBlockEntities;
import io.github.kosianodangoo.chunkregenerator.common.menu.ChunkRegeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class ChunkRegeneratorBlockEntity extends BlockEntity implements MenuProvider {
    protected boolean working = false;
    protected final EnergyStorage energyStorage = new EnergyStorage(ChunkRegeneratorConfig.CHUNK_REGENERATOR_ENERGY_CAPACITY.get());
    private final LazyOptional<IEnergyStorage> energyStorageCap = LazyOptional.of(() -> energyStorage);

    public static String WORKING_NBT = "isWorking";
    public static String ENERGY_STORAGE_NBT = "energyStorage";
    public static String TARGET_CHUNKS_NBT = "targetChunks";

    public static final int RADIUS = 3;
    public static final int RANGE = RADIUS * 2 + 1;

    public final ContainerData data;

    public boolean[][] targetChunks = new boolean[RANGE][RANGE];

    public final int energyUsage = ChunkRegeneratorConfig.CHUNK_REGENERATOR_ENERGY_USAGE.get();

    public ChunkRegeneratorBlockEntity(BlockEntityType<?> pBlockEntityType, BlockPos pPos, BlockState pState) {
        super(pBlockEntityType, pPos, pState);
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                int index = i >> 1;
                int shortindex = i & 1;
                return switch (index) {
                    case 0 -> energyStorage.getEnergyStored() >> (Short.SIZE * shortindex) & 0xffff;
                    case 1 -> energyStorage.getMaxEnergyStored() >> (Short.SIZE * shortindex) & 0xffff;
                    default -> 0;
                };
            }

            @Override
            public void set(int p_39285_, int p_39286_) {
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    public ChunkRegeneratorBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        this(ChunkRegeneratorBlockEntities.CHUNK_REGENERATOR.get(), p_155229_, p_155230_);
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public Collection<ChunkPos> getTargetChunks() {
        ChunkPos baseChunkPos = new ChunkPos(this.worldPosition);
        Collection<ChunkPos> targetChunks = new ArrayList<>(RANGE*RANGE);
        for (int i = 0; i < RANGE; i++) {
            for (int j = 0; j < RANGE; j++) {
                if (this.targetChunks[i][j]) {
                    targetChunks.add(new ChunkPos(baseChunkPos.x + i - RADIUS, baseChunkPos.z + j - RADIUS));
                }
            }
        }
        return targetChunks;
    }

    public int getRequiredEnergy(Collection<ChunkPos> targetChunks) {
        return targetChunks.size() * energyUsage;
    }

    public boolean canRegenerateChunks(Collection<ChunkPos> targetChunks) {
        int requiredEnergy = this.getRequiredEnergy(targetChunks);
        return this.energyStorage.extractEnergy(requiredEnergy, true) >= requiredEnergy;
    }

    public boolean regenerateChunks(Level pLevel, BlockPos pPos) {
        return regenerateChunks(pLevel, pPos, getTargetChunks());
    }

    public boolean regenerateChunks(Level pLevel, BlockPos pPos, Collection<ChunkPos> targetChunks) {
        if(targetChunks.isEmpty() || !canRegenerateChunks(targetChunks) || !(pLevel instanceof ServerLevel serverLevel)) return false;
        ChunkRegenerator.regenerateChunks(serverLevel, targetChunks);
        this.energyStorage.extractEnergy(getRequiredEnergy(targetChunks), false);
        return true;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorageCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public void saveTargetChunks(CompoundTag compoundTag) {
        byte[] targetChunksData = new byte[RANGE];
        for (int i = 0; i < RANGE; i++) {
            byte rowData = 0;
            for (int j = 0; j < RANGE; j++) {
                if (targetChunks[i][j]) {
                    rowData |= (byte) (1 << j);
                }
            }
            targetChunksData[i] = rowData;
        }
        compoundTag.putByteArray(TARGET_CHUNKS_NBT, targetChunksData);
    }

    public void loadTargetChunks(CompoundTag compoundTag) {
        byte[] targetChunksData = compoundTag.getByteArray(TARGET_CHUNKS_NBT);
        for (int i = 0; i < RANGE; i++) {
            for (int j = 0; j < RANGE; j++) {
                targetChunks[i][j] = (targetChunksData[i] & 1 << j) != 0;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putBoolean(WORKING_NBT, isWorking());
        compoundTag.put(ENERGY_STORAGE_NBT, energyStorage.serializeNBT());
        saveTargetChunks(compoundTag);
    }

    public void toggleTargetChunk(int x, int z) {
        if (x < 0 || z < 0 || x > RANGE || z > RANGE) return;
        targetChunks[x][z] = !targetChunks[x][z];
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        setWorking(compoundTag.getBoolean(WORKING_NBT));
        if(compoundTag.get(ENERGY_STORAGE_NBT) instanceof IntTag energyTag) {
            energyStorage.deserializeNBT(energyTag);
        }
        loadTargetChunks(compoundTag);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag pTag) {
        super.handleUpdateTag(pTag);
        loadTargetChunks(pTag);
    }


    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveTargetChunks(tag);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pId, Inventory pInventory, Player pPlayer) {
        return new ChunkRegeneratorMenu(pId, pInventory, this, data);
    }
}
