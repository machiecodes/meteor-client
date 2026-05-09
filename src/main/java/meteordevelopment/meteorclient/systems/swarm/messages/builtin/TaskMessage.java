/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.messages.builtin;

import meteordevelopment.meteorclient.systems.swarm.messages.Message;
import meteordevelopment.meteorclient.systems.swarm.tasks.Task;
import meteordevelopment.meteorclient.systems.swarm.tasks.Tasks;
import net.minecraft.nbt.CompoundTag;

public class TaskMessage extends Message {
    public Task task;

    public TaskMessage() {}

    public TaskMessage(Task task) {
        this.task = task;
    }

    @Override
    protected String type() {
        return "run-task";
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type());
        tag.put("task", task.toTag());
        return tag;
    }

    @Override
    public Message fromTag(CompoundTag tag) {
        task = Tasks.fromTag(tag.getCompoundOrEmpty("task"));
        return this;
    }
}
