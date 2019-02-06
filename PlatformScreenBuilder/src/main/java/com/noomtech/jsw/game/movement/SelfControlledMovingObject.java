package com.noomtech.jsw.game.movement;


/**
 * Represents an object that dictates its own movement, meaning that it's controlled by the game application as
 * opposed to something like the {@link com.noomtech.jsw.game.gameobjects.objects.JSW} which is controlled by the user
 */
public interface SelfControlledMovingObject {
    /**
     * Called by the application logic.  Move the object and return true if the move has hit the player
     */
    boolean doMove(CollisionHandler collisionHandler);
}
