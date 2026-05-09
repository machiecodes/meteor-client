/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.tasks;

import meteordevelopment.meteorclient.utils.misc.ISerializable;

import java.util.Collections;
import java.util.List;

public abstract class Task implements ISerializable<Task> {
    protected abstract String type();

    public List<Task> distribute(int workerCount) {
        return Collections.nCopies(workerCount, this);
    }

}
