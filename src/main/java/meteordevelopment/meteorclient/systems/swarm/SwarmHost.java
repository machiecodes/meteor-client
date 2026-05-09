/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.MeteorClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class SwarmHost {
    private final Thread hostThread;
    private final ServerSocket hostSocket;

    private final List<SwarmConnection> connections = new ArrayList<>(8);
    private int nextId;

    public SwarmHost() throws IOException {
        hostSocket = new ServerSocket(Swarm.get().port.get());
        hostSocket.setSoTimeout(50);

        hostThread = new Thread(this::hostLoop, "swarm-host");
        hostThread.setDaemon(true);
        hostThread.start();
    }

    private void hostLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = hostSocket.accept();
                String label = "swarm-host" + nextId++;
                SwarmConnection conn = new SwarmConnection(socket, connections::remove, label);

                conn.open();
                connections.add(conn);
            } catch (SocketException | SocketTimeoutException ignored) {
            } catch (IOException e) {
                MeteorClient.LOG.error("", e);
            }
        }
    }

    public void stop() {
        hostThread.interrupt();

        List<SwarmConnection> toClose = new ArrayList<>(connections);
        connections.clear();
        for (SwarmConnection conn : toClose) conn.close();

        try {
            hostSocket.close();
        } catch (IOException ignored) {
        }
    }
}
