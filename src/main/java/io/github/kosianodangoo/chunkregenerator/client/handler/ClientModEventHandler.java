package io.github.kosianodangoo.chunkregenerator.client.handler;

import io.github.kosianodangoo.chunkregenerator.ChunkRegenerator;
import io.github.kosianodangoo.chunkregenerator.client.screen.ChunkRegeneratorScreen;
import io.github.kosianodangoo.chunkregenerator.common.init.ChunkRegeneratorMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ChunkRegenerator.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEventHandler {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ChunkRegeneratorMenuTypes.CHUNK_REGENERATOR.get(), ChunkRegeneratorScreen::new);
        });
    }
}
