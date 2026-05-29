/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;

public class SwarmCommand extends Command {
    private final static SimpleCommandExceptionType SWARM_NOT_ENABLED = new SimpleCommandExceptionType(
        Component.literal("Swarm must be enabled to use this command."));
    private final static SimpleCommandExceptionType SWARM_NOT_HOST = new SimpleCommandExceptionType(
        Component.literal("This command can only be used on the host instance."));
    private final static SimpleCommandExceptionType SWARM_NOT_WORKER = new SimpleCommandExceptionType(
        Component.literal("This command can only be used on worker instances."));
    private final static SimpleCommandExceptionType BARITONE_NOT_PRESENT = new SimpleCommandExceptionType(
        Component.literal("Baritone must be present to use this command."));

    public SwarmCommand() {
        super("swarm", "Control Swarm via commands.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {

    }
}
