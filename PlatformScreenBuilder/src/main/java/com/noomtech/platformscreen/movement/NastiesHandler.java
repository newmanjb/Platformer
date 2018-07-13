package com.noomtech.platformscreen.movement;

import com.noomtech.platformscreen.gameobjects.GameObject;
import com.noomtech.platformscreen.gameobjects.Nasty;
import com.noomtech.platformscreen.gamethread.GamePanel;
import com.noomtech.platformscreen.utils.Utils;

import java.util.List;


/**
 * Handles the movements of the nasties.
 * @see Nasty
 * @author Joshua Newman
 */
public class NastiesHandler implements Runnable {


    private List<Nasty> nasties;
    private GameObject[][] checkWhenMovingDown;
    private GameObject[][] checkWhenMovingUp;
    private int numPixelsBeforeSleeping;
    private long sleepDurationBetweenMovements;
    private GamePanel parent;


    public NastiesHandler(
            GamePanel parent, List<Nasty> nasties, GameObject[][] checkWhenMovingDown,
            GameObject[][] checkWhenMovingUp, int numPixelsBeforeSleeping, long sleepDurationBetweenMovements) {

        this.parent = parent;
        this.nasties = nasties;
        this.checkWhenMovingDown = checkWhenMovingDown;
        this.checkWhenMovingUp = checkWhenMovingUp;
        this.numPixelsBeforeSleeping = numPixelsBeforeSleeping;
        this.sleepDurationBetweenMovements = sleepDurationBetweenMovements;
    }

    public void run() {
        //Move each nasty the configured number of pixels and then sleep for the configured number of millis before
        // moving them again
        while(parent.shouldRunNastiesThread()) {
            for (Nasty nasty : nasties) {
                doMove(nasty);
            }

            Utils.sleepAndCatchInterrupt(sleepDurationBetweenMovements);
        }
    }

    /**
     * Move the nasty the given number of pixels
     * @see Nasty
     */
    private void doMove(Nasty nasty) {
        for (int i = 0; i < numPixelsBeforeSleeping; i++) {
            int moveDirection = nasty.getMoveYDirection();
            int yOrdinateToCheck = moveDirection > 0 ? nasty.getY() + nasty.getHeight() : nasty.getY();
            GameObject[] possibleCollisionArea = moveDirection > 0 ? checkWhenMovingDown[yOrdinateToCheck] :
                    checkWhenMovingUp[yOrdinateToCheck + moveDirection];
            GameObject collidedwith = Utils.getCollidedWhileMovingUpOrDown(nasty, possibleCollisionArea);
            if(collidedwith != null) {
                nasty.onCollision();
            }
            nasty.setLocation(nasty.getX(), nasty.getY() + nasty.getMoveYDirection());
            nasty.onMove(PlayerMovementType.FALL);
            if(parent.getPlayerCollisionArea().intersects(nasty.getCollisionArea())) {
                //The player's hit a nasty!!!!
                parent.playerHitLethalObject(nasty);
            }

        }
    }
}
