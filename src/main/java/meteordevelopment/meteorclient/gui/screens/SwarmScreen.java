/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.builtin.SwarmTab;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.swarm.Swarm;
import net.minecraft.util.Util;

import static meteordevelopment.meteorclient.utils.Utils.getWindowHeight;
import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public class SwarmScreen extends TabScreen {
    private final Swarm swarm = Swarm.get();

    private WWindow settingsWindow;
    private WContainer settingsContainer;

    private WWindow controlsWindow;
    private WContainer controlsContainer;

    public SwarmScreen(GuiTheme theme, SwarmTab tab) {
        super(theme, tab);
    }

    @Override
    public void initWidgets() {
        WWindowController controller = add(new WWindowController()).widget();
        settingsWindow = createSettingsWindow(controller);
        controlsWindow = createControlsWindow(controller);
    }

    private WWindow createSettingsWindow(WContainer c) {
        WWindow w = theme.window("Settings");
        w.id = "swarm-settings";
        c.add(w);

        w.view.scrollOnlyWhenMouseOver = true;
        w.view.maxHeight -= 20;

        settingsContainer = w.add(theme.verticalList()).expandX().widget();
        settingsContainer.add(theme.settings(swarm.settings)).expandX();

        w.add(theme.horizontalSeparator()).expandX();

        WButton guide = w.add(theme.button("Guide")).expandX().widget();
        guide.action = () -> Util.getPlatform().openUri("https://github.com/MeteorDevelopment/meteor-client/wiki/Swarm-Guide");

        return w;
    }

    private WWindow createControlsWindow(WContainer c) {
        WWindow w = theme.window("Controls");
        w.id = "swarm-controls";
        c.add(w);

        w.view.scrollOnlyWhenMouseOver = true;
        w.view.maxHeight -= 20;

        WHorizontalList bottom = w.add(theme.horizontalList()).expandX().widget();

        WButton start = bottom.add(theme.button("Start")).expandX().widget();
        start.action = () -> {};

        WButton stop = bottom.add(theme.button("Stop")).expandX().widget();
        stop.action = () -> {};

        return w;
    }

    @Override
    public void tick() {
        super.tick();

        swarm.settings.tick(settingsContainer, theme);
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
