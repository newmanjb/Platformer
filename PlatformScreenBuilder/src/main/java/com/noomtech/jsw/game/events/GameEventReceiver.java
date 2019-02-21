package com.noomtech.jsw.game.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Used as a "hub" to receive all the events that occur as a result of the game being played and to broadcast them to
 * listeners
 * @see GameEventReceiver
 */
public class GameEventReceiver {


    private List<GamePlayListener> listeners = new CopyOnWriteArrayList();

    private GameEventReceiver() {
    }
    private static final class INSTANCE_HOLDER {
        private static final GameEventReceiver INSTANCE = new GameEventReceiver();
    }

    public static GameEventReceiver getInstance() {
        return INSTANCE_HOLDER.INSTANCE;
    }

    public void addListener(GamePlayListener listener) {
        listeners.add(listener);
    }

    public void onLevelComplete() {
        fireEvent(listener -> listener.onLevelComplete());
    }
    public void onPlayerFalling() {
        fireEvent(listener -> listener.onPlayerFalling());
    }
    public void onPlayerStoppedFalling() {
        fireEvent(listener -> listener.onPlayerStoppedFalling());
    }
    public void onPlayerDied() {
        fireEvent(listener -> listener.onPlayerDied());
    }
    public void onPlayerJumping() {
        fireEvent(listener -> listener.onPlayerJumping());
    }
    public void onPlayerStoppedJumping() {
        fireEvent(listener -> listener.onPlayerStoppedJumping());
    }
    public void onPlayerWalking() {
        fireEvent(listener -> listener.onPlayerWalking());
    }
    public void onPlayerStoppedWalking() {
        fireEvent(listener -> listener.onPlayerStoppedWalking());
    }

    private void fireEvent(Firer firer) {
        for(GamePlayListener listener : listeners) {
            firer.fire(listener);
        }
    }

    private interface Firer {
        void fire(GamePlayListener listener);
    }
}
