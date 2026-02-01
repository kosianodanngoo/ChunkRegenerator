package io.github.kosianodangoo.chunkregenerator.common.init;

import io.github.kosianodangoo.chunkregenerator.ChunkRegenerator;
import io.github.kosianodangoo.chunkregenerator.common.block.entity.ChunkRegeneratorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ChunkRegeneratorBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ChunkRegenerator.MOD_ID);

    public static RegistryObject<BlockEntityType<ChunkRegeneratorBlockEntity>> CHUNK_REGENERATOR = TILE_ENTITIES.register(
            "chunk_regenerator",
            () -> BlockEntityType.Builder.of(ChunkRegeneratorBlockEntity::new, ChunkRegeneratorBlocks.CHUNK_REGENERATOR.get()).build(null)
    );

    public static void register(IEventBus modEventBus) {
        TILE_ENTITIES.register(modEventBus);
    }
}
