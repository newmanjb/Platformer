package com.noomtech.jsw.game.movement;

import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.objects.Nasty;
import com.noomtech.jsw.game.gamethread.GamePanel;
import com.noomtech.jsw.game.utils.GameUtils;

import java.util.List;
import java.util.concurrent.Executors;


/**
 * Handles the movements of the nasties.
 * @see Nasty
 * @author Joshua Newman
 */
public class NastiesHandler implements Runnable {


    private List<Nasty> nasties;
    private GameObject[][] checkWhenMovingDown;
    private GameObject[][] checkWhenMovingUp;
    private long sleepDurationBetweenMovements;
    private GamePanel parent;


    public NastiesHandler(
            GamePanel parent, List<Nasty> nasties, GameObject[][] checkWhenMovingDown,
            GameObject[][] checkWhenMovingUp, long sleepDurationBetweenMovements) {

        this.parent = parent;
        this.nasties = nasties;
        this.checkWhenMovingDown = checkWhenMovingDown;
        this.checkWhenMovingUp = checkWhenMovingUp;
        this.sleepDurationBetweenMovements = sleepDurationBetweenMovements;
    }

    public void run() {
        //Move each nasty the configured number of pixels and then sleep for the configured number of millis before
        // moving them again
        while(parent.shouldRunNastiesThread()) {
            for (Nasty nasty : nasties) {
                doMove(nasty);
            }

            GameUtils.sleepAndCatchInterrupt(sleepDurationBetweenMovements);
        }
    }

    /**
     * Move the nasty the given number of pixels
     * @see Nasty
     */
    private void doMove(Nasty nasty) {
        int moveDirection = nasty.getMoveYDirection();
        GameObject collidedWith = moveDirection > 0 ? nasty.checkIfBottomIsTouching(checkWhenMovingDown) : nasty.checkIfTopIsTouching(checkWhenMovingUp);
        if(collidedWith != null) {
            nasty.onCollision();
        }
        nasty.setLocation(nasty.getX(), nasty.getY() + nasty.getMoveYDirection());
        nasty.onMove();
        if(parent.getPlayerCollisionArea().intersects(nasty.getImageArea())) {
            //The player's hit a nasty!!!!
            Executors.newSingleThreadExecutor().submit(() -> {parent.processShowstopper(nasty);});
        }
    }
}
