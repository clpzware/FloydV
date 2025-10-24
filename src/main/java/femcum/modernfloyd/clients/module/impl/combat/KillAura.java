package femcum.modernfloyd.clients.module.impl.combat;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.impl.player.*;
import femcum.modernfloyd.clients.component.impl.player.rotationcomponent.MovementFix;
import femcum.modernfloyd.clients.component.impl.render.ESPComponent;
import femcum.modernfloyd.clients.component.impl.render.espcomponent.api.ESPColor;
import femcum.modernfloyd.clients.component.impl.render.espcomponent.impl.AboveBox;
import femcum.modernfloyd.clients.component.impl.render.espcomponent.impl.FullBox;
import femcum.modernfloyd.clients.component.impl.render.espcomponent.impl.SigmaRing;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.RightClickEvent;
import femcum.modernfloyd.clients.event.impl.motion.PostMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.motion.SlowDownEvent;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.event.impl.render.MouseOverEvent;
import femcum.modernfloyd.clients.event.impl.render.RenderItemEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.util.EvictingList;
import femcum.modernfloyd.clients.util.RayCastUtil;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.util.rotation.RotationUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.util.vector.Vector3d;
import femcum.modernfloyd.clients.value.impl.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import rip.vantage.commons.util.time.StopWatch;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(aliases = {"module.combat.killaura.name", "Aura", "Force Field"}, description = "module.combat.killaura.description", category = Category.COMBAT)
public final class KillAura extends Module {
    private final ModeValue mode = new ModeValue("Attack Mode", this)
            .add(new SubMode("Single"))
            .add(new SubMode("Switch"))
            .add(new SubMode("Multiple"))
            .setDefault("Single");

    private final BoundsNumberValue switchDelay = new BoundsNumberValue("Switch Delay", this, 0, 0, 0, 10, 1, () -> !mode.getValue().getName().equals("Switch"));
    public final ModeValue autoBlock = new ModeValue("Auto Block", this)
            .add(new SubMode("None"))
            .add(new SubMode("Fake"))
            .add(new SubMode("Vanilla"))
            .add(new SubMode("NCP"))
            .add(new SubMode("Legit"))
            .add(new SubMode("Grim"))
            .add(new SubMode("Intave"))
            .add(new SubMode("Old Intave"))
            .add(new SubMode("Imperfect Vanilla"))
            .add(new SubMode("Vanilla ReBlock"))
            .add(new SubMode("Watchdog 1.17"))
            .add(new SubMode("New NCP"))
            .add(new SubMode("Universal"))
            .add(new SubMode("Watchdog"))
            // .add(new SubMode("Watchdog2"))
            .setDefault("None");

    private final BooleanValue rightClickOnly = new BooleanValue("Right Click Only", this, false, () -> autoBlock.getValue().getName().equals("None") || autoBlock.getValue().getName().equals("Fake"));
    private final BooleanValue preventServerSideBlocking = new BooleanValue("Prevent Serverside Blocking", this, false, () -> !(autoBlock.getValue().getName().equals("None") || autoBlock.getValue().getName().equals("Fake")));
    private final ModeValue sorting = new ModeValue("Sorting", this)
            .add(new SubMode("Distance"))
            .add(new SubMode("Health"))
            .add(new SubMode("Hurt Time"))
            .setDefault("Distance");
    private final ModeValue clickMode = new ModeValue("Click Delay Mode", this)
            .add(new SubMode("Normal"))
            .add(new SubMode("Hit Select"))
            .add(new SubMode("1.9+"))
            .add(new SubMode("1.9+ With 1.8 Animations"))
            .setDefault("Normal");
    private final NumberValue range = new NumberValue("Range", this, 3, 3, 6, 0.1);
    private final BoundsNumberValue cps = new BoundsNumberValue("CPS", this, 10, 15, 1, 20, 1);
    private final BoundsNumberValue rotationSpeed = new BoundsNumberValue("Rotation speed", this, 5, 10, 0, 10, 1);
    private final ListValue<MovementFix> movementCorrection = new ListValue<>("Movement correction", this);
    private final BooleanValue keepSprint = new BooleanValue("Keep sprint", this, false);

