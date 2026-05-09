/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.client.renderer.item.properties.numeric.Time;
import net.minecraft.nbt.CompoundTag;

import java.io.IOException;

public class Swarm extends System<Swarm> {
    public final Settings settings = new Settings();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgServers = settings.createGroup("Servers");

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
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

    // Servers

    private final Setting<Boolean> followHost = sgServers.add(new BoolSetting.Builder()
        .name("follow-host")
        .description("Workers will automatically join/leave servers with the host.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> staggerJoins = sgServers.add(new BoolSetting.Builder()
        .name("stagger-joins")
        .description("Add a delay between each worker join to circumvent IP rate-limiting plugins.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Host && followHost.get())
        .build()
    );

    private final Setting<Integer> joinDelay = sgServers.add(new IntSetting.Builder()
        .name("join-delay")
        .description("The time in milliseconds between each worker joining.")
        .defaultValue(1000)
        .min(0)
        .sliderMax(5000)
        .visible(() -> mode.get() == Mode.Host && followHost.get() && staggerJoins.get())
        .build()
    );

    private final Setting<Boolean> autoLogin = sgServers.add(new BoolSetting.Builder()
        .name("auto-login")
        .description("Automatically register/login on cracked servers with auth plugins.")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Worker && followHost.get())
        .build()
    );

    private final Setting<Boolean> loginAfterRegister = sgServers.add(new BoolSetting.Builder()
        .name("login-after-register")
        .description("Send the login command after registering.")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Worker && followHost.get() && autoLogin.get())
        .build()
    );

    private final Setting<String> registerCommand = sgServers.add(new StringSetting.Builder()
        .name("register-command")
        .description("The command to register an account on the server, including password.")
        .defaultValue("/register CHANGEME CHANGEME")
        .visible(() -> mode.get() == Mode.Worker && followHost.get() && autoLogin.get())
        .build()
    );

    private final Setting<String> loginCommand = sgServers.add(new StringSetting.Builder()
        .name("login-command")
        .description("The command to login to the server, including password.")
        .defaultValue("/login CHANGEME")
        .visible(() -> mode.get() == Mode.Worker && followHost.get() && autoLogin.get())
        .build()
    );

    // Host



    // Worker


    private SwarmHost host;
    private SwarmWorker worker;
    private boolean enabled;

    private String errorMessage;
    private long errorTime;
    private final int THREE_SECONDS = 3000;


    public Swarm() {
        super("swarm");
    }

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

    private void setErrorMessage(String message) {
        errorMessage = message;
        errorTime = java.lang.System.currentTimeMillis();

    }

    public String getErrorMessage() {
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
