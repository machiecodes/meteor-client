/*
 * This file is part of the Meteor Client distribution (https://github.com/MachieCodes/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.swarm.messages.builtin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.macros.Macros;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.profiles.Profile;
import meteordevelopment.meteorclient.systems.profiles.Profiles;
import meteordevelopment.meteorclient.systems.swarm.Swarm;
import meteordevelopment.meteorclient.systems.swarm.messages.Message;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadProfileMessage extends Message {
    private Profile profile;

    public LoadProfileMessage() {}

    public LoadProfileMessage(Profile profile) {
        this.profile = profile;
    }

    @Override
    protected String type() {
        return "load-profile";
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type());
        tag.putString("name", profile.name.get());

        ListTag loadOnJoinTag = new ListTag();
        for (String s : profile.loadOnJoin.get()) loadOnJoinTag.add(StringTag.valueOf(s));
        tag.put("loadOnJoin", loadOnJoinTag);

        File folder = profile.getFile();
        if (profile.modules.get()) readFile(tag, folder, "modules");
        if (profile.hud.get()) readFile(tag, folder, "hud");
        if (profile.macros.get()) readFile(tag, folder, "macros");
        if (profile.swarm.get()) readFile(tag, folder, "swarm");
        if (profile.waypoints.get()) readFile(tag, folder, "waypoints");

        return tag;
    }

    @Override
    public Message fromTag(CompoundTag tag) {
        String name = tag.getString("name").orElseThrow();

        Profile profile = Profiles.get().get(name);
        if (profile == null) {
            profile = new Profile();
            profile.name.set(name);
        }

        ListTag loadOnJoinTag = tag.getListOrEmpty("loadOnJoin");
        List<String> loadOnJoin = new ArrayList<>(loadOnJoinTag.size());
        for (Tag t : loadOnJoinTag) loadOnJoin.add(t.asString().orElse(""));
        profile.loadOnJoin.set(loadOnJoin);

        profile.modules.set(tag.contains("modules"));
        profile.hud.set(tag.contains("hud"));
        profile.macros.set(tag.contains("macros"));
        profile.swarm.set(tag.contains("swarm"));
        profile.waypoints.set(tag.contains("waypoints"));

        if (tag.contains("modules")) Modules.get().fromTag(tag.getCompoundOrEmpty("modules"));
        if (tag.contains("hud")) Hud.get().fromTag(tag.getCompoundOrEmpty("hud"));
        if (tag.contains("macros")) Macros.get().fromTag(tag.getCompoundOrEmpty("macros"));
        if (tag.contains("swarm")) Swarm.get().fromTag(tag.getCompoundOrEmpty("swarm"));
        if (tag.contains("waypoints")) Waypoints.get().fromTag(tag.getCompoundOrEmpty("waypoints"));

        Profiles.get().add(profile);
        profile.save();
        profile.load();

        return this;
    }

    private void readFile(CompoundTag tag, File folder, String name) {
        File file = new File(folder, name + ".nbt");
        if (!file.exists()) return;
        try {
            tag.put(name, NbtIo.read(file.toPath()));
        } catch (IOException e) {
            MeteorClient.LOG.error("Error reading profile file {}", file, e);
        }
    }
}
