package io.github.kosianodangoo.chunkregenerator.common.network;

import io.github.kosianodangoo.chunkregenerator.ChunkRegenerator;
import io.github.kosianodangoo.chunkregenerator.common.network.serverbound.ServerboundUpdateTargetChunksPacket;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ChunkRegeneratorConnection {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE
            = NetworkRegistry.newSimpleChannel(ChunkRegenerator.getResourceLocation("network"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void init() {
        int id = -1;

        INSTANCE.registerMessage(++id, ServerboundUpdateTargetChunksPacket.class, ServerboundUpdateTargetChunksPacket::encode, ServerboundUpdateTargetChunksPacket::decode, ServerboundUpdateTargetChunksPacket::handle);
    }
}
