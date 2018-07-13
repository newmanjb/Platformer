package com.noomtech.platformscreen.gameobjects;

import com.noomtech.platformscreen.movement.PlayerMovementType;

import java.awt.*;


/**
 * Superclass for anything animated
 * @author Joshua Newman
 */
public abstract class AnimatedGameObject extends GameObject {


    public AnimatedGameObject(Rectangle collisionArea) {
        super(collisionArea);
    }

    /**
     * Callback for when the object has moved
     */
    public void onMove(PlayerMovementType movementType) {
        reactToMove(movementType);
    }

    /**
     * Subclasses should override in order to process a movement
     */
    protected abstract void reactToMove(PlayerMovementType movementType);
}
