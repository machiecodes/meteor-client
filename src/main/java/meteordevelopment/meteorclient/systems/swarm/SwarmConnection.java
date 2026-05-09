/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.swarm.messages.Message;
import meteordevelopment.meteorclient.systems.swarm.messages.Messages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class SwarmConnection {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final BlockingQueue<CompoundTag> outGoing = new LinkedBlockingQueue<>();

    private final Thread readLoop;
    private final Thread writeLoop;
    private volatile boolean running;

    final Consumer<SwarmConnection> onClose;
    final Consumer<Message> onMessage;
    final String label;

    public SwarmConnection(String ip, int port, Consumer<SwarmConnection> onClose,
                           Consumer<Message> onMessage, String label) throws IOException {
        this(new Socket(ip, port), onClose, onMessage, label);
    }

    public SwarmConnection(Socket socket, Consumer<SwarmConnection> onClose,
                           Consumer<Message> onMessage, String label) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        readLoop = new Thread(this::readLoop, label + "-read");
        readLoop.setDaemon(true);

        writeLoop = new Thread(this::writeLoop, label + "-write");
        writeLoop.setDaemon(true);

        this.onMessage = onMessage;
        this.onClose = onClose;
        this.label = label;
    }

    public void open() {
        running = true;
        readLoop.start();
        writeLoop.start();

        MeteorClient.LOG.info("Connection {} to {} opened.", label, getAddress());
    }

    public void close() {
        if (!running) return;
        running = false;

        readLoop.interrupt();
        writeLoop.interrupt();

        try {
            socket.close();
        } catch (IOException e) {
            MeteorClient.LOG.error("", e);
        }

        MeteorClient.LOG.info("Connection {} to {} closed.", label, getAddress());
        if (onClose != null) onClose.accept(this);
    }

    public void send(Message message) {
        try {
            outGoing.put(message.toTag());
        } catch (InterruptedException e) {
            MeteorClient.LOG.error("", e);
        }
    }

    private void readLoop() {
        try {
            while (running) {
                CompoundTag tag = NbtIo.read(in, NbtAccounter.unlimitedHeap());
                Message message = Messages.fromTag(tag);
                if (message != null) onMessage.accept(message);
            }
        } catch (EOFException | SocketException ignored) {
        } catch (IOException e) {
            MeteorClient.LOG.error("", e);
        } finally {
            close();
        }
    }

    private void writeLoop() {
        try {
            while (running) {
                CompoundTag message = outGoing.take();
                NbtIo.write(message, out);
                out.flush();
            }
        } catch (EOFException | InterruptedException ignored) {
        } catch (IOException e) {
            MeteorClient.LOG.error("", e);
        } finally {
            close();
        }
    }

    public String getAddress() {
        String ip = socket.getInetAddress().getHostAddress();
        return (ip.equals("127.0.0.1") ? "localhost" : ip) + ":" + socket.getPort();
    }
}