    private final ModeValue espMode = new ModeValue("Target ESP Mode", this)
            .add(new SubMode("Ring"))
            .add(new SubMode("Box"))
            .add(new SubMode("None"))
            .setDefault("Ring");

    public final ModeValue boxMode = new ModeValue("Box Mode", this, () -> !(espMode.getValue()).getName().equals("Box"))
            .add(new SubMode("Above"))
            .add(new SubMode("Full"))
            .setDefault("Ring");

    private final BooleanValue rayCast = new BooleanValue("Ray cast", this, false);
    private final BooleanValue throughWalls = new BooleanValue("Through Walls", this, false, () -> !rayCast.getValue());

    private final BooleanValue advanced = new BooleanValue("Advanced", this, false);
    private final ModeValue rotationMode = new ModeValue("Rotation Mode", this, () -> !advanced.getValue())
            .add(new SubMode("Legit/Normal"))
            .add(new SubMode("Snap"))
            .add(new SubMode("NCP"))
            .add(new SubMode("Autistic AntiCheat"))
            .setDefault("Legit/Normal");
    private final BooleanValue attackWhilstScaffolding = new BooleanValue("Attack whilst Scaffolding", this, false, () -> !advanced.getValue());
    private final BooleanValue noSwing = new BooleanValue("No swing", this, false, () -> !advanced.getValue());
    private final BooleanValue autoDisable = new BooleanValue("Auto disable", this, false, () -> !advanced.getValue());
    private final BooleanValue showTargets = new BooleanValue("Targets", this, false);
    public final BooleanValue player = new BooleanValue("Player", this, true, () -> !showTargets.getValue());
    public final BooleanValue invisibles = new BooleanValue("Invisibles", this, false, () -> !showTargets.getValue());
    public final BooleanValue animals = new BooleanValue("Animals", this, false, () -> !showTargets.getValue());
    public final BooleanValue mobs = new BooleanValue("Mobs", this, false, () -> !showTargets.getValue());
    public final BooleanValue teams = new BooleanValue("Player Teammates", this, true, () -> !showTargets.getValue());

    private final Queue<Packet<?>> packetQueue = new ConcurrentLinkedQueue<>();

    private final StopWatch attackStopWatch = new StopWatch();
    private final StopWatch clickStopWatch = new StopWatch();

    private boolean blocking, swing, allowAttack;
    private long nextSwing;

    private List<EntityLivingBase> targets;
    public EntityLivingBase target;

    private int attack, expandRange, blockTicks, switchTicks;
    public int hitTicks;
    boolean shouldBlink;
    public boolean swingg;

    // Pointless to remember past 9 because hurt resistance is 10 ticks
    private final EvictingList<EntityLivingBase> pastTargets = new EvictingList<>(9);

    public KillAura() {
        for (MovementFix movementFix : MovementFix.values()) {
            movementCorrection.add(movementFix);
        }

        movementCorrection.setDefault(MovementFix.OFF);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        this.packetQueue.forEach(PacketUtil::sendNoEvent);
        this.packetQueue.clear();

        this.hitTicks++;

        // Set blocking to false when switching items
        if (getComponent(Slot.class).getItemStack() == null || !(getComponent(Slot.class).getItemStack().getItem() instanceof ItemSword)) {
            blocking = false;
        }

        if (GUIDetectionComponent.inGUI()) {
            return;
        }

        if (target == null || mc.thePlayer.isDead || getModule(Scaffold.class).isEnabled()) {
            if (!BadPacketsComponent.bad()) {
                this.unblock(false);
                target = null;
            }
        }
        Color color = /*getTheme().getFirstColor()*/ Color.WHITE;

        switch (espMode.getValue().getName()) {
            case "Ring":
                ESPComponent.add(new SigmaRing(new ESPColor(color, color, color)));
                break;
            case "Box":
                switch (boxMode.getValue().getName()) {
                    case "Full":
                        ESPComponent.add(new FullBox(new ESPColor(color, color, color)));
                        break;
                    case "Above":
                        ESPComponent.add(new AboveBox(new ESPColor(color, color, color)));
                        break;
                }
                break;
        }

        if (autoBlock.getValue().getName().equals("Watchdog")) {
            if (target != null
                    && mc.thePlayer.getHeldItem() != null
                    && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                blockTicks++;
                if (blockTicks == 1) {
                    blockTicks = 0;
                    BlinkComponent.dispatch();
                }

                if (blockTicks == 0) {
                    MovingObjectPosition movingObjectPosition = mc.objectMouseOver;
                    mc.objectMouseOver = RayCastUtil.rayCast(RotationComponent.lastRotations, 3);
                    mc.rightClickMouse();
                    mc.objectMouseOver = movingObjectPosition;
                    BlinkComponent.blinking = true;

                }
            } else {
                BlinkComponent.dispatch();
            }

        }
    };

