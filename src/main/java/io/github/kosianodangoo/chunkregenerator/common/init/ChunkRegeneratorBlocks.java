package io.github.kosianodangoo.chunkregenerator.common.init;

import io.github.kosianodangoo.chunkregenerator.ChunkRegenerator;
import io.github.kosianodangoo.chunkregenerator.common.block.ChunkRegeneratorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ChunkRegeneratorBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ChunkRegenerator.MOD_ID);

    public static final RegistryObject<Block> CHUNK_REGENERATOR = BLOCKS.register("chunk_regenerator", ChunkRegeneratorBlock::new);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
