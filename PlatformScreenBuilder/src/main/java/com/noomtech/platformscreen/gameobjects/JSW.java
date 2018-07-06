package com.noomtech.platformscreen.gameobjects;

import java.awt.*;

/**
 * The player
 * @author Joshua Newman
 */
public class JSW extends GameObject {


    public JSW(Rectangle collisionArea) {
        super(collisionArea);
    }

    @Override
    public void paintIt(Graphics graphics) {
        graphics.setColor(Color.BLUE);
        Rectangle r = getCollisionArea();
        graphics.drawRect(r.x, r.y, r.width, r.height);
    }

    public void setLocation(int x, int y) {
        Rectangle r = getCollisionArea();
        r.x = x;
        r.y = y;
    }
}
