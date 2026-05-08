/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.nbt.CompoundTag;

public class Swarm extends System<Swarm> {
    public Swarm() {
        super("swarm");
    }

    public static Swarm get() {
        return Systems.get(Swarm.class);
    }

    @Override
    public CompoundTag toTag() {
        return super.toTag();
    }

    @Override
    public Swarm fromTag(CompoundTag tag) {
        return super.fromTag(tag);
    }
}
