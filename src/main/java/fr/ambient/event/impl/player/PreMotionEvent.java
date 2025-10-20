package fr.ambient.event.impl.player;

import fr.ambient.event.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PreMotionEvent extends CancellableEvent {

    public PreMotionEvent(double posX, double posY, double posZ, float yaw, float pitch, boolean onGround){
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }


    private double posX, posY, posZ;
    private float yaw, pitch;
    private boolean onGround;




}
