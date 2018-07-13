package com.noomtech.platformscreen.gameobjects;

import java.awt.*;


/**
 * An object that the player has to collect in order to finish the game
 * @author Joshua Newman
 */
public class FinishingObject extends GameObject {


    public FinishingObject(Rectangle collisionArea) {
        super(collisionArea);
    }


    public void paintIt(Graphics g) {
        g.setColor(Color.YELLOW);
        Rectangle r = getCollisionArea();
        g.fillRect(r.x, r.y, r.width, r.height);
    }
}
