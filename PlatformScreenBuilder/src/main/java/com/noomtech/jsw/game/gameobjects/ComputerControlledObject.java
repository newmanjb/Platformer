package com.noomtech.jsw.game.gameobjects;


import com.noomtech.jsw.game.handlers.CollisionHandler;

/**
 * Represents an object that dictates its own movement or animation cycles, meaning that it's controlled by the game application as
 * opposed to something like the {@link com.noomtech.jsw.game.gameobjects.concrete_objects.JSW} which is controlled by the user
 */
public interface ComputerControlledObject {
    /**
     * Called by the application logic.  Move the object and return true if the move has resulted in the object hitting the player
     */
    boolean doMove(CollisionHandler collisionHandler);
    void setToStartingState();
}
