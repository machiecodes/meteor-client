/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.swarm.messages.Message;
import meteordevelopment.meteorclient.systems.swarm.messages.builtin.JoinServerMessage;
import meteordevelopment.meteorclient.systems.swarm.messages.builtin.LeaveServerMessage;
import meteordevelopment.meteorclient.systems.swarm.messages.builtin.TaskMessage;
import meteordevelopment.meteorclient.systems.swarm.tasks.Task;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SwarmHost {
    private final Swarm swarm = Swarm.get();

    private final Thread hostThread;
    private final ServerSocket hostSocket;
    private final List<SwarmConnection> connections = new ArrayList<>(8);
    private int nextId;

    private final Queue<SwarmConnection> pendingJoins = new ArrayDeque<>();
    private long lastJoinTime;


    public SwarmHost() throws IOException {
        hostSocket = new ServerSocket(Swarm.get().port.get());
        hostSocket.setSoTimeout(50);

        hostThread = new Thread(this::hostLoop, "swarm-host");
        hostThread.setDaemon(true);
        hostThread.start();

        MeteorClient.EVENT_BUS.subscribe(this);
    }

    private void hostLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = hostSocket.accept();
                String label = "swarm-host" + nextId++;

                SwarmConnection conn = new SwarmConnection(socket, connections::remove, this::handleMessage, label);
                conn.open();
                connections.add(conn);

                if (!swarm.followHost.get()) continue;

                ServerData server = mc.getCurrentServer();

                if (server != null) {
                    ServerAddress address = ServerAddress.parseString(server.ip);
                    String ip = address.getHost();
                    int port = address.getPort();

                    Message joinMessage = new JoinServerMessage(ip, port);
                    conn.send(joinMessage);
                } else {
                    conn.send(new LeaveServerMessage());
                }
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

        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    public void sendMessage(Message message) {
        for (SwarmConnection conn : connections) conn.send(message);
    }

    public void sendTask(Task task) {
        List<Task> distributed = task.distribute(connections.size());
        for (int i = 0; i < connections.size(); i++) {
            connections.get(i).send(new TaskMessage(distributed.get(i)));
        }
    }

    private void handleMessage(Message message) {

    }

    public void handleJoinLeave(ServerData server) {
        if (server != null) {
            pendingJoins.clear();
            pendingJoins.addAll(connections);
        } else {
            pendingJoins.clear();
            for (SwarmConnection conn : connections) conn.send(new LeaveServerMessage());
        }
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        while (!pendingJoins.isEmpty() && System.currentTimeMillis() - lastJoinTime >= swarm.joinDelay.get()) {
            if (mc.getCurrentServer() == null) break;

            ServerData server = mc.getCurrentServer();
            ServerAddress address = ServerAddress.parseString(server.ip);
            String ip = address.getHost();
            int port = address.getPort();

            pendingJoins.remove().send(new JoinServerMessage(ip, port));

            if (swarm.staggerJoins.get()) {
                lastJoinTime = System.currentTimeMillis();
                break;
            }
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        lastJoinTime = System.currentTimeMillis();
        handleJoinLeave(mc.getCurrentServer());
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        handleJoinLeave(null);
    }
}
