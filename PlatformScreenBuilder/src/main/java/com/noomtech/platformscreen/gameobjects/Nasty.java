package com.noomtech.platformscreen.gameobjects;

import java.awt.*;


/**
 * Represents a moving, lethal object on the screen
 * @author Joshua Newman
 */
public class Nasty extends GameObject implements Lethal {


    //The direction along the y-axis that this nasty is moving.  Start the nasty off moving down
    private int moveYDirection = 1;


    public Nasty(Rectangle collisionArea) {
        super(collisionArea);
    }

    @Override
    public void paintIt(Graphics graphics) {
        graphics.setColor(Color.RED);
        Rectangle r = getCollisionArea();
        graphics.fillRect(r.x, r.y, r.width, r.height);
    }


    public void setLocation(int x, int y) {
        Rectangle r = getCollisionArea();
        r.x = x;
        r.y = y;
    }

    /**
     * @return Which direction this nasty is moving in - 1 for down, -1 for up.
     */
    public int getMoveYDirection() {
        return moveYDirection;
    }

    /**
     * Gives this nasty the opportunity to update itself in reaction to a collision e.g. it might change direction
     */
    public void onCollision() {
        //Go from moving down to up or from moving up to down
        moveYDirection = -moveYDirection;
    }

    public void setToStartingState() {
        super.setToStartingState();
        moveYDirection = 1;
    }
}
