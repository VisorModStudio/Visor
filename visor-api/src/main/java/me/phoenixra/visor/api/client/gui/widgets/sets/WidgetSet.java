package me.phoenixra.visor.api.client.gui.widgets.sets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WidgetSet {

    <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets();

    <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets();


    void onPreRender(@NotNull GuiGraphics guiGraphics,
                     int mouseX, int mouseY,
                     float partialTicks);
    void onTick();



}
