package com.noomtech.jsw.game.movement.jsw;


import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.concrete_objects.JSW;
import com.noomtech.jsw.game.handlers.JSWControlsHandler;
import com.noomtech.jsw.game.handlers.CollisionHandler;
import com.noomtech.jsw.game.utils.GameUtils;

import java.util.concurrent.Executors;


/**
 * Moves the jsw right
 * @see JSWMover
 * @see JSWControlsHandler
 */
public class RightMover extends JSWMover {


    private final JSW JSW;
    private final CollisionHandler COLLISION_HANDLER;
    private final JSWControlsHandler JSW_CONTROLS_HANDLER;


    public RightMover(CollisionHandler collisionHandler, JSWControlsHandler jswControlsHandler, JSW jsw, int num_PixelsPerMovement, int num_millisBetweenMovements) {
        super(Executors.newSingleThreadExecutor(), num_PixelsPerMovement,
                num_millisBetweenMovements);
        this.JSW = jsw;
        this.JSW_CONTROLS_HANDLER = jswControlsHandler;
        this.COLLISION_HANDLER = collisionHandler;
    }

    @Override
    public void run() {
        boolean hasBeenStopped = false;
        stopRequestReceived = false;
        int numPixelsMoved = 0;
        while(!hasBeenStopped) {
            GameObject touching = COLLISION_HANDLER.checkIfTouchingAnythingGoingRight(JSW);
            if (touching == null) {
                numPixelsMoved = move(numPixelsMoved);
                hasBeenStopped = JSW_CONTROLS_HANDLER.doFallCheck();
            }
            else {
                hasBeenStopped = JSW_CONTROLS_HANDLER.hitWhileWalking(touching, PlayerMovementType.WALK_RIGHT);
                if(!hasBeenStopped) {
                    numPixelsMoved = move(numPixelsMoved);
                    hasBeenStopped = JSW_CONTROLS_HANDLER.doFallCheck();
                }
            }

            if(!hasBeenStopped) {
                if(stopRequestReceived) {
                    hasBeenStopped = true;
                    JSW_CONTROLS_HANDLER.leftRightMoveFinishedWithoutBeingStopped(PlayerMovementType.WALK_RIGHT);
                }
                else {
                    if((numPixelsMoved % numPixelsBetweenSleeps) == 0) {
                        GameUtils.sleepAndCatchInterrupt(numMillisBetweenMovements);
                    }
                }
            }
        }
        running = false;
    }

    private int move(int numberPixelsMoved) {
        //We can move
        JSW.setLocation(JSW.getX() + 1, JSW.getY());
        JSW.onMove();
        return ++numberPixelsMoved;
    }
};
