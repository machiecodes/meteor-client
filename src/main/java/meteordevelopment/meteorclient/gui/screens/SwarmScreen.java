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
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.profiles.Profile;
import meteordevelopment.meteorclient.systems.profiles.Profiles;
import meteordevelopment.meteorclient.systems.swarm.Swarm;
import meteordevelopment.meteorclient.utils.render.color.Color;
import com.mojang.datafixers.util.Pair;

import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public class SwarmScreen extends TabScreen {
    private final Swarm swarm = Swarm.get();

    private WContainer hostSettingsContainer;
    private WContainer workerSettingsContainer;

    private WContainer controlsRoot;
    private WContainer controlSettingsContainer;
    private WLabel statusLabel;

    public SwarmScreen(GuiTheme theme, SwarmTab tab) {
        super(theme, tab);
    }

    @Override
    public void initWidgets() {
        WWindowController controller = add(new WWindowController()).widget();

        createHostSettingsWindow(controller);
        createWorkerSettingsWindow(controller);
        createControlsWindow(controller);
    }

    private void createHostSettingsWindow(WContainer c) {
        WWindow w = theme.window("Host Settings");
        w.id = "swarm-host-settings";
        c.add(w);

        w.view.scrollOnlyWhenMouseOver = true;
        w.view.maxHeight -= 20;

        hostSettingsContainer = w.view.add(theme.verticalList()).expandX().widget();
        hostSettingsContainer.add(theme.settings(swarm.hostSettings)).expandX();
    }

    private void createWorkerSettingsWindow(WContainer c) {
        WWindow w = theme.window("Worker Settings");
        w.id = "swarm-worker-settings";
        c.add(w);

        w.view.scrollOnlyWhenMouseOver = true;
        w.view.maxHeight -= 20;

        workerSettingsContainer = w.view.add(theme.verticalList()).expandX().widget();
        workerSettingsContainer.add(theme.settings(swarm.workerSettings)).expandX();
    }

    private void createControlsWindow(WContainer c) {
        WWindow w = theme.window("Controls");
        w.id = "swarm-controls";
        c.add(w);

        w.view.scrollOnlyWhenMouseOver = true;
        w.view.maxHeight -= 20;

        controlsRoot = w.view;
        populateControls();
    }

    private void populateControls() {
        controlSettingsContainer = null;

        WHorizontalList statusRow = controlsRoot.add(theme.horizontalList()).expandCellX().centerX().padVertical(6).widget();
        statusRow.add(theme.label("Status: ")).widget().color = Color.WHITE;
        statusLabel = statusRow.add(theme.label("")).widget();

        if (!swarm.isEnabled()) {
            controlSettingsContainer = controlsRoot.add(theme.verticalList()).expandX().minWidth(400).widget();
            controlSettingsContainer.add(theme.settings(swarm.connSettings)).expandX();
        }

        WSection controls = controlsRoot.add(theme.section("Controls")).expandX().minWidth(400).widget();

        if (!swarm.isEnabled()) {
            WButton enable = controls.add(theme.button("Enable")).expandX().widget();
            enable.action = () -> {
                if (swarm.mode.get() == Swarm.Mode.Host) {
                    swarm.enableHost();
                } else {
                    swarm.enableWorker();
                }

                reloadControls();
            };
        } else {
            WButton disable = controls.add(theme.button("Disable")).expandX().widget();
            disable.action = () -> {
                if (swarm.mode.get() == Swarm.Mode.Host) {
                    swarm.disableHost();
                } else {
                    swarm.disableWorker();
                }

                reloadControls();
            };
        }
    }

    private void reloadControls() {
        controlsRoot.clear();
        populateControls();
    }

    @Override
    public void tick() {
        if (controlSettingsContainer != null) swarm.connSettings.tick(controlSettingsContainer, theme);
        if (hostSettingsContainer != null) swarm.hostSettings.tick(hostSettingsContainer, theme);
        if (workerSettingsContainer != null) swarm.workerSettings.tick(workerSettingsContainer, theme);

        if (statusLabel != null) {
            Pair<String, Color> status = swarm.getStatus();
            statusLabel.set(status.getFirst());
            statusLabel.color = status.getSecond();
        }
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