    @Override
    public void onEnable() {
        this.attack = 0;
        this.blockTicks = 0;
        this.nextSwing = 0;
//        Client.INSTANCE.setClickGUI(new RiseClickGUI());
    }

    @Override
    public void onDisable() {
        this.packetQueue.forEach(PacketUtil::sendNoEvent);
        this.packetQueue.clear();
        BlinkComponent.dispatch();
        target = null;

        this.unblock(false);
        mc.gameSettings.keyBindUseItem.setPressed(false);
        BlinkComponent.blinking = false;
    }

    @EventLink
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        if (this.autoDisable.getValue()) {
            this.toggle();
        }
    };

    public void getTargets() {
        double range = this.range.getValue().doubleValue();

        // Create a MUTABLE ArrayList from the targets to prevent UnsupportedOperationException
        targets = new ArrayList<>(TargetComponent.getTargets(range));

        if (mode.getValue().getName().equals("Switch")) {
            targets.removeAll(pastTargets);
        }

        if (targets.isEmpty()) {
            pastTargets.clear();
            // Again, create a MUTABLE ArrayList
            targets = new ArrayList<>(TargetComponent.getTargets(range + expandRange));
        }

        switch (sorting.getValue().getName()) {
            case "Health":
                targets.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
                sortByTargets();
                break;

            case "Hurt Time":
                targets.sort(Comparator.comparingDouble(entity -> entity.hurtTime));
                sortByTargets();
                break;
        }
    }

    private void sortByTargets() {
        targets.sort((o1, o2) -> {
            boolean isTarget1 = UserFriendAndTargetComponent.isTarget(o1.getCommandSenderName());
            boolean isTarget2 = UserFriendAndTargetComponent.isTarget(o2.getCommandSenderName());
            if (isTarget1 && !isTarget2) {
                return -1;
            } else if (!isTarget1 && isTarget2) {
                return 1;
            }
            return 0;
        });
    }

    @EventLink(value = Priorities.HIGH)
    public final Listener<PreUpdateEvent> onHighPreUpdate = event -> {
        if (RotationComponent.isSmoothed()) {
            return;
        }

        mc.entityRenderer.getMouseOver(1);

        this.allowAttack = !BadPacketsComponent.bad(false, false, false, true, true);

        if (mc.thePlayer.getHealth() <= 0.0 && this.autoDisable.getValue()) {
            this.toggle();
        }

        if (getModule(Scaffold.class).isEnabled() && !attackWhilstScaffolding.getValue()) {
            return;
        }

        this.attack = Math.max(Math.min(this.attack, this.attack - 2), 0);

        /*
         * Heuristic fix
         */
        if (mc.thePlayer.ticksExisted % 20 == 0) {
            expandRange = (int) (3 + Math.random() * 0.5);
        }

        if (GUIDetectionComponent.inGUI()) {
            return;
        }

        /*
         * Getting targets and selecting the nearest one
         */
        this.getTargets();

        if (targets.isEmpty()) {
            target = null;
            this.cantPreBlock();
            return;
        }

        target = targets.get(0);

        if (target == null || mc.thePlayer.isDead) {
            this.cantPreBlock();
            return;
        }

        if (this.canBlock()) {
            this.preBlock();
        } else {
            this.cantPreBlock();
        }

        /*
         * Calculating rotations to target
         */
        this.rotations();
    };

    // We attack on an event after all others, because other modules may have overridden the rotations
    // this way we won't attack if a module has overriden the killaura's rotations
    @EventLink()
    public final Listener<PreUpdateEvent> onMediumPriorityPreUpdate = event -> {
        if (target == null || mc.thePlayer.isDead) {
            return;
        }

        /*
         * Doing the attack
         */
        this.doAttack(targets);

        /*
         * Blocking
         */
        if (this.canBlock()) {
            this.postAttackBlock();
        }
    };

    public void cantPreBlock() {
        switch (autoBlock.getValue().getName()) {
            case "Universal":
                this.blockTicks = -1;
                break;
        }
    }

    public void rotations() {
        final float rotationSpeed = this.rotationSpeed.getRandomBetween().floatValue();

        switch (rotationMode.getValue().getName()) {
            case "Legit/Normal":
                Vector2f targetRotations = RotationUtil.calculate(target, true, range.getValue().doubleValue());

                if (rotationSpeed != 0) RotationComponent.setRotations(targetRotations, rotationSpeed,
                        movementCorrection.getValue() == MovementFix.OFF ? MovementFix.OFF : movementCorrection.getValue(),
                        rotations -> {
                            MovingObjectPosition movingObjectPosition = RayCastUtil.rayCast(rotations, range.getValue().floatValue(), -0.1f);

                            return movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY;
                        });
                break;

            case "NCP":
                int predict = (int) (Math.random() * 1);
                Vector3d position = new Vector3d(target.posX, target.posY, target.posZ);
                Vector3d playerPosition = new Vector3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                Vector3d predictedPosition = MoveUtil.predictMovement(target, new Vector2f(0, 1), predict, mc.thePlayer.isSprinting());
                target.setPosition(predictedPosition.x, predictedPosition.y, predictedPosition.z);
                mc.thePlayer.setPosition(mc.thePlayer.posX + mc.thePlayer.motionX * predict, mc.thePlayer.posY + (mc.thePlayer.motionY + 0.17) * predict, mc.thePlayer.posZ + mc.thePlayer.motionZ * predict);
                final Vector2f axis = RotationUtil.applySensitivityPatch(RotationUtil.calculate(target, true, range.getValue().doubleValue()));
                target.setPosition(position.x, position.y, position.z);
                mc.thePlayer.setPosition(playerPosition.x, playerPosition.y, playerPosition.z);

                if (rotationSpeed != 0) {
                    if (Math.random() > 0.1) {
                        RotationComponent.setRotations(axis, rotationSpeed,
                                movementCorrection.getValue() == MovementFix.OFF ? MovementFix.OFF : movementCorrection.getValue());
                    } else {
                        RotationComponent.setRotations(RotationComponent.targetRotations.add((float) ((Math.random() - 0.5) * 10), (float) ((Math.random() - 0.5) * 3)), rotationSpeed,
                                movementCorrection.getValue() == MovementFix.OFF ? MovementFix.OFF : movementCorrection.getValue());

                    }
                }
                //ChatUtil.display("Prediction Position: " + position.x + position.y + position.z);
                break;

            case "Snap":
                final Vector2f rotations = RotationUtil.calculate(target, true, range.getValue().doubleValue());

                if (rotationSpeed != 0 && lastSafeUnBlockTick()) {
                    RotationComponent.setRotations(rotations, rotationSpeed,
                            movementCorrection.getValue() == MovementFix.OFF ? MovementFix.OFF : movementCorrection.getValue());
                } else {
                    RotationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), rotationSpeed, movementCorrection.getValue() == MovementFix.OFF ? MovementFix.OFF : movementCorrection.getValue());
                }
                break;

            case "Autistic AntiCheat":
                RotationComponent.setRotations(new Vector2f(RotationComponent.rotations.x + rotationSpeed * 10, 0), (rotationSpeed * 10) / 18,
                        movementCorrection.getValue() == MovementFix.OFF ? MovementFix.OFF : movementCorrection.getValue());
                break;
        }

    }

    @EventLink
    public final Listener<MouseOverEvent> onMouseOver = event ->
            event.setRange(event.getRange() + range.getValue().doubleValue() - 3);

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        if (target != null && this.canBlock()) {
            this.postBlock();
        }
    };

    public Tuple<Boolean, Double> getDelay() {
        double delay = -1;
        boolean flag = false;

        switch (clickMode.getValue().getName()) {
            case "1.9+ With 1.8 Animations":
            case "1.9+": {
                if (clickMode.getValue().getName().equals("1.9+ With 1.8 Animations") && Math.random() > 0.2) {
                    RenderUtil.renderAttack(target);
                }

                double speed = 4;

                if (mc.thePlayer.getHeldItem() != null) {
                    final Item item = mc.thePlayer.getHeldItem().getItem();

                    if (item instanceof ItemSword) {
                        speed = 1.6;
                    } else if (item instanceof ItemSpade) {
                        speed = 1;
                    } else if (item instanceof ItemPickaxe) {
                        speed = 1.2;
                    } else if (item instanceof ItemAxe) {
                        switch (((ItemAxe) item).getToolMaterial()) {
                            case WOOD:
                            case STONE:
                                speed = 0.8;
                                break;

                            case IRON:
                                speed = 0.9;
                                break;

                            default:
                                speed = 1;
                                break;
                        }
                    } else if (item instanceof ItemHoe) {
                        switch (((ItemHoe) item).getToolMaterial()) {
                            case WOOD:
                            case GOLD:
                                speed = 1;
                                break;

                            case STONE:
                                speed = 2;
                                break;

                            case IRON:
                                speed = 3;
                                break;
                        }
                    }
                }

                delay = 1 / speed * 20 - 1;
                break;
            }
        }

        delay = clickDelayBlock(delay);

        return new Tuple<>(flag, delay);
    }

    private void doAttack(final List<EntityLivingBase> targets) {
        Tuple<Boolean, Double> tuple = getDelay();
        final double delay = tuple.getSecond();
        final boolean flag = tuple.getFirst();

        if (attackStopWatch.finished(this.nextSwing) && target != null && (clickStopWatch.finished((long) (delay * 50)) || flag)) {
            final long clicks = (long) (this.cps.getRandomBetween().longValue() * 1.5);
            this.nextSwing = 1000 / clicks;

            if (Math.sin(nextSwing) + 1 > Math.random() || attackStopWatch.finished(this.nextSwing + 500) || Math.random() > 0.5) {
                if (this.allowAttack) {
                    /*
                     * Attacking target
                     */
                    final double range = this.range.getValue().doubleValue();
                    final Vec3 rotationVector = mc.thePlayer.getVectorForRotation(RotationComponent.rotations.getY(), RotationComponent.rotations.getX());
                    MovingObjectPosition movingObjectPosition = RayCastUtil.rayCast(RotationComponent.rotations, range);

                    if (throughWalls.getValue()) {
                        Vec3 eyes = mc.thePlayer.getPositionEyes(1);
                        movingObjectPosition = target.getEntityBoundingBox().expand(0.1, 0.1, 0.1).calculateIntercept(eyes,
                                eyes.addVector(rotationVector.xCoord * range, rotationVector.yCoord * range, rotationVector.zCoord * range));

                        if (movingObjectPosition != null) {
                            movingObjectPosition.typeOfHit = MovingObjectPosition.MovingObjectType.ENTITY;
                            movingObjectPosition.entityHit = target;
                        }
                    }

                    switch (this.mode.getValue().getName()) {
                        case "Switch":
                        case "Single": {
                            if ((mc.thePlayer.getDistanceToEntity(target) <= range && !rayCast.getValue()) ||
                                    (rayCast.getValue() && movingObjectPosition != null && movingObjectPosition.entityHit == target)) {
                                this.attack(target);
                            } else if (movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                                if (!(movingObjectPosition.entityHit instanceof EntityFireball))
                                    this.attack((EntityLivingBase) movingObjectPosition.entityHit);
                            } else {
                                switch (clickMode.getValue().getName()) {
                                    case "Normal":
                                    case "Hit Select":
                                        if (mc.playerController.curBlockDamageMP != 0) return;
//                                        PacketUtil.send(new C0APacketAnimation());
                                        break;
                                }
                            }
                            break;
                        }

                        case "Multiple": {
                            targets.removeIf(target -> mc.thePlayer.getDistanceToEntity(target) > range);

                            if (!targets.isEmpty()) {
                                targets.forEach(this::attack);
                            }
                            break;
                        }
                    }

                    this.attackStopWatch.reset();
                }
            }
        }
    }

    private boolean lastSafeUnBlockTick() {
        Tuple<Boolean, Double> tuple = getDelay();
        final double delay = tuple.getSecond();
        final boolean flag = tuple.getFirst();

        return attackStopWatch.finished(this.nextSwing - 50) && target != null && (clickStopWatch.finished((long) (delay * 50) - 50) || flag) && !(clickMode.getValue().getName().equals("Hit Select") && target.hurtTime > (PingComponent.getPing() / 50 - 1) && mc.thePlayer.ticksSinceVelocity > 11) && allowAttack;
    }

    private void attack(final EntityLivingBase target) {
        final AttackEvent event = new AttackEvent(target);
        Floyd.INSTANCE.getEventBus().handle(event);

        if (this.canBlock()) {
            this.attackBlock();
        }

        mc.thePlayer.swingItem();
        if (this.keepSprint.getValue()) {
            mc.playerController.syncCurrentPlayItem();

            PacketUtil.send(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

            if (mc.thePlayer.fallDistance > 0 && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null) {
                mc.thePlayer.onCriticalHit(target);
            }
        } else {
            mc.playerController.attackEntity(mc.thePlayer, target);
        }

        this.clickStopWatch.reset();
        this.hitTicks = 0;
    }

    private void block(final boolean check, final boolean interact) {
        if (!blocking || !check) {
            MovingObjectPosition movingObjectPosition = RayCastUtil.rayCast(RotationComponent.lastRotations, 3);

            if (interact && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                this.interact(movingObjectPosition);
            }

            PacketUtil.send(new C08PacketPlayerBlockPlacement(getComponent(Slot.class).getItemStack()));

            blocking = true;
        }
    }

    public void interact(MovingObjectPosition mouse) {
        if (!mc.playerController.isPlayerRightClickingOnEntity(mc.thePlayer, mouse.entityHit, mouse)) {
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, mouse.entityHit);
        }
    }

    private void unblock(final boolean swingCheck) {
        if (blocking && (!swingCheck || !swing)) {
            PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            blocking = false;
        }

        if (mc.gameSettings.keyBindUseItem.isKeyDown() && getComponent(Slot.class).getItemStack() != null && getComponent(Slot.class).getItemStack().getItem() instanceof ItemSword) {
            //mc.gameSettings.keyBindUseItem.setPressed(false);
        }
    }

    @EventLink(value = Priorities.HIGH)
    public final Listener<RenderItemEvent> onRenderItem = event -> {
        if (target != null && !autoBlock.getValue().getName().equals("None") && this.canBlock()) {
            event.setEnumAction(EnumAction.BLOCK);
            event.setUseItem(true);
        }
    };

    int ticks = 0;
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (event.isCancelled()) {
            return;
        }

        final Packet<?> packet = event.getPacket();

        if (packet instanceof C0APacketAnimation) {
            swing = true;
        } else if (packet instanceof C03PacketPlayer) {
            swing = false;
        }

        this.packetBlock(event);
    };

    public double clickDelayBlock(double delay) {
        switch (autoBlock.getValue().getName()) {
            case "Universal":
                delay