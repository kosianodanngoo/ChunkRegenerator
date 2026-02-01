package io.github.kosianodangoo.chunkregenerator.common.init;

import io.github.kosianodangoo.chunkregenerator.ChunkRegenerator;
import io.github.kosianodangoo.chunkregenerator.common.menu.ChunkRegeneratorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ChunkRegeneratorMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ChunkRegenerator.MOD_ID);

    public static final RegistryObject<MenuType<ChunkRegeneratorMenu>> CHUNK_REGENERATOR = MENU_TYPES.register("chunk_regenerator", () -> IForgeMenuType.create(ChunkRegeneratorMenu::new));

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
