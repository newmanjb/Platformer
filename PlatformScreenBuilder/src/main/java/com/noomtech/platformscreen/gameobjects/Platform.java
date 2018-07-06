package com.noomtech.platformscreen.gameobjects;

import java.awt.*;


/**
 * Represents a platform on the screen
 * @author Joshua Newman
 */
public class Platform extends GameObject {


    private static final Color COLOR = Color.GREEN;


    public Platform(Rectangle collisionArea) {
        super(collisionArea);
    }


    @Override
    public void paintIt(Graphics g) {
        g.setColor(COLOR);
        Rectangle r = getCollisionArea();
        g.fillRect(r.x, r.y, r.width, r.height);
    }
}
