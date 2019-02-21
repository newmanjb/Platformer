package com.noomtech.jsw.game.movement.jsw;

import com.noomtech.jsw.game.events.GameEventReceiver;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.concrete_objects.JSW;
import com.noomtech.jsw.game.handlers.JSWControlsHandler;
import com.noomtech.jsw.game.handlers.CollisionHandler;
import com.noomtech.jsw.game.utils.GameUtils;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Handles the movement of the sprite when its jumping
 * @see JSWMover
 * @see JSWControlsHandler
 */
public class JumpMover extends JSWMover {

    private final JSW JSW;
    private final CollisionHandler COLLISION_HANDLER;
    private final JSWControlsHandler JSW_CONTROLS_HANDLER;


    //Defines the increments that need to be made to the sprite's location in order to get them to jump left, right
    //or up.

    private int[][] trajectoryRight;
    private int[][] trajectoryLeft;
    private int[][] trajectoryUp;


    public JumpMover(JSW jsw, int num_pixelsPerMovement, int num_millisBetweenMovements,
                     CollisionHandler collisionHandler, JSWControlsHandler jswControlsHandler) {

        super(Executors.newSingleThreadExecutor(), num_pixelsPerMovement,
                num_millisBetweenMovements);

        this.JSW = jsw;
        this.COLLISION_HANDLER = collisionHandler;
        this.JSW_CONTROLS_HANDLER = jswControlsHandler;

        int playerWidth = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(JSW.getWidth()));
        int playerHeight = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(JSW.getHeight()));

        //Jumping consists of 3 types of movement 1: up and across, 2: across 3: down and across.
        //Each movement is only 1 pixel up/down and/or across but the number of these movements needs to be scaled
        // in relation to the screen size.
        int numOfMovementsUpAndAcross = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(playerHeight));
        int numOfMovementsAcross = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(playerWidth));
        int numOfMovementsDownAndAcross = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(playerHeight));
        List<int[]> trajectoryRightList = new ArrayList(numOfMovementsUpAndAcross + numOfMovementsAcross + numOfMovementsDownAndAcross);
        for(int i = 0 ; i < numOfMovementsUpAndAcross ; i++) {
            trajectoryRightList.add(new int[]{1,-1});
        }
        for(int i = numOfMovementsUpAndAcross ; i <numOfMovementsUpAndAcross + numOfMovementsAcross ; i++) {
            trajectoryRightList.add(new int[]{1,0});
        }
        for(int i = numOfMovementsUpAndAcross + numOfMovementsAcross ; i < numOfMovementsUpAndAcross + numOfMovementsAcross + numOfMovementsDownAndAcross ; i++) {
            trajectoryRightList.add(new int[]{1,1});
        }
        List<int[]> trajectoryLeftList = new ArrayList<>(numOfMovementsUpAndAcross + numOfMovementsAcross + numOfMovementsDownAndAcross);
        for(int i = 0 ; i < numOfMovementsUpAndAcross ; i++) {
            trajectoryLeftList.add(new int[]{-1,-1});
        }
        for(int i = numOfMovementsUpAndAcross ; i <numOfMovementsUpAndAcross + numOfMovementsAcross ; i++) {
            trajectoryLeftList.add(new int[]{-1,0});
        }
        for(int i = numOfMovementsUpAndAcross + numOfMovementsAcross ; i < numOfMovementsUpAndAcross + numOfMovementsAcross + numOfMovementsDownAndAcross ; i++) {
            trajectoryLeftList.add(new int[]{-1,1});
        }
        int numTrajectoryUpMovements = GameUtils.getScaledPixelsToScreenHeightValue(new BigDecimal(100));
        List<int[]> trajectoryUpList = new ArrayList<>(numTrajectoryUpMovements);
        for(int i = 0 ; i < numTrajectoryUpMovements ; i++) {
            trajectoryUpList.add(new int[]{0,-1});
        }
        for(int i = numTrajectoryUpMovements ; i < (numTrajectoryUpMovements * 2) ; i++) {
            trajectoryUpList.add(new int[]{0,1});
        }

        trajectoryLeft = trajectoryLeftList.toArray(new int[trajectoryLeftList.size()][2]);
        trajectoryRight = trajectoryRightList.toArray(new int[trajectoryRightList.size()][2]);
        trajectoryUp = trajectoryUpList.toArray(new int[trajectoryUpList.size()][2]);
    }

    @Override
    public void run() {

        boolean stoppedBeforeFinishing = false;

        //Get the trajectory depending on which direction we are jumping in
        int [][]trajectoryBeingUsed = JSW_CONTROLS_HANDLER.leftKeyPressed ? trajectoryLeft :
                JSW_CONTROLS_HANDLER.rightKeyPressed ? trajectoryRight : trajectoryUp;

        GameEventReceiver.getInstance().onPlayerJumping();

        //Step along the trajectory array making the increments to the sprite's location.  If something blocks
        //our path then refer to the controller.
        int i;
        for (i = 0; i < trajectoryBeingUsed.length && !stoppedBeforeFinishing; i++) {
            int[] movements = trajectoryBeingUsed[i];
            Point proposedNewLocation  = new Point(JSW.getX() + movements[0], JSW.getY() + movements[1]);

            if (movements[1] != 0) {
                //JSW needs to move up or down
                GameObject touching = movements[1] < 0 ? COLLISION_HANDLER.checkIfTouchingAnythingGoingUp(JSW) : COLLISION_HANDLER.checkIfTouchingAnythingGoingDown(JSW);
                JumpMovementResult jumpMovementResult = JumpMovementResult.MOVE;
                if(touching != null) {
                    //If the jsw has hit something while jumping then it might stop the entire movement or just movement
                    //in this direction
                    jumpMovementResult = JSW_CONTROLS_HANDLER.hitWhileJumping(touching, movements, true);
                    stoppedBeforeFinishing = jumpMovementResult == JumpMovementResult.STOP_COMPLETELY;
                }

                if(jumpMovementResult == JumpMovementResult.MOVE) {
                    updateLocationY(proposedNewLocation);
                }
            }

            if (!stoppedBeforeFinishing && movements[0] != 0) {
                //JSW needs to moves left or right
                GameObject touching = movements[0] < 0 ? COLLISION_HANDLER.checkIfTouchingAnythingGoingLeft(JSW) : COLLISION_HANDLER.checkIfTouchingAnythingGoingRight(JSW);
                JumpMovementResult jumpMovementResult = JumpMovementResult.MOVE;
                if(touching != null) {
                    jumpMovementResult = JSW_CONTROLS_HANDLER.hitWhileJumping(touching, movements, false);
                    stoppedBeforeFinishing = jumpMovementResult == JumpMovementResult.STOP_COMPLETELY;
                }

                if(jumpMovementResult == JumpMovementResult.MOVE) {
                    updateLocationX(proposedNewLocation);
                }
            }

            if(!stoppedBeforeFinishing && (i + 1) % numPixelsBetweenSleeps == 0) {
                GameUtils.sleepAndCatchInterrupt(numMillisBetweenMovements);
            }
        }

        //If the jsw has completed the jump without being stopped prematurely then tell the controller it's finished.
        // Note that if it was stopped prematurely then the controller will already have been notified so we don't need to tell it
        //anything.
        if(i == trajectoryBeingUsed.length && !stoppedBeforeFinishing) {
            JSW_CONTROLS_HANDLER.jumpFinishedWithoutBeingStopped();
        }
        GameEventReceiver.getInstance().onPlayerStoppedJumping();
        running = false;
    }

    private void updateLocationY(Point proposedLocation) {
        JSW.setLocation(JSW.getX(), proposedLocation.y);
        JSW.onMove();
    }

    private void updateLocationX(Point proposedLocation) {
        JSW.setLocation(proposedLocation.x, JSW.getY());
        JSW.onMove();
    }
}
