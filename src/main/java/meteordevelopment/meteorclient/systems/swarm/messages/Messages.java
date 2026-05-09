/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.messages;

import meteordevelopment.meteorclient.systems.swarm.messages.builtin.JoinServerMessage;
import meteordevelopment.meteorclient.systems.swarm.messages.builtin.LeaveServerMessage;
import meteordevelopment.meteorclient.systems.swarm.messages.builtin.LoadProfileMessage;
import meteordevelopment.meteorclient.systems.swarm.messages.builtin.TaskMessage;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Messages {
    private static final Map<String, Supplier<Message>> REGISTRY = new HashMap<>();

    static {
        register("join-server", JoinServerMessage::new);
        register("leave-server", LeaveServerMessage::new);
        register("load-profile", LoadProfileMessage::new);
        register("run-task", TaskMessage::new);
    }

    private static void register(String type, Supplier<Message> supplier) {
        REGISTRY.put(type, supplier);
    }

    public static Message fromTag(CompoundTag tag) {
        var supplier = REGISTRY.get(tag.getString("type").orElseThrow());
        return supplier.get().fromTag(tag);
    }
}
