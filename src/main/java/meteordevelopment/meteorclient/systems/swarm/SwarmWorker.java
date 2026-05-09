/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.swarm.messages.Message;
import meteordevelopment.meteorclient.systems.swarm.messages.builtin.JoinServerMessage;
import meteordevelopment.meteorclient.systems.swarm.messages.builtin.LeaveServerMessage;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SwarmWorker {
    private final Swarm swarm = Swarm.get();
    private final SwarmConnection connection;

    private final Queue<String> chatQueue = new ArrayDeque<>();
    private long lastSendTime;

    public SwarmWorker() throws IOException {
        connection = new SwarmConnection(swarm.ip.get(), swarm.port.get(),
            (_) -> swarm.disable(), this::handleMessage,"swarm-worker");
        connection.open();

        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void stop() {
        connection.close();

        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    private void handleMessage(Message message) {
        if (message instanceof JoinServerMessage m) {
            mc.execute(() -> {
                ServerData current = mc.getCurrentServer();
                if (current != null) {
                    ServerAddress currentAddress = ServerAddress.parseString(current.ip);
                    if (currentAddress.getHost().equals(m.ip) && currentAddress.getPort() == m.port) return;
                    disconnect();
                }

                ServerAddress address = new ServerAddress(m.ip, m.port);
                ServerData data = new ServerData("Swarm", m.ip + ":" + m.port, ServerData.Type.OTHER);
                ConnectScreen.startConnecting(new TitleScreen(), mc, address, data, false, null);
            });
        } else if (message instanceof LeaveServerMessage) {
            mc.execute(this::disconnect);
        }
    }

    private void disconnect() {
        chatQueue.clear();
        if (mc.getCurrentServer() == null || !Utils.canUpdate()) return;
        mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket(Component.empty()));
    }

    private void sendChatMsg(String msg) {
        chatQueue.add(msg);
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        while (!chatQueue.isEmpty() && System.currentTimeMillis() - lastSendTime >= swarm.chatDelay.get()) {
            ChatUtils.sendPlayerMsg(chatQueue.remove());
            lastSendTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        if (!swarm.autoLogin.get()) return;

        String key = mc.getCurrentServer().ip + "|" + mc.player.getName().getString();
        String password = swarm.logins.getOrDefault(key, swarm.password.get());

        if (swarm.logins.containsKey(key)) {
            sendChatMsg(swarm.loginCommand.get().replace("{password}", password));
        } else {
            for (String command: swarm.registerCommands.get()) {
                sendChatMsg(command.replace("{password}", password));
            }
            swarm.logins.put(key, password);
            swarm.save();
        }
    }
}
