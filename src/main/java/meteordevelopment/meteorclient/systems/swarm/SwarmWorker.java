/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import java.io.IOException;

public class SwarmWorker {
    private final Swarm swarm = Swarm.get();
    private final SwarmConnection connection;

    public SwarmWorker() throws IOException {
        connection = new SwarmConnection(swarm.ip.get(), swarm.port.get(),
            (_) -> swarm.disable(), "swarm-worker");
        connection.open();
    }

    public void stop() {
        connection.close();
    }
}
