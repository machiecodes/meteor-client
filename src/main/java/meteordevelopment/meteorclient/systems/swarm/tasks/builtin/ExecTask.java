/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.tasks.builtin;

import meteordevelopment.meteorclient.systems.swarm.tasks.Task;
import net.minecraft.nbt.CompoundTag;

public class ExecTask extends Task {
    @Override
    protected String type() {
        return "exec";
    }

    @Override
    public CompoundTag toTag() {
        return null;
    }

    @Override
    public Task fromTag(CompoundTag tag) {
        return null;
    }
}
