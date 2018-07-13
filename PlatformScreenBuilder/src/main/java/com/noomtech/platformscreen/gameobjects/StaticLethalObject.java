package com.noomtech.platformscreen.gameobjects;

import java.awt.*;


/**
 * Represents a non-moving lethal object
 * @author Joshua Newman
 */
public class StaticLethalObject extends GameObject implements Lethal {

    public StaticLethalObject(Rectangle collisionArea) {
        super(collisionArea);
    }

    @Override
    public void paintIt(Graphics g) {
        g.setColor(Color.BLUE);
        Rectangle r = getCollisionArea();
        g.fillRect(r.x, r.y, r.width, r.height);
    }
}
