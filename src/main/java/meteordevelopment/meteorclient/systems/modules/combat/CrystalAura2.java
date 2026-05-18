/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Set;

public class CrystalAura2 extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgSwitch = settings.createGroup("Switch");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgSupport = settings.createGroup("Support");
    private final SettingGroup sgFacePlace = settings.createGroup("Face Place");
    private final SettingGroup sgBreak = settings.createGroup("Break");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> mode112 = sgGeneral.add(new BoolSetting.Builder()
        .name("1.12-mode")
        .description("Will only place crystals in 2-high places, will not airplace support blocks.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates server-side towards the crystals being hit/placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> yawSteps = sgGeneral.add(new BoolSetting.Builder()
        .name("yaw-steps")
        .description("Limit the distance you can rotate in one tick, for strict anticheats.")
        .defaultValue(true)
        .visible(rotate::get)
        .build()
    );

    private final Setting<Double> maxStep = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-step")
        .description("Maximum number of degrees to rotate in one tick.")
        .defaultValue(180)
        .range(1, 180)
        .visible(() -> rotate.get() && yawSteps.get())
        .build()
    );

    // Targeting

    private final Setting<Double> targetRange = sgTargeting.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("How close entities must be to target them.")
        .defaultValue(10)
        .min(0)
        .sliderMax(16)
        .build()
    );

    private final Setting<Integer> maxTargets = sgTargeting.add(new IntSetting.Builder()
        .name("max-targets")
        .description("How many entities can be targeted at once.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    private final Setting<SortPriority> targetPriority = sgTargeting.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.ClosestAngle)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entitiesToTarget = sgTargeting.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Which types of entities to target.")
        .onlyAttackable()
        .defaultValue(EntityType.PLAYER, EntityType.WARDEN, EntityType.WITHER)
        .build()
    );

    private final Setting<Boolean> ignoreNakeds = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-nakeds")
        .description("Ignore players with no items.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignorePassive = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-passive")
        .description("Only attack neutral mobs if they're attacking you.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> predictMovement = sgTargeting.add(new BoolSetting.Builder()
        .name("predict-movement")
        .description("Naively predict the movement of targets.")
        .defaultValue(false)
        .build()
    );

    // Damage

    private final Setting<Double> minDamage = sgDamage.add(new DoubleSetting.Builder()
        .name("min-damage")
        .description("Minimum damage a crystal needs to deal to your target.")
        .defaultValue(6)
        .min(0)
        .build()
    );

    private final Setting<Double> maxDamage = sgDamage.add(new DoubleSetting.Builder()
        .name("max-damage")
        .description("Maximum damage crystals can deal to yourself.")
        .defaultValue(6)
        .range(0, 36)
        .sliderMax(36)
        .build()
    );

    private final Setting<Double> ignoreRatio = sgDamage.add(new DoubleSetting.Builder()
        .name("ignore-ratio")
        .description("Ignore min/max damage if you would deal this times more damage to your enemy than yourself.")
        .defaultValue(3.5)
        .min(1)
        .sliderMax(5)
        .build()
    );

    private final Setting<Boolean> antiSuicide = sgDamage.add(new BoolSetting.Builder()
        .name("anti-suicide")
        .description("Will not place or break crystals if they would kill you.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> totalDamage = sgDamage.add(new BoolSetting.Builder()
        .name("total-damage")
        .description("""
            Instead of finding a spot that deals the most damage to a single target,
            prioritize spots that would deal the most total damage to all targets.
            """)
        .defaultValue(true)
        .build()
    );

    // Switch

    private final Setting<SwitchMode> switchMode = sgSwitch.add(new EnumSetting.Builder<SwitchMode>()
        .name("switch-mode")
        .description("""
            If/how to automatically swap to crystals to place. Inventory mode
            bypasses switch delay but may not work on strict servers.
            """)
        .defaultValue(SwitchMode.Hotbar)
        .build()
    );

    private final Setting<Integer> switchDelay = sgSwitch.add(new IntSetting.Builder()
        .name("switch-delay")
        .description("How long to wait in ticks before breaking crystals after switching hotbar slots.")
        .defaultValue(0)
        .min(0)
        .build()
    );

    private final Setting<Boolean> noGapSwitch = sgSwitch.add(new BoolSetting.Builder()
        .name("no-gap-switch")
        .description("Won't auto switch if you're holding a gapple.")
        .defaultValue(true)
        .visible(() -> switchMode.get() != SwitchMode.Manual)
        .build()
    );

    private final Setting<Boolean> noBowSwitch = sgSwitch.add(new BoolSetting.Builder()
        .name("no-bow-switch")
        .description("Won't auto switch if you're holding a bow.")
        .defaultValue(true)
        .visible(() -> switchMode.get() != SwitchMode.Manual)
        .build()
    );

    private final Setting<Boolean> antiWeakness = sgSwitch.add(new BoolSetting.Builder()
        .name("anti-weakness")
        .description("Switch to tools/weapons if you have weakness so you can still break crystals.")
        .defaultValue(true)
        .build()
    );

    // Pause

    public final Setting<Double> pauseHealth = sgPause.add(new DoubleSetting.Builder()
        .name("pause-health")
        .description("Pauses when you go below a certain health.")
        .defaultValue(6)
        .range(0, 36)
        .sliderRange(0, 36)
        .build()
    );

    public final Setting<List<Module>> pauseModules = sgPause.add(new ModuleListSetting.Builder()
        .name("pause-modules")
        .description("Pauses while any of the selected modules are active.")
        .defaultValue(BedAura.class, AnchorAura.class)
        .build()
    );

    public final Setting<PauseMode> pauseOnUse = sgPause.add(new EnumSetting.Builder<PauseMode>()
        .name("pause-on-use")
        .description("Which processes should be paused while using an item.")
        .defaultValue(PauseMode.Place)
        .build()
    );

    public final Setting<PauseMode> pauseOnMine = sgPause.add(new EnumSetting.Builder<PauseMode>()
        .name("pause-on-mine")
        .description("Which processes should be paused while mining a block.")
        .defaultValue(PauseMode.None)
        .build()
    );

    private final Setting<Boolean> pauseOnLag = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-lag")
        .description("Whether to pause if the server is not responding.")
        .defaultValue(true)
        .build()
    );

    // Place

    private final Setting<Boolean> doPlace = sgPlace.add(new BoolSetting.Builder()
        .name("place")
        .description("If the CA should place crystals.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> placeDelay = sgPlace.add(new IntSetting.Builder()
        .name("place-delay")
        .description("How long in ticks to wait to place a crystal after breaking one.")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .visible(doPlace::get)
        .build()
    );

    private final Setting<Double> placeRange = sgPlace.add(new DoubleSetting.Builder()
        .name("place-range")
        .description("Range in which to place crystals.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .visible(doPlace::get)
        .build()
    );

    private final Setting<Double> placeWallsRange = sgPlace.add(new DoubleSetting.Builder()
        .name("walls-range")
        .description("Range in which to place crystals when behind blocks.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .visible(doPlace::get)
        .build()
    );

    private final Setting<Boolean> breakSurround = sgPlace.add(new BoolSetting.Builder()
        .name("break-surround")
        .description("Place crystals next to players' surround blocks if they're about to break.")
        .defaultValue(true)
        .visible(doPlace::get)
        .build()
    );

    private final Setting<Boolean> holdSurround = sgPlace.add(new BoolSetting.Builder()
        .name("hold-surround")
        .description("Instantly replace crystals that are breaking a target's surround.")
        .defaultValue(true)
        .visible(doPlace::get)
        .build()
    );

    // Support

    private final Setting<Boolean> doSupport = sgSupport.add(new BoolSetting.Builder()
        .name("support")
        .description("Place obsidian near targets, creating better damaging placements.")
        .defaultValue(true)
        .visible(doPlace::get)
        .build()
    );

    private final Setting<Integer> spPlaceDelay = sgSupport.add(new IntSetting.Builder()
        .name("place-delay")
        .description("How long in ticks to wait between replacing support blocks.")
        .defaultValue(10)
        .min(1)
        .sliderMax(40)
        .visible(() -> doPlace.get() && doSupport.get())
        .build()
    );

    private final Setting<Double> spMinDamage = sgSupport.add(new DoubleSetting.Builder()
        .name("min-damage")
        .description("Minimum damage a crystal would deal for a block to be considered.")
        .defaultValue(10)
        .range(0, 36)
        .sliderMax(36)
        .visible(() -> doPlace.get() && doSupport.get())
        .build()
    );

    private final Setting<Boolean> spNaiveSearch = sgSupport.add(new BoolSetting.Builder()
        .name("naive-search")
        .description("Stop searching at the first spot that satisfies min damage, helpful for weak PCs and laptops.")
        .defaultValue(false)
        .visible(() -> doPlace.get() && doSupport.get())
        .build()
    );

    private final Setting<Boolean> spLastResort = sgSupport.add(new BoolSetting.Builder()
        .name("last-resort")
        .description("Only use support if no other placements are found.")
        .defaultValue(true)
        .visible(doPlace::get)
        .build()
    );

    // Faceplace

    private final Setting<Boolean> doFacePlace = sgFacePlace.add(new BoolSetting.Builder()
        .name("face-place")
        .description("Place next to a target's face if they're about to pop/die.")
        .defaultValue(true)
        .visible(doPlace::get)
        .build()
    );

    private final Setting<Integer> fpHealth = sgFacePlace.add(new IntSetting.Builder()
        .name("min-health")
        .description("Face place players with this amount of health or less.")
        .defaultValue(6)
        .range(1,10)
        .visible(() -> doPlace.get() && doFacePlace.get())
        .build()
    );

    private final Setting<Integer> fpDurability = sgFacePlace.add(new IntSetting.Builder()
        .name("min-durability")
        .description("Face place players if their armor is below this % durability.")
        .defaultValue(10)
        .range(1,20)
        .sliderMax(20)
        .visible(() -> doPlace.get() && doFacePlace.get())
        .build()
    );

    private final Setting<Boolean> fpCustomDelay = sgFacePlace.add(new BoolSetting.Builder()
        .name("custom-delay")
        .description("Use a separate place delay for face placing.")
        .defaultValue(true)
        .visible(() -> doPlace.get() && doFacePlace.get())
        .build()
    );

    private final Setting<Integer> fpDelay = sgFacePlace.add(new IntSetting.Builder()
        .name("place-delay")
        .description("The place delay used while face placing targets.")
        .defaultValue(6)
        .sliderMax(20)
        .visible(() -> doPlace.get() && doFacePlace.get() && fpCustomDelay.get())
        .build()
    );

    private final Setting<Boolean> fpMissingArmor = sgFacePlace.add(new BoolSetting.Builder()
        .name("missing-armor")
        .description("Face place players if they're missing a piece of armor.")
        .defaultValue(true)
        .visible(() -> doPlace.get() && doFacePlace.get())
        .build()
    );

    private final Setting<Boolean> fpSelf = sgFacePlace.add(new BoolSetting.Builder()
        .name("face-place-self")
        .description("Whether to faceplace a target if you're in their hole.")
        .defaultValue(false)
        .visible(() -> doPlace.get() && doFacePlace.get())
        .build()
    );

    // Break

    private final Setting<Boolean> doBreak = sgBreak.add(new BoolSetting.Builder()
        .name("break")
        .description("Whether the CA should break crystals.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> instantBreak = sgBreak.add(new BoolSetting.Builder()
        .name("instant-break")
        .description("Break crystals as soon as they spawn into the world, ignoring delay.")
        .defaultValue(false)
        .visible(doBreak::get)
        .build()
    );

    private final Setting<Integer> breakDelay = sgBreak.add(new IntSetting.Builder()
        .name("break-delay")
        .description("The delay in ticks to wait to break a crystal after it's placed.")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .visible(() -> doBreak.get() && !instantBreak.get())
        .build()
    );

    private final Setting<Integer> ticksExisted = sgBreak.add(new IntSetting.Builder()
        .name("ticks-existed")
        .description("Amount of ticks a crystal needs to have lived for it to be attacked by CrystalAura.")
        .defaultValue(0)
        .min(0)
        .visible(doBreak::get)
        .build()
    );

    private final Setting<Double> breakRange = sgBreak.add(new DoubleSetting.Builder()
        .name("break-range")
        .description("Range in which to break crystals.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .visible(doBreak::get)
        .build()
    );

    private final Setting<Double> breakWallsRange = sgBreak.add(new DoubleSetting.Builder()
        .name("walls-range")
        .description("Range in which to break crystals when behind blocks.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .visible(doBreak::get)
        .build()
    );

    private final Setting<Integer> breakAttempts = sgBreak.add(new IntSetting.Builder()
        .name("break-attempts")
        .description("How many times to hit a crystal before stopping to target it.")
        .defaultValue(2)
        .sliderMin(1)
        .sliderMax(5)
        .visible(doBreak::get)
        .build()
    );

    private final Setting<Integer> attackFrequency = sgBreak.add(new IntSetting.Builder()
        .name("attack-frequency")
        .description("Maximum hits to do per second.")
        .defaultValue(25)
        .min(1)
        .sliderRange(1, 30)
        .visible(doBreak::get)
        .build()
    );

    private final Setting<Boolean> hurtTime = sgBreak.add(new BoolSetting.Builder()
        .name("hurt-time")
        .description("Avoid breaking crystals on players while they're immune from taking damage.")
        .defaultValue(false)
        .visible(doBreak::get)
        .build()
    );

    private final Setting<Boolean> onlyOwn = sgBreak.add(new BoolSetting.Builder()
        .name("only-own")
        .description("Only break crystals that you placed.")
        .defaultValue(false)
        .visible(doBreak::get)
        .build()
    );

    private final Setting<Boolean> idPredict = sgBreak.add(new BoolSetting.Builder()
        .name("ID-predict")
        .description("""
            Attempt to predict the entity ID of placed crystals and attack them
            before they spawn on the client. Helps with high ping but yields
            diminishing return.
            """)
        .defaultValue(false)
        .visible(doBreak::get)
        .build()
    );

    private final Setting<Integer> predictWindow = sgBreak.add(new IntSetting.Builder()
        .name("window")
        .description("Predict this many possible IDs after placing a crystal.")
        .defaultValue(0)
        .min(0)
        .sliderMax(20)
        .visible(() -> doBreak.get() && idPredict.get())
        .build()
    );

    // Render

    public final Setting<SwingMode> swingMode = sgRender.add(new EnumSetting.Builder<SwingMode>()
        .name("swing-mode")
        .description("How to swing when placing.")
        .defaultValue(SwingMode.Both)
        .build()
    );



    public CrystalAura2() {
        super(Categories.Combat, "crystal-aura-2", "Automatically places and attacks crystals.");
    }

    @Override
    public void onActivate() {

    }

    @Override
    public void onDeactivate() {

    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {

    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {

    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {

    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {

    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {

    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {

    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {

    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {

    }


    public enum SwitchMode {
        Inventory,
        Hotbar,
        Manual
    }

    public enum PauseMode {
        Both,
        Place,
        Break,
        None;

        public boolean matches(PauseMode process) {
            return this == process || this == Both;
        }
    }

    public enum SwingMode {
        Both,
        Packet,
        Client,
        None;

        public boolean packet() {
            return this == Packet || this == Both;
        }

        public boolean client() {
            return this == Client || this == Both;
        }
    }
}
