package io.github.kosianodangoo.chunkregenerator.common.menu;

import io.github.kosianodangoo.chunkregenerator.common.block.entity.ChunkRegeneratorBlockEntity;
import io.github.kosianodangoo.chunkregenerator.common.init.ChunkRegeneratorBlockEntities;
import io.github.kosianodangoo.chunkregenerator.common.init.ChunkRegeneratorMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class ChunkRegeneratorMenu extends AbstractContainerMenu {

    public final ContainerData data;

    public final ChunkRegeneratorBlockEntity blockEntity;

    public ChunkRegeneratorMenu(int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        this(pContainerId, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos(), ChunkRegeneratorBlockEntities.CHUNK_REGENERATOR.get()).orElseThrow(), new SimpleContainerData(4));
    }

    public ChunkRegeneratorMenu(int pContainerId, Inventory inventory, ChunkRegeneratorBlockEntity blockEntity, ContainerData data) {
        super(ChunkRegeneratorMenuTypes.CHUNK_REGENERATOR.get(), pContainerId);

        this.blockEntity = blockEntity;
        this.data = data;

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return pPlayer.isAlive();
    }

    public int getStoredEnergy() {
        return Short.toUnsignedInt((short) data.get(1)) << 16 | Short.toUnsignedInt((short) data.get(0));
    }

    public int getMaxEnergy() {
        return Short.toUnsignedInt((short) data.get(3)) << 16 | Short.toUnsignedInt((short) data.get(2));
    }
}
