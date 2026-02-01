package io.github.kosianodangoo.chunkregenerator.common.network.serverbound;

import io.github.kosianodangoo.chunkregenerator.common.menu.ChunkRegeneratorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundUpdateTargetChunksPacket {
    private final int menuId;
    private final byte x;
    private final byte z;

    public ServerboundUpdateTargetChunksPacket(int menuId, int x, int z) {
        this.menuId = menuId;
        this.x = (byte) x;
        this.z = (byte) z;
    }

    public static void encode(ServerboundUpdateTargetChunksPacket message, FriendlyByteBuf buf) {
        buf.writeInt(message.menuId);
        buf.writeByte(message.x);
        buf.writeByte(message.z);
    }

    public static ServerboundUpdateTargetChunksPacket decode(FriendlyByteBuf buf) {
        return new ServerboundUpdateTargetChunksPacket(buf.readInt(), buf.readByte(), buf.readByte());
    }

    public static void handle(ServerboundUpdateTargetChunksPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer entity = context.getSender();
                if (entity == null) {
                    return;
                }
                if (entity.containerMenu.containerId == message.menuId && entity.containerMenu instanceof ChunkRegeneratorMenu menu) {
                    menu.blockEntity.toggleTargetChunk(message.x, message.z);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
