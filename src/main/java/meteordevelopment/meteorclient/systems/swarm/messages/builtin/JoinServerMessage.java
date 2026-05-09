/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.messages.builtin;

import meteordevelopment.meteorclient.systems.swarm.messages.Message;
import net.minecraft.nbt.CompoundTag;

public class JoinServerMessage extends Message {
    public String ip;
    public int port;

    public JoinServerMessage(){}

    public JoinServerMessage(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    protected String type() {
        return "join-server";
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type());

        tag.putString("ip", ip);
        tag.putInt("port", port);

        return tag;
    }

    @Override
    public Message fromTag(CompoundTag tag) {
        this.ip = tag.getString("ip").orElseThrow();
        this.port = tag.getInt("port").orElseThrow();

        return this;
    }
}
