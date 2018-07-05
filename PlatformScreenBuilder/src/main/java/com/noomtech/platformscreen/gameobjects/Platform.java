package com.noomtech.platformscreen.gameobjects;

import com.noomtech.platformscreen.gameobjects.GameObject;

import java.awt.*;

public class Platform extends GameObject {


    private static final Color COLOR = Color.BLUE;


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
