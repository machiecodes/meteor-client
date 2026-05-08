/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import net.minecraft.nbt.CompoundTag;

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

    private final Setting<String> ipAddress = sgGeneral.add(new StringSetting.Builder()
        .name("IP")
        .description("The IP address to connect to.")
        .defaultValue("localhost")
        .visible(() -> mode.get() == Mode.Worker)
        .build()
    );

    private final Setting<Integer> port = sgGeneral.add(new IntSetting.Builder()
        .name("port")
        .description("The port to connect to.")
        .defaultValue(6000)
        .range(1, 65535)
        .noSlider()
        .build()
    );

    private final Setting<Integer> heartbeatInterval = sgGeneral.add(new IntSetting.Builder()
        .name("heartbeat-interval")
        .description("How often in milliseconds to test if all connections are healthy.")
        .defaultValue(3_000)
        .min(500)
        .sliderMax(10_000)
        .build()
    );

    private final Setting<Integer> timeoutThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("timeout-threshold")
        .description("How many heartbeats must be dropped before a connection is severed.")
        .defaultValue(5)
        .min(3)
        .sliderMax(10)
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
        .name("handle-logins")
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
        .description("The command to register an account on the server.")
        .defaultValue("/register CHANGEME CHANGEME")
        .visible(() -> mode.get() == Mode.Worker && followHost.get() && autoLogin.get())
        .build()
    );

    private final Setting<String> loginCommand = sgServers.add(new StringSetting.Builder()
        .name("login-command")
        .description("The command to login to the server.")
        .defaultValue("/login CHANGEME")
        .visible(() -> mode.get() == Mode.Worker && followHost.get() && autoLogin.get())
        .build()
    );

    // Host



    // Worker



    public Swarm() {
        super("swarm");
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
