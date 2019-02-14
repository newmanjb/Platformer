package com.noomtech.jsw.game.movement.jsw;

import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.concrete_objects.JSW;
import com.noomtech.jsw.game.handlers.JSWControlsHandler;
import com.noomtech.jsw.game.handlers.CollisionHandler;
import com.noomtech.jsw.game.utils.GameUtils;

import java.util.concurrent.Executors;


/**
 * Handles the movement of the JSW when its falling
 */
public class FallMover extends JSWMover {

    private final JSW JSW;
    private final JSWControlsHandler JSW_CONTROLS_HANDLER;
    private final CollisionHandler COLLISION_HANDLER;


    public FallMover(JSW jsw, JSWControlsHandler jswControlsHandler, int num_PixelsPerMovement, int num_MillisBetweenMovements, CollisionHandler collisionHandler) {
        super(Executors.newSingleThreadExecutor(),
                num_PixelsPerMovement,
                num_MillisBetweenMovements);
        this.JSW = jsw;
        this.COLLISION_HANDLER = collisionHandler;
        this.JSW_CONTROLS_HANDLER = jswControlsHandler;
    }

    @Override
    public void run() {

        stopRequestReceived = false;
        boolean stopMoving = false;
        int numPixelsFallen = 0;
        while (!stopMoving) {
            GameObject touching = COLLISION_HANDLER.checkIfTouchingAnythingGoingDown(JSW);
            if(touching != null) {
                stopMoving = JSW_CONTROLS_HANDLER.hitWhileFalling(touching);
            }
            if(!stopMoving) {
                JSW.setLocation(JSW.getImageArea().x, JSW.getImageArea().y + 1);
                numPixelsFallen++;
                JSW.onMove();
            }

            stopMoving = stopMoving || stopRequestReceived;
            if (!stopMoving && (numPixelsFallen % numPixelsBetweenSleeps == 0)) {
                GameUtils.sleepAndCatchInterrupt(numMillisBetweenMovements);
            }
        }
        running = false;
    }
}