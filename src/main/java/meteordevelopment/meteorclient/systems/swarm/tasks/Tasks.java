/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.tasks;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Tasks {
    private static final Map<String, Supplier<Task>> REGISTRY = new HashMap<>();

    static {

    }

    public static void register(String type, Supplier<Task> factory) {
        REGISTRY.put(type, factory);
    }

    public static Task fromTag(CompoundTag tag) {
        var supplier = REGISTRY.get(tag.getString("type").orElseThrow());
        return supplier == null ? null : supplier.get().fromTag(tag);
    }
}
