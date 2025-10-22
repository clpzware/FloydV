package femcum.modernfloyd.clients.component.impl.player;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;
import femcum.modernfloyd.clients.module.impl.combat.KillAura;
import femcum.modernfloyd.clients.util.Accessor;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import rip.vantage.commons.util.time.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetComponent extends Component implements Accessor {
    private static final Map<Class<?>, List<EntityLivingBase>> targetMap = new HashMap<>();
    private static final Map<Class<?>, Integer> entityAmountMap = new HashMap<>();
    private static final Map<Class<?>, StopWatch> timerMap = new HashMap<>();
    private static final Map<Integer, Class<?>> queuedMap = new HashMap<>();
    private static int id;
    private static final KillAura killAura = Floyd.INSTANCE.getModuleManager().get(KillAura.class);

    public static void forceUpdate() {
        targetMap.clear();
        entityAmountMap.clear();
        timerMap.clear();
        queuedMap.clear();
    }

    @EventLink
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        forceUpdate();
        id = 0;
    };

    public static EntityLivingBase getTarget(double range) {
        return getTargets(range).stream().findFirst().orElse(null);
    }

    public static List<EntityLivingBase> getTargets(double range) {
        return getTargets(killAura.getClass(), killAura.player.getValue(), killAura.invisibles.getValue(),
                killAura.animals.getValue(), killAura.mobs.getValue(), killAura.teams.getValue())
                .stream()
                .filter(entity -> mc.thePlayer.getDistanceToEntity(entity) <= range)
                .toList();
    }

    public static List<EntityLivingBase> getTargets(Class<?> module, double range, boolean players, boolean invisible,
                                                    boolean animals, boolean mobs, boolean teams) {
        return getTargets(module, players, invisible, animals, mobs, teams)
                .stream()
                .filter(entity -> mc.thePlayer.getDistanceToEntity(entity) <= range)
                .toList();
    }

    public static List<EntityLivingBase> getTargets() {
        return getTargets(killAura.getClass(), killAura.player.getValue(), killAura.invisibles.getValue(),
                killAura.animals.getValue(), killAura.mobs.getValue(), killAura.teams.getValue());
    }

    public static List<EntityLivingBase> getTargets(Class<?> module, boolean players, boolean invisible,
                                                    boolean animals, boolean mobs, boolean teams) {
        // Return cached targets if they exist, are recent (within 5 seconds), and the world hasn't changed significantly
        if (queuedMap.containsValue(module) && !timerMap.get(module).finished(5000L)) {
            return targetMap.getOrDefault(module, new ArrayList<>())
                    .stream()
                    .filter(mc.theWorld.loadedEntityList::contains)
                    .toList();
        }

        // Update targets if cache is missing, outdated, or world entity count has changed
        if (!targetMap.containsKey(module) ||
                timerMap.getOrDefault(module, new StopWatch()).finished(5000L) ||
                (entityAmountMap.containsKey(module) &&
                        entityAmountMap.get(module) != mc.theWorld.loadedEntityList.size() &&
                        timerMap.getOrDefault(module, new StopWatch()).finished(1000L))) {
            List<EntityLivingBase> startingTargets = mc.theWorld.loadedEntityList
                    .stream()
                    .filter(entity -> entity instanceof EntityLivingBase &&
                            entity != mc.getRenderViewEntity() &&
                            !UserFriendAndTargetComponent.isFriend(entity.getCommandSenderName()))
                    .map(EntityLivingBase.class::cast)
                    .filter(entity -> !Floyd.INSTANCE.getBotManager().contains(entity))
                    .filter(entity -> invisible || !entity.isInvisible())
                    .filter(entity -> {
                        if (entity instanceof EntityPlayer) {
                            return players && (!PlayerUtil.sameTeam(entity) || teams);
                        }
                        if (entity instanceof EntityAnimal) {
                            return animals;
                        }
                        if (entity instanceof EntityMob) {
                            return mobs;
                        }
                        return false;
                    })
                    .toList();

            // Update caches
            targetMap.put(module, startingTargets);
            entityAmountMap.put(module, mc.theWorld.loadedEntityList.size());
            timerMap.put(module, new StopWatch());
            queuedMap.put(id++, module);
        }

        return targetMap.getOrDefault(module, new ArrayList<>())
                .stream()
                .filter(mc.theWorld.loadedEntityList::contains)
                .toList();
    }
}