/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.systems.swarm.messages.Message;

import java.io.IOException;

public class SwarmWorker {
    private final Swarm swarm = Swarm.get();
    private final SwarmConnection connection;

    public SwarmWorker() throws IOException {
        connection = new SwarmConnection(swarm.ip, swarm.port,
            (_) -> swarm.disable(), this::handleMessage, "swarm-worker");
        connection.open();
    }

    public void stop() {
        connection.close();
    }

    private void handleMessage(Message message) {

    }
}
