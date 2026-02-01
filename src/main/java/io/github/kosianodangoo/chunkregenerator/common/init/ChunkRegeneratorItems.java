package io.github.kosianodangoo.chunkregenerator.common.init;

import io.github.kosianodangoo.chunkregenerator.ChunkRegenerator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ChunkRegeneratorItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ChunkRegenerator.MOD_ID);

    public static RegistryObject<Item> CHUNK_REGENERATOR = ITEMS.register("chunk_regenerator", () -> new BlockItem(ChunkRegeneratorBlocks.CHUNK_REGENERATOR.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
