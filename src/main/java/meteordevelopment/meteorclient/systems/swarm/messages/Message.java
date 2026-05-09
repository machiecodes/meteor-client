/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.messages;

import meteordevelopment.meteorclient.utils.misc.ISerializable;

public abstract class Message implements ISerializable<Message> {
    protected abstract String type();
}
