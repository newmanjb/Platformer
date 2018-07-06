package com.noomtech.platformscreen.gameobjects;

import java.awt.*;


/**
 * Represents a moving, lethal object on the screen
 * @author Joshua Newman
 */
public class Nasty extends GameObject {


    public Nasty(Rectangle collisionArea) {
        super(collisionArea);
    }

    @Override
    public void paintIt(Graphics graphics) {
        graphics.setColor(Color.RED);
        Rectangle r = getCollisionArea();
        graphics.drawRect(r.x, r.y, r.width, r.height);
    }
}
