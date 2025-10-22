package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.component.impl.player.BadPacketsComponent;
import femcum.modernfloyd.clients.component.impl.player.RotationComponent;
import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.component.impl.player.rotationcomponent.MovementFix;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.math.MathUtil;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.value.impl.BoundsNumberValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import rip.vantage.commons.util.time.StopWatch;

@ModuleInfo(aliases = {"module.player.autopot.name"}, description = "module.player.autopot.description", category = Category.PLAYER)
public class AutoPot extends Module {

    private final NumberValue health = new NumberValue("Health", this, 15, 1, 20, 1);
    private final BoundsNumberValue delay = new BoundsNumberValue("Delay", this, 500, 1000, 50, 5000, 50);
    private final BoundsNumberValue rotationSpeed = new BoundsNumberValue("Rotation Speed", this, 5, 10, 0, 10, 1);
    private final StopWatch stopWatch = new StopWatch();

    private int attackTicks;
    private long nextThrow;

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        this.attackTicks++;

        if (mc.currentScreen != null) {
            this.attackTicks = 0;
        }

        if (mc.thePlayer.onGroundTicks <= 1 || !stopWatch.finished(nextThrow) || attackTicks < 10 || this.getModule(Scaffold.class).isEnabled()) {
            return;
        }

        for (int i = 0; i < 9; i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null) {
                continue;
            }

            final Item item = stack.getItem();

            if (item instanceof ItemPotion) {
                final ItemPotion potion = (ItemPotion) item;
                final PotionEffect effect = potion.getEffects(stack).get(0);

                if (!ItemPotion.isSplash(stack.getMetadata()) ||
                        !PlayerUtil.goodPotion(effect.getPotionID()) ||
                        (effect.getPotionID() == Potion.regeneration.id ||
                                effect.getPotionID() == Potion.heal.id) &&
                                mc.thePlayer.getHealth() > this.health.getValue().floatValue()) {
                    continue;
                }

                if (mc.thePlayer.isPotionActive(effect.potionID) &&
                        mc.thePlayer.getActivePotionEffect(effect.potionID).duration != 0) {
                    continue;
                }

                final double minRotationSpeed = this.rotationSpeed.getValue().doubleValue();
                final double maxRotationSpeed = this.rotationSpeed.getSecondValue().doubleValue();
                final float rotationSpeed = (float) MathUtil.getRandom(minRotationSpeed, maxRotationSpeed);
                RotationComponent.setRotations(new Vector2f((float) (mc.thePlayer.rotationYaw + (Math.random() - 0.5) * 3), (float) (87 + Math.random() * 3)), rotationSpeed, MovementFix.NORMAL);

                getComponent(Slot.class).setSlot(i);

                if (RotationComponent.rotations.y > 85 && !BadPacketsComponent.bad(false, true, false, true, false)) {
                    mc.playerController.syncCurrentPlayItem();
                    PacketUtil.send(new C08PacketPlayerBlockPlacement(getComponent(Slot.class).getItemStack()));

                    this.nextThrow = Math.round(MathUtil.getRandom(delay.getValue().longValue(), delay.getSecondValue().longValue()));
                    stopWatch.reset();
                    break;
                }
            }
        }
    };

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> this.attackTicks = 0;
}