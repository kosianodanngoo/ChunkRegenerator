package io.github.kosianodangoo.chunkregenerator.client.screen;

import io.github.kosianodangoo.chunkregenerator.ChunkRegenerator;
import io.github.kosianodangoo.chunkregenerator.client.screen.widget.EnergyBarWidget;
import io.github.kosianodangoo.chunkregenerator.common.block.entity.ChunkRegeneratorBlockEntity;
import io.github.kosianodangoo.chunkregenerator.common.menu.ChunkRegeneratorMenu;
import io.github.kosianodangoo.chunkregenerator.common.network.ChunkRegeneratorConnection;
import io.github.kosianodangoo.chunkregenerator.common.network.serverbound.ServerboundUpdateTargetChunksPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;


public class ChunkRegeneratorScreen extends AbstractContainerScreen<ChunkRegeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            ChunkRegenerator.getResourceLocation("textures/gui/chunk_regenerator.png");

    public ChunkRegeneratorScreen(ChunkRegeneratorMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = Integer.MIN_VALUE;
        this.inventoryLabelX = Integer.MIN_VALUE;
    }

    public EnergyBarWidget energyBar;

    public Checkbox[][] checkboxes = new Checkbox[ChunkRegeneratorBlockEntity.RANGE][ChunkRegeneratorBlockEntity.RANGE];

    @Override
    protected void init() {
        super.init();
        int x = (width - this.imageWidth) / 2;
        int y = (height - this.imageHeight) / 2;

        for (int i = 0; i < ChunkRegeneratorBlockEntity.RANGE; i++) {
            for (int j = 0; j < ChunkRegeneratorBlockEntity.RANGE; j++) {
                int targetX = i;
                int targetY = j;
                checkboxes[i][j] = new Checkbox(x + i * 20 + 10, y + j * 20 + 20, 20, 20, Component.empty(), this.menu.blockEntity.targetChunks[i][j]) {
                    @Override
                    public void onPress() {
                        super.onPress();
                        ChunkRegeneratorConnection.INSTANCE.sendToServer(new ServerboundUpdateTargetChunksPacket(menu.containerId, targetX, targetY));
                    }
                };
                checkboxes[i][j].setTooltip(Tooltip.create(Component.translatable("gui.chunk_regenerator.chunk_regenerator.target_chunk", targetX - ChunkRegeneratorBlockEntity.RADIUS, targetY - ChunkRegeneratorBlockEntity.RADIUS)));
                addRenderableWidget(checkboxes[i][j]);
            }
        }

        addRenderableWidget(energyBar = new EnergyBarWidget(x + 162, y + 7, 5, 150, Component.empty(), this.menu::getStoredEnergy, this.menu::getMaxEnergy));

    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - this.imageWidth) / 2;
        int y = (height - this.imageHeight) / 2;



        guiGraphics.blit(TEXTURE,x,y,0,0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int i, int i1, float v) {
        this.renderBackground(graphics);
        super.render(graphics, i, i1, v);
        this.renderTooltip(graphics, i, i1);
    }
}
