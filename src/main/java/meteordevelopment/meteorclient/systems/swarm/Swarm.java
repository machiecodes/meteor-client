/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.PostInit;
import net.minecraft.nbt.CompoundTag;

import java.io.IOException;

public class Swarm extends System<Swarm> {
    public SwarmHost host;
    public SwarmWorker worker;
    private boolean enabled;

    public final Settings settings = new Settings();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMisc = settings.createGroup("Misc");

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Whether this instance should act as a worker or the host.")
        .defaultValue(Mode.Worker)
        .build()
    );

    public final Setting<String> ip = sgGeneral.add(new StringSetting.Builder()
        .name("IP")
        .description("The IP address to connect to.")
        .defaultValue("localhost")
        .visible(() -> mode.get() == Mode.Worker)
        .build()
    );

    public final Setting<Integer> port = sgGeneral.add(new IntSetting.Builder()
        .name("port")
        .description("The port to connect to.")
        .defaultValue(6000)
        .range(1, 65535)
        .noSlider()
        .build()
    );

    public final Setting<Boolean> autoEnable = sgMisc.add(new BoolSetting.Builder()
        .name("auto-enable")
        .description("Enable Swarm automatically after the client loads.")
        .defaultValue(false)
        .build()
    );




    public Swarm() {
        super("swarm");
    }

    private String errorMessage;
    private long errorTime;

    public void enable() {
        disable();

        if (mode.get() == Mode.Host) {
            try {
                host = new SwarmHost();
                enabled = true;
                MeteorClient.LOG.info("Swarm enabled as host, listening on port {}", port.get());
            } catch (IOException e) {
                MeteorClient.LOG.error("Failed to start host server on port {}", port.get(), e);
                setErrorMessage("Failed to start host server");

            }
        } else {
            try {
                MeteorClient.LOG.info("Swarm enabled as worker, connecting to port {}", port.get());
                worker = new SwarmWorker();
                enabled = true;
            } catch (IOException e) {
                MeteorClient.LOG.error("Failed to connect to host at {}:{}", ip.get(), port.get());
                setErrorMessage("Failed to connect to host");
            }
        }
    }

    public void disable() {
        if (host != null) {
            host.stop();
            host = null;
            MeteorClient.LOG.info("Swarm disabled");
        }

        if (worker != null) {
            worker.stop();
            worker = null;
            MeteorClient.LOG.info("Swarm disabled");
        }

        enabled = false;

    }

    @PostInit
    public static void postInit() {
        if (!Swarm.get().autoEnable.get()) return;
        Swarm.get().enable();
    }

    private void setErrorMessage(String message) {
        errorMessage = message;
        errorTime = java.lang.System.currentTimeMillis();

    }

    public String getErrorMessage() {
        int THREE_SECONDS = 3000;
        if (errorMessage != null && java.lang.System.currentTimeMillis() - errorTime > THREE_SECONDS) {
            errorMessage = null;
        }
        return errorMessage;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHost() {
        return mode.get() == Mode.Host;
    }

    public static Swarm get() {
        return Systems.get(Swarm.class);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putString("version", MeteorClient.VERSION.toString());
        tag.put("settings", settings.toTag());

        return tag;
    }

    @Override
    public Swarm fromTag(CompoundTag tag) {
        if (tag.contains("settings")) settings.fromTag(tag.getCompoundOrEmpty("settings"));

        return this;
    }

    public enum Mode {
        Host,
        Worker
    }
}
