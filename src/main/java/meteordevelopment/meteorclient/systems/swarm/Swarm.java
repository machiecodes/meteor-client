/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import com.mojang.datafixers.util.Pair;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.swarm.tasks.Task;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.nbt.CompoundTag;

import java.io.IOException;

public class Swarm extends System<Swarm> {
    public final Settings connSettings = new Settings();
    public final Settings hostSettings = new Settings();
    public final Settings workerSettings = new Settings();

    // CONNECTION

    private final SettingGroup sgConn = connSettings.createGroup("Connection");

    public final EnumSetting<Mode> mode = sgConn.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Whether to act as the host or a worker.")
        .defaultValue(Mode.Worker)
        .build()
    );

    public final StringSetting ip = sgConn.add(new StringSetting.Builder()
        .name("IP")
        .description("Host IP address to connect to.")
        .defaultValue("localhost")
        .build()
    );

    public final IntSetting port = sgConn.add(new IntSetting.Builder()
        .name("port")
        .description("Port to use for the swarm connection.")
        .defaultValue(6000)
        .range(1024, 65535)
        .noSlider()
        .build()
    );

    private boolean enabled;
    private SwarmHost host;
    private SwarmWorker worker;
    private Task task;

    private String statusMessage;
    private Color statusColor;
    private long statusTime;

    public Swarm() {
        super("swarm");
    }

    public void enableHost() {
        if (isHost()) return;
        disableWorker();

        try {
            host = new SwarmHost();
            enabled = true;
            MeteorClient.LOG.info("Swarm enabled as host, listening on port {}", port.get());
        } catch (IOException e) {
            MeteorClient.LOG.error("Failed to start host server on port {}", port.get(), e);
            setStatus("Failed to start host server", Color.RED);
        }
    }

    public void disableHost() {
        if (host == null) return;

        host.stop();
        host = null;
        enabled = false;

        MeteorClient.LOG.info("Swarm disabled");
    }

    public void enableWorker() {
        if (isWorker()) return;
        disableHost();

        try {
            MeteorClient.LOG.info("Swarm enabled as worker, connecting to port {}", port.get());
            worker = new SwarmWorker();
            enabled = true;
        } catch (IOException e) {
            MeteorClient.LOG.error("Failed to connect to host at {}:{}", ip.get(), port.get());
            setStatus("Failed to connect to host", Color.RED);
        }
    }

    public void disableWorker() {
        if (worker == null) return;

        worker.stop();
        worker = null;
        enabled = false;

        MeteorClient.LOG.info("Swarm disabled");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHost() {
        return enabled && host != null;
    }

    public boolean isWorker() {
        return enabled && worker != null;
    }

    // Tasks

    public void setTask(Task task) {

    }

    public Task getTask() {
        return task;
    }

    // UI

    public void setStatus(String status, Color color) {
        statusMessage = status;
        statusColor = color;
        statusTime = java.lang.System.currentTimeMillis();
    }

    public Pair<String, Color> getStatus() {
        int TWO_SECONDS = 2000;
        if (java.lang.System.currentTimeMillis() - statusTime > TWO_SECONDS) {
            statusMessage = null;
        }

        if (statusMessage != null) {
            return new Pair<>(statusMessage, statusColor);
        }

        if (!enabled) {
            return new Pair<>("Disabled", Color.GRAY);
        }

        if (mode.get() == Mode.Host) {
            return new Pair<>("Hosting on localhost:%d".formatted(port.get()), Color.GREEN);
        }

        return new Pair<>("Connected to host at %s:%d".formatted(ip.get(), port.get()), Color.GREEN);
    }

    // System

    public static Swarm get() {
        return Systems.get(Swarm.class);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.put("connSettings", connSettings.toTag());
        tag.put("hostSettings", hostSettings.toTag());
        tag.put("workerSettings", workerSettings.toTag());

        return tag;
    }

    @Override
    public Swarm fromTag(CompoundTag tag) {
        connSettings.fromTag(tag.getCompoundOrEmpty("connSettings"));
        hostSettings.fromTag(tag.getCompoundOrEmpty("hostSettings"));
        workerSettings.fromTag(tag.getCompoundOrEmpty("workerSettings"));

        return this;
    }

    public enum Mode {
        Host,
        Worker
    }
}
