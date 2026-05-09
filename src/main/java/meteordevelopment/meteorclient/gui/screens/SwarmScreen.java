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
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.profiles.Profile;
import meteordevelopment.meteorclient.systems.profiles.Profiles;
import meteordevelopment.meteorclient.systems.swarm.Swarm;
import meteordevelopment.meteorclient.systems.swarm.messages.builtin.LoadProfileMessage;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.Util;

import java.util.List;

import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public class SwarmScreen extends TabScreen {
    private final Swarm swarm = Swarm.get();

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

        settingsContainer = w.add(theme.verticalList()).expandX().widget();
        settingsContainer.add(theme.settings(swarm.settings)).expandX();

        w.add(theme.horizontalSeparator()).expandX();

        WButton guide = w.add(theme.button("Guide")).expandX().widget();
        guide.action = () -> Util.getPlatform().openUri("https://github.com/MeteorDevelopment/meteor-client/wiki/Swarm-Guide");
    }

    private void createControlsWindow(WContainer c) {
        WWindow w = theme.window("Controls");
        w.id = "swarm-controls";
        c.add(w);

        w.view.scrollOnlyWhenMouseOver = true;
        w.view.maxHeight -= 20;
        w.minWidth = 400;

        WHorizontalList statusRow = w.add(theme.horizontalList()).expandX().widget();

        statusRow.add(theme.label("Status: "));
        statusLabel = statusRow.add(theme.label("")).widget();

        WHorizontalList controlRow = w.add(theme.horizontalList()).expandX().widget();

        WButton enableButton = controlRow.add(theme.button("Enable")).expandX().widget();
        enableButton.action = swarm::enable;

        WButton disableButton = controlRow.add(theme.button("Disable")).expandX().widget();
        disableButton.action = swarm::disable;

        List<Profile> profiles = Profiles.get().getAll();
        if (!profiles.isEmpty()) {
            String[] names = profiles.stream().map(p -> p.name.get()).toArray(String[]::new);

            WHorizontalList profileRow = w.add(theme.horizontalList()).expandX().widget();

            WButton loadProfileButton = profileRow.add(theme.button("Load Profile")).expandX().widget();
            WDropdown<String> profileDropdown = profileRow.add(theme.dropdown(names, names[0])).widget();

            loadProfileButton.action = () -> {
                Profile selected = Profiles.get().get(profileDropdown.get());
                if (selected != null && swarm.isEnabled() && swarm.isHost()) {
                    swarm.host.sendMessage(new LoadProfileMessage(selected));
                }
            };

            w.add(theme.horizontalSeparator()).expandX();
        }
    }

    @Override
    public void tick() {
        super.tick();

        swarm.settings.tick(settingsContainer, theme);

        String err = swarm.getErrorMessage();

        if (err != null) {
            statusLabel.set(err);
            statusLabel.color = Color.RED;
        } else if (!swarm.isEnabled()) {
            statusLabel.set("Disabled");
            statusLabel.color = Color.GRAY;
        } else if (swarm.isHost()) {
            statusLabel.set("Listening on port " + swarm.port.get());
            statusLabel.color = Color.GREEN;
        } else {
            statusLabel.set("Connected to host at " + swarm.ip.get() + ":" + swarm.port.get());
            statusLabel.color = Color.GREEN;
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
