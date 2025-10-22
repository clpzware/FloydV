package femcum.modernfloyd.clients.script.api;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.impl.player.TargetComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.TickEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.ScriptBlockPos;
import femcum.modernfloyd.clients.script.api.wrapper.impl.ScriptEntityLiving;
import femcum.modernfloyd.clients.script.api.wrapper.impl.ScriptWorld;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;

public class WorldAPI extends ScriptWorld {

    public WorldAPI() {
        super(MC.theWorld);

        Floyd.INSTANCE.getEventBus().register(this);
    }

    @EventLink
    public final Listener<TickEvent> onTick = event -> {
        if (this.wrapped == null) {
            this.wrapped = MC.theWorld;
        }
    };

    public ScriptEntityLiving[] getEntities() {
        final Object[] entityLivingBases = MC.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityLivingBase).toArray();
        final ScriptEntityLiving[] scriptEntities = new ScriptEntityLiving[entityLivingBases.length];

        for (int index = 0; index < entityLivingBases.length; index++) {
            scriptEntities[index] = new ScriptEntityLiving((EntityLivingBase) entityLivingBases[index]);
        }

        return scriptEntities;
    }

    public ScriptEntityLiving getTargetEntity(int range) {
        EntityLivingBase entityLivingBase = TargetComponent.getTarget(range);
        return entityLivingBase != null ? new ScriptEntityLiving(entityLivingBase) : null;
    }

    public void removeEntity(int id) {
        MC.theWorld.removeEntityFromWorld(id);
    }

    public void removeEntity(ScriptEntityLiving entity) {
        removeEntity(entity.getEntityId());
    }

    public ScriptBlockPos newBlockPos(int x, int y, int z) {
        return new ScriptBlockPos(new BlockPos(x, y, z));
    }

    public String getBlockName(ScriptBlockPos blockPos) {
        return blockPos.getBlock().getName();
    }

}
