package com.noomtech.jsw.game.movement.jsw;

import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.concrete_objects.JSW;
import com.noomtech.jsw.game.handlers.JSWControlsHandler;
import com.noomtech.jsw.game.handlers.CollisionHandler;
import com.noomtech.jsw.game.utils.GameUtils;

import java.util.concurrent.Executors;


/**
 * Moves the jsw left
 * @see JSWMover
 * @see JSWControlsHandler
 */
public class LeftMover extends JSWMover {


    private final JSW JSW;
    private final CollisionHandler COLLISION_HANDLER;
    private final JSWControlsHandler JSW_CONTROLS_HANDLER;


    public LeftMover(CollisionHandler collisionHandler, JSWControlsHandler jswControlsHandler, JSW jsw, int num_PixelsPerMovement, int num_millisBetweenMovements) {
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
            GameObject touching = COLLISION_HANDLER.checkIfTouchingAnythingGoingLeft(JSW);
            if (touching == null) {
                //Not touching anything so jsw can move
                numPixelsMoved = move(numPixelsMoved);
                //Now that it's moved dheck if JSW should fall
                hasBeenStopped = JSW_CONTROLS_HANDLER.doFallCheck();
            }
            else {
                //Jsw is touching something so see if this is something that will stop up
                hasBeenStopped = JSW_CONTROLS_HANDLER.hitWhileWalking(touching, PlayerMovementType.WALK_LEFT);
                if(!hasBeenStopped) {
                    numPixelsMoved = move(numPixelsMoved);
                    //Now that it's moved dheck if JSW should fall
                    hasBeenStopped = JSW_CONTROLS_HANDLER.doFallCheck();
                }
            }

            if(!hasBeenStopped) {
                //Movement has finished without being stopped prematurely
                if(stopRequestReceived) {
                    hasBeenStopped = true;
                    JSW_CONTROLS_HANDLER.leftRightMoveFinishedWithoutBeingStopped(PlayerMovementType.WALK_LEFT);
                }
                else {
                    //Even though the movement has finished without being stopped then, if no stop request has been
                    //received, initiate another cycle pausing beforehand if necessary
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
        JSW.setLocation(JSW.getX() - 1, JSW.getY());
        JSW.onMove();
        return ++numberPixelsMoved;
    }
};

