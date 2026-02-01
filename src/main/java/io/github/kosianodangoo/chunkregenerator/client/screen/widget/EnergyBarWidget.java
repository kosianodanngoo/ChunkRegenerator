package io.github.kosianodangoo.chunkregenerator.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;


public class EnergyBarWidget extends AbstractWidget {
    private Supplier<Integer> energyGetter;
    private Supplier<Integer> capacityGetter;
    private int color;

    public EnergyBarWidget(int x, int y, int width, int height, Component message, Supplier<Integer> energyGetter, Supplier<Integer> capacityGetter) {
        this(x, y, width, height, message, energyGetter, capacityGetter, 0xFFFF0000);
    }

    public EnergyBarWidget(int x, int y, int width, int height, Component message, Supplier<Integer> energyGetter, Supplier<Integer> capacityGetter, int color) {
        super(x, y, width, height, message);
        this.energyGetter = energyGetter;
        this.capacityGetter = capacityGetter;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getEnergy() {
        return energyGetter.get();
    }

    public int getMaxEnergy() {
        return capacityGetter.get();
    }

    protected void renderWidget(GuiGraphics guiGraphics, int var2, int var3, float var4) {
        guiGraphics.fill(this.getX(), this.getY() + this.getHeight() - Math.round((float)this.getHeight() * ((float)getEnergy() / (float)getMaxEnergy())), this.getX()+this.getWidth(), this.getY() + this.getHeight(), this.getColor());
        this.setTooltip(Tooltip.create(Component.translatable("gui.chunk_regenerator.energy_bar", getEnergy(), getMaxEnergy())));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.chunk_regenerator.energy_bar", getEnergy(), getMaxEnergy()));
    }
}
