/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.swarm.tasks.Task;
import net.minecraft.nbt.CompoundTag;

import java.io.IOException;

public class Swarm extends System<Swarm> {
    public final Settings hostSettings = new Settings();
    public final Settings workerSettings = new Settings();



    private boolean enabled;
    public String ip;
    public int port;

    private SwarmHost host;
    private SwarmWorker worker;
    private Task task;

    private String errorMessage;
    private long errorTime;

    public Swarm() {
        super("swarm");
    }

    // ENABLE/DISABLE

    public void enableHost() {
        if (isHost()) return;
        disableWorker();

        try {
            host = new SwarmHost();
            enabled = true;
            MeteorClient.LOG.info("Swarm enabled as host, listening on port {}", port);
        } catch (IOException e) {
            MeteorClient.LOG.error("Failed to start host server on port {}", port, e);
            setErrorMessage("Failed to start host server");
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
            MeteorClient.LOG.info("Swarm enabled as worker, connecting to port {}", port);
            worker = new SwarmWorker();
            enabled = true;
        } catch (IOException e) {
            MeteorClient.LOG.error("Failed to connect to host at {}:{}", ip, port);
            setErrorMessage("Failed to connect to host");
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

    // TASKS

    public void setTask(Task task) {

    }

    public Task getTask() {
        return task;
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

    public static Swarm get() {
        return Systems.get(Swarm.class);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putString("version", MeteorClient.VERSION.toString());
        tag.put("settings", hostSettings.toTag());

        return tag;
    }

    @Override
    public Swarm fromTag(CompoundTag tag) {
        if (tag.contains("settings")) hostSettings.fromTag(tag.getCompoundOrEmpty("settings"));

        return this;
    }

    public enum Mode {
        Host,
        Worker
    }
}
