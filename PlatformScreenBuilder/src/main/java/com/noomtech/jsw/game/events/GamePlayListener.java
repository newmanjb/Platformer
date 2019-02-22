package com.noomtech.jsw.game.events;

/**
 * Used to listen for events that occur from the playing of the game
 * @see GameEventReceiver
 */
public interface GamePlayListener {

    void onLevelComplete();
    void onPlayerFalling();
    void onPlayerStoppedFalling();
    void onPlayerDied();
    void onPlayerJumping();
    void onPlayerStoppedJumping();
    void onPlayerWalking();
    void onPlayerStoppedWalking();
    void onPlayerLandedOnSolid();
}
