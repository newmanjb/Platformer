package com.noomtech.jsw.game.handlers;

import com.noomtech.jsw.common.utils.GlobalConfig;
import com.noomtech.jsw.game.GamePlayDisplay;
import com.noomtech.jsw.game.gameobjects.ComputerControlledObject;
import com.noomtech.jsw.game.utils.GameUtils;

import java.math.BigDecimal;
import java.util.List;


/**
 * Handles the movements of all computer-controlled game objects
 * @see ComputerControlledObject
 */
public class ComputerControlledMovementHandler {


    private final List<ComputerControlledObject> COMPUTER_CONTROLLED_GAME_OBJECTS;
    private final CollisionHandler COLLISION_HANDLER;
    private final long SLEEP_DURATION_BETWEEN_MOVEMENTS;
    private final GamePlayDisplay GAME_PANEL;
    private final Object STOP_START_MUTEX = new Object();
    private boolean run;
    private volatile Thread movingThread;


    public ComputerControlledMovementHandler(
            GamePlayDisplay gameDisplay, List<ComputerControlledObject> computerControlledGameObjects, CollisionHandler collisionHandler) {

        this.GAME_PANEL = gameDisplay;
        this.COMPUTER_CONTROLLED_GAME_OBJECTS = computerControlledGameObjects;
        this.COLLISION_HANDLER = collisionHandler;

        //Derive the sleep value using the num movement cycles per sec
        long num_movement_cycles_ps = Long.parseLong(GlobalConfig.getInstance().getProperty("num_movement_cycles_ps"));
        num_movement_cycles_ps = GameUtils.getScaledMsToScreenWidthValue(new BigDecimal(num_movement_cycles_ps));
        SLEEP_DURATION_BETWEEN_MOVEMENTS = 1000L/num_movement_cycles_ps;
    }

    public void start() {
        synchronized (STOP_START_MUTEX) {
            if (run) {
                throw new IllegalArgumentException("Already running");
            }

            movingThread = new Thread(() -> {
                //Move each object then sleep for the configured number of millis
                while (run) {
                    for (int i = 0; i < COMPUTER_CONTROLLED_GAME_OBJECTS.size() && run; i++) {
                        ComputerControlledObject computerControlledObject = COMPUTER_CONTROLLED_GAME_OBJECTS.get(i);
                        if(computerControlledObject.doMove(COLLISION_HANDLER)) {
                            GAME_PANEL.isThisAShowstopper(computerControlledObject);
                        }
                    }

                    if(run) {
                        GameUtils.sleepAndCatchInterrupt(SLEEP_DURATION_BETWEEN_MOVEMENTS);
                    }
                }
            });
            movingThread.setName("Mover for moving game objects");
            movingThread.setDaemon(true);

            run = true;

            movingThread.start();
        }
    }

    public void stop() throws InterruptedException {
        synchronized (STOP_START_MUTEX) {
            if (!run) {
                throw new IllegalArgumentException("Not running");
            }

            run = false;
            movingThread.join();
        }
    }

    public void setEverythingToStartingState() {
        for (ComputerControlledObject computerControlledObject : COMPUTER_CONTROLLED_GAME_OBJECTS) {
            computerControlledObject.setToStartingState();
        }
    }
}
