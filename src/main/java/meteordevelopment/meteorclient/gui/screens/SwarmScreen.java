/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.builtin.SwarmTab;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.swarm.Swarm;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.Util;

import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public class SwarmScreen extends TabScreen {
    private final Swarm swarm = Swarm.get();

    private static Swarm.Mode settingsMode;
    private WContainer settingsContainer;

    private WLabel statusLabel;

    public SwarmScreen(GuiTheme theme, SwarmTab tab) {
        super(theme, tab);
    }

    @Override
    public void initWidgets() {
        WWindowController controller = add(new WWindowController()).widget();
        createSettingsWindow(controller);
        createControlsWindow(controller);
    }

    private void createSettingsWindow(WContainer c) {
        WWindow w = theme.window("Settings");
        w.id = "swarm-settings";
        c.add(w);

        w.view.scrollOnlyWhenMouseOver = true;
        w.view.maxHeight -= 20;
    }

    private void createControlsWindow(WContainer c) {
        WWindow w = theme.window("Controls");
        w.id = "swarm-controls";
        c.add(w);

        w.view.scrollOnlyWhenMouseOver = true;
        w.view.maxHeight -= 20;
    }

    private static class WWindowController extends WContainer {
        @Override
        protected void onCalculateWidgetPositions() {
            double pad = theme.scale(4);
            double totalWidth = 0;

            for (Cell<?> cell : cells) {
                totalWidth += cell.widget().width;
            }
            totalWidth += pad * (cells.size() - 1);

            double x = (getWindowWidth() - totalWidth) / 2.0;
            double y = theme.scale(50);

            for (Cell<?> cell : cells) {
                cell.x = x;
                cell.y = y;

                cell.width = cell.widget().width;
                cell.height = cell.widget().height;

                cell.alignWidget();

                x += cell.width + pad;
            }
        }
    }
}
