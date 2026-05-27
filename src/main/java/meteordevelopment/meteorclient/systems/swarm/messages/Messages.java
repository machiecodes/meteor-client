/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.messages;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Messages {
    private static final Map<String, Supplier<Message>> REGISTRY = new HashMap<>();

    static {

    }

    private static void register(String type, Supplier<Message> supplier) {
        REGISTRY.put(type, supplier);
    }

    public static Message fromTag(CompoundTag tag) {
        var supplier = REGISTRY.get(tag.getString("type").orElse(null));
        if (supplier == null) MeteorClient.LOG.info("Received message of unknown type, ignoring.");
        return supplier == null ? null : supplier.get().fromTag(tag);
    }
}
