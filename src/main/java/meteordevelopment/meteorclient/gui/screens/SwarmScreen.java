/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.builtin.SwarmTab;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.swarm.Swarm;
import net.minecraft.util.Util;

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
        createSettingsWindow();
        createControlsWindow();
    }

    private void createSettingsWindow() {
        settingsWindow = theme.window("Settings");
        settingsWindow.id = "swarm-settings";
        add(settingsWindow);

        settingsWindow.view.scrollOnlyWhenMouseOver = true;
        settingsWindow.view.maxHeight -= 20;

        settingsContainer = settingsWindow.add(theme.verticalList()).expandX().widget();
        settingsContainer.add(theme.settings(swarm.settings)).expandX();

        settingsWindow.add(theme.horizontalSeparator()).expandX();

        WButton guide = settingsWindow.add(theme.button("Guide")).expandX().widget();
        guide.action = () -> Util.getPlatform().openUri("https://github.com/MeteorDevelopment/meteor-client/wiki/Swarm-Guide");
    }

    private void createControlsWindow() {
        controlsWindow = theme.window("Controls");
        controlsWindow.id = "swarm-controls";
        add(controlsWindow);

        controlsWindow.view.scrollOnlyWhenMouseOver = true;
        controlsWindow.view.maxHeight -= 20;

        WHorizontalList bottom = controlsWindow.add(theme.horizontalList()).expandX().widget();

        WButton start = bottom.add(theme.button("Start")).expandX().widget();
        start.action = () -> {};

        WButton stop = bottom.add(theme.button("Stop")).expandX().widget();
        stop.action = () -> {};
    }

    @Override
    public void tick() {
        super.tick();

        swarm.settings.tick(settingsContainer, theme);
    }
}
