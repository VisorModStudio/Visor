package me.phoenixra.visor.api.client.gui.widgets.lists;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

//@TODO
public class WidgetsList <E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {

    public WidgetsList(Minecraft minecraft, int width, int height,
                       int y0, int y1,
                       int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
    }

    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (this.getItemCount() == 0) {
            return null;
        } else if (!(event instanceof FocusNavigationEvent.ArrowNavigation)) {
            return super.nextFocusPath(event);
        } else {
            FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)event;
            E entry = (E)(this.getFocused());
            if (arrowNavigation.direction().getAxis() == ScreenAxis.HORIZONTAL && entry != null) {
                return ComponentPath.path(this, entry.nextFocusPath(event));
            } else {
                int i = -1;
                ScreenDirection screenDirection = arrowNavigation.direction();
                if (entry != null) {
                    i = entry.children().indexOf(entry.getFocused());
                }

                if (i == -1) {
                    switch (screenDirection) {
                        case LEFT:
                            i = Integer.MAX_VALUE;
                            screenDirection = ScreenDirection.DOWN;
                            break;
                        case RIGHT:
                            i = 0;
                            screenDirection = ScreenDirection.DOWN;
                            break;
                        default:
                            i = 0;
                    }
                }

                E entry2 = entry;

                ComponentPath componentPath;
                do {
                    entry2 = (E)(this.nextEntry(screenDirection, (entryx) -> !entryx.children().isEmpty(), entry2));
                    if (entry2 == null) {
                        return null;
                    }

                    componentPath = entry2.focusPathAtIndex(arrowNavigation, i);
                } while(componentPath == null);

                return ComponentPath.path(this, componentPath);
            }
        }

    }

    public void setFocused(@Nullable GuiEventListener focused) {
        super.setFocused(focused);
        if (focused == null) {
            this.setSelected(null);
        }

    }

    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.isFocused() ? NarrationPriority.FOCUSED : super.narrationPriority();
    }

    protected boolean isSelectedItem(int index) {
        return false;
    }

    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Environment(EnvType.CLIENT)
    public abstract static class Entry<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements ContainerEventHandler {
        @Nullable
        private GuiEventListener focused;
        @Setter @Getter
        private boolean dragging;

        public Entry() {
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public void setFocused(@Nullable GuiEventListener focused) {
            if (this.focused != null) {
                this.focused.setFocused(false);
            }

            if (focused != null) {
                focused.setFocused(true);
            }

            this.focused = focused;
        }

        @Nullable
        public GuiEventListener getFocused() {
            return this.focused;
        }

        @Nullable
        public ComponentPath focusPathAtIndex(FocusNavigationEvent event, int index) {
            if (this.children().isEmpty()) {
                return null;
            } else {
                ComponentPath componentPath = ((GuiEventListener)this.children().get(Math.min(index, this.children().size() - 1))).nextFocusPath(event);
                return ComponentPath.path(this, componentPath);
            }
        }

        @Nullable
        public ComponentPath nextFocusPath(FocusNavigationEvent event) {
            if (event instanceof FocusNavigationEvent.ArrowNavigation) {
                FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)event;
                byte var10000;
                switch (arrowNavigation.direction()) {
                    case LEFT:
                        var10000 = -1;
                        break;
                    case RIGHT:
                        var10000 = 1;
                        break;
                    case UP:
                    case DOWN:
                        var10000 = 0;
                        break;
                    default:
                        throw new IncompatibleClassChangeError();
                }

                int i = var10000;
                if (i == 0) {
                    return null;
                }

                int j = Mth.clamp(i + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1);

                for(int k = j; k >= 0 && k < this.children().size(); k += i) {
                    GuiEventListener guiEventListener = (GuiEventListener)this.children().get(k);
                    ComponentPath componentPath = guiEventListener.nextFocusPath(event);
                    if (componentPath != null) {
                        return ComponentPath.path(this, componentPath);
                    }
                }
            }

            return super.nextFocusPath(event);
        }
    }

}

