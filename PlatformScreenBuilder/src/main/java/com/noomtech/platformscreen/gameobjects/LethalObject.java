package com.noomtech.platformscreen.gameobjects;

import java.awt.*;


/**
 * Represents a non-moving lethal object
 * @author Joshua Newman
 */
public class LethalObject extends GameObject implements Lethal {

    public LethalObject(Rectangle collisionArea) {
        super(collisionArea);
    }

    @Override
    public void paintIt(Graphics g) {
        g.setColor(Color.BLUE);
        Rectangle r = getCollisionArea();
        g.fillRect(r.x, r.y, r.width, r.height);
    }
}
