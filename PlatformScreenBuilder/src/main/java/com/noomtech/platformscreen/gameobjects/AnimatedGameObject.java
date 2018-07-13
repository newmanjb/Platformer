package com.noomtech.platformscreen.gameobjects;

import com.noomtech.platformscreen.movement.PlayerMovementType;

import java.awt.*;


/**
 * Superclass for anything animated
 * @author Joshua Newman
 */
public abstract class AnimatedGameObject extends GameObject {


    protected int previousXLocation;
    protected int previousYLocation;


    public AnimatedGameObject(Rectangle collisionArea) {
        super(collisionArea);
        this.previousXLocation = startingLocation.x;
        this.previousYLocation = startingLocation.y;
    }

    /**
     * Callback for when the object has moved
     */
    public void onMove(PlayerMovementType movementType) {
        reactToMove(movementType);
        previousXLocation = getX();
        previousYLocation = getY();
    }

    public void setToStartingState() {
        super.setToStartingState();
        previousXLocation = getX();
        previousYLocation = getY();
    }

    /**
     * Subclasses should override in order to process a movement
     */
    protected abstract void reactToMove(PlayerMovementType movementType);
}