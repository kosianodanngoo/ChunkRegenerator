package io.github.kosianodangoo.chunkregenerator.common.handler;

import io.github.kosianodangoo.chunkregenerator.ChunkRegenerator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = ChunkRegenerator.MOD_ID)
public class CommonForgeEventHandler {
    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("regeneratechunks").requires((source) -> source.hasPermission(2)).executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    ServerLevel level = source.getLevel();
                    Vec3 pos = source.getPosition();
                    ChunkPos chunkPos = new ChunkPos(new BlockPos((int) pos.x(), (int) pos.y(), (int) pos.z()));
                    source.sendSystemMessage(Component.translatable("command.chunk_regenerator.regeneratechunks.start", chunkPos.x, chunkPos.z, chunkPos.x, chunkPos.z));
                    long startedTime = System.currentTimeMillis();
                    int result = ChunkRegenerator.regenerateChunks(level, Set.of(chunkPos));
                    long processTime = System.currentTimeMillis() - startedTime;
                    source.sendSuccess(() -> Component.translatable("command.chunk_regenerator.regeneratechunks.success", 1, processTime, processTime), true);
                    return result;
                }).then(
                        Commands.argument("from", ColumnPosArgument.columnPos()).executes(ctx -> {
                            CommandSourceStack source = ctx.getSource();
                            ServerLevel level = source.getLevel();
                            ChunkPos chunkPos = ColumnPosArgument.getColumnPos(ctx, "from").toChunkPos();
                            source.sendSystemMessage(Component.translatable("command.chunk_regenerator.regeneratechunks.start", chunkPos.x, chunkPos.z, chunkPos.x, chunkPos.z));
                            long startedTime = System.currentTimeMillis();
                            int result = ChunkRegenerator.regenerateChunks(level, Set.of(chunkPos));
                            long processTime = System.currentTimeMillis() - startedTime;
                            source.sendSuccess(() -> Component.translatable("command.chunk_regenerator.regeneratechunks.success", 1, processTime, processTime), true);
                            return result;
                        }).then(
                                Commands.argument("to", ColumnPosArgument.columnPos()).executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    ServerLevel level = source.getLevel();
                                    ChunkPos chunkPos1 = ColumnPosArgument.getColumnPos(ctx, "from").toChunkPos();
                                    ChunkPos chunkPos2 = ColumnPosArgument.getColumnPos(ctx, "to").toChunkPos();
                                    int minX = Math.min(chunkPos1.x, chunkPos2.x);
                                    int maxX = Math.max(chunkPos1.x, chunkPos2.x);
                                    int minZ = Math.min(chunkPos1.z, chunkPos2.z);
                                    int maxZ = Math.max(chunkPos1.z, chunkPos2.z);
                                    int modifiedChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
                                    Collection<ChunkPos> targetChunks = new ArrayList<>(modifiedChunks);
                                    for (int x = minX; x <= maxX; x++) {
                                        for (int z = minZ; z <= maxZ; z++) {
                                            targetChunks.add(new ChunkPos(x, z));
                                        }
                                    }
                                    source.sendSystemMessage(Component.translatable("command.chunk_regenerator.regeneratechunks.start", chunkPos1.x, chunkPos1.z, chunkPos2.x, chunkPos2.z));
                                    long startedTime = System.currentTimeMillis();
                                    int result = ChunkRegenerator.regenerateChunks(level, targetChunks);
                                    long processTime = System.currentTimeMillis() - startedTime;
                                    source.sendSuccess(() -> Component.translatable("command.chunk_regenerator.regeneratechunks.success", modifiedChunks, processTime, processTime/modifiedChunks), true);
                                    return result;
                                })
                        )
                )
        );

    }
}
