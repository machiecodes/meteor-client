/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.messages.builtin;

import meteordevelopment.meteorclient.systems.swarm.messages.Message;
import net.minecraft.nbt.CompoundTag;

public class LeaveServerMessage extends Message {
    @Override
    protected String type() {
        return "leave-server";
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type());

        return tag;
    }

    @Override
    public Message fromTag(CompoundTag tag) {
        return this;
    }
}
