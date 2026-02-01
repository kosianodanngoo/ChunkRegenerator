package io.github.kosianodangoo.chunkregenerator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.logging.LogUtils;
import io.github.kosianodangoo.chunkregenerator.common.init.ChunkRegeneratorBlockEntities;
import io.github.kosianodangoo.chunkregenerator.common.init.ChunkRegeneratorBlocks;
import io.github.kosianodangoo.chunkregenerator.common.init.ChunkRegeneratorItems;
import io.github.kosianodangoo.chunkregenerator.common.init.ChunkRegeneratorMenuTypes;
import io.github.kosianodangoo.chunkregenerator.common.network.ChunkRegeneratorConnection;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ChunkRegenerator.MOD_ID)
public class ChunkRegenerator {
    public static final String MOD_ID = "chunk_regenerator";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ChunkRegenerator() {
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();

        IEventBus modEventBus = context.getModEventBus();

        ChunkRegeneratorBlocks.register(modEventBus);
        ChunkRegeneratorItems.register(modEventBus);
        ChunkRegeneratorBlockEntities.register(modEventBus);
        ChunkRegeneratorMenuTypes.register(modEventBus);

        ChunkRegeneratorConnection.init();

        context.registerConfig(ModConfig.Type.COMMON, ChunkRegeneratorConfig.SPEC);
    }


    public static ResourceLocation getResourceLocation(String location) {
        return new ResourceLocation(MOD_ID, location);
    }

    public static int regenerateChunks(ServerLevel level, Collection<ChunkPos> targetChunks) {
        ServerChunkCache chunkSource = level.getChunkSource();
        chunkSource.chunkMap.debugReloadGenerator();

        for (ChunkPos chunkPos : targetChunks) {
            LevelChunk levelChunk = chunkSource.getChunk(chunkPos.x, chunkPos.z, false);
            if (levelChunk != null) {
                for(BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 16);
                }
            }
        }

        ProcessorMailbox<Runnable> processorMailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-regeneratechunks");

        for (ChunkStatus chunkStatus : ImmutableList.of(ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.FEATURES, ChunkStatus.INITIALIZE_LIGHT, ChunkStatus.LIGHT, ChunkStatus.SPAWN)) {
            long generationStartTime = System.currentTimeMillis();
            Supplier unitSupplier = () -> Unit.INSTANCE;
            Objects.requireNonNull(processorMailbox);
            CompletableFuture<Unit> unitSupplierAsync = CompletableFuture.supplyAsync(unitSupplier, processorMailbox::tell);

            for (ChunkPos chunkPos : targetChunks) {
                LevelChunk levelChunk = chunkSource.getChunk(chunkPos.x, chunkPos.z, false);
                if (levelChunk != null) {
                    List<ChunkAccess> chunkAccesses = Lists.newArrayList();
                    int requiredChunkRange = Math.max(1, chunkStatus.getRange());

                    for (int requiredZ = chunkPos.z - requiredChunkRange; requiredZ <= chunkPos.z + requiredChunkRange; ++requiredZ) {
                        for (int requiredX = chunkPos.x - requiredChunkRange; requiredX <= chunkPos.x + requiredChunkRange; ++requiredX) {
                            ChunkAccess chunkAccess = chunkSource.getChunk(requiredX, requiredZ, chunkStatus.getParent(), true);
                            ChunkAccess processedChunkAccess;
                            if (chunkAccess instanceof ImposterProtoChunk) {
                                processedChunkAccess = new ImposterProtoChunk(((ImposterProtoChunk) chunkAccess).getWrapped(), true);
                            } else if (chunkAccess instanceof LevelChunk) {
                                processedChunkAccess = new ImposterProtoChunk((LevelChunk) chunkAccess, true);
                            } else {
                                processedChunkAccess = chunkAccess;
                            }

                            chunkAccesses.add(processedChunkAccess);
                        }
                    }

                    Function chunkStatusProcessor = (unit) -> {
                        Objects.requireNonNull(processorMailbox);
                        return chunkStatus.generate(processorMailbox::tell, level, chunkSource.getGenerator(), level.getStructureManager(), chunkSource.getLightEngine(), (failed) -> {
                            throw new UnsupportedOperationException("Not creating full chunks here");
                        }, chunkAccesses).thenApply((accessChunkOrLoadingFailure) -> {
                            if (chunkStatus == ChunkStatus.NOISE) {
                                accessChunkOrLoadingFailure.left().ifPresent((chunkAccess) -> Heightmap.primeHeightmaps(chunkAccess, ChunkStatus.POST_FEATURES));
                            }

                            return Unit.INSTANCE;
                        });
                    };
                    Objects.requireNonNull(processorMailbox);
                    unitSupplierAsync = unitSupplierAsync.thenComposeAsync(chunkStatusProcessor, processorMailbox::tell);
                }
            }

            MinecraftServer server = level.getServer();
            Objects.requireNonNull(unitSupplierAsync);
            server.managedBlock(unitSupplierAsync::isDone);
            LOGGER.debug("{} took {} ms", chunkStatus, (System.currentTimeMillis() - generationStartTime));
        }

        long blockChangeStartTime = System.currentTimeMillis();

        for (ChunkPos chunkPos : targetChunks) {
            LevelChunk levelChunk = chunkSource.getChunk(chunkPos.x, chunkPos.z, false);
            if (levelChunk != null) {
                for(BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
                    chunkSource.blockChanged(blockPos);
                }
            }
        }

        LOGGER.debug("blockChanged took {} ms", (System.currentTimeMillis() - blockChangeStartTime));
        return 1;
    }
}
