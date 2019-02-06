package com.noomtech.jsw.game.movement;

import com.noomtech.jsw.common.utils.GlobalConfig;
import com.noomtech.jsw.game.gameobjects.objects.Nasty;
import com.noomtech.jsw.game.gamethread.GamePanel;
import com.noomtech.jsw.game.utils.GameUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;


/**
 * Handles the movements of all the nasties in one thread:
 *  1:  Loop through all nasties and move each one once
 *  2: Sleep for a configured amount of time
 *  3: Goto 1
 *
 * @see Nasty
 * @author Joshua Newman
 */
public class NastiesHandler implements Runnable {


    private List<Nasty> nasties;
    private final CollisionHandler COLLISION_HANDLER;
    private long sleepDurationBetweenMovements;
    private GamePanel parent;


    public NastiesHandler(
            GamePanel parent, List<Nasty> nasties, CollisionHandler collisionHandler) {

        this.parent = parent;
        this.nasties = nasties;
        this.COLLISION_HANDLER = collisionHandler;

        //Derive the sleep value using the num movement cycles per sec
        long num_movement_cycles_ps = Long.parseLong(GlobalConfig.getInstance().getProperty("num_movement_cycles_ps"));
        num_movement_cycles_ps = GameUtils.getScaledMsToScreenWidthValue(new BigDecimal(num_movement_cycles_ps));
        sleepDurationBetweenMovements = 1000L/num_movement_cycles_ps;
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
     * Move the nasty
     * @see Nasty
     */
    private void doMove(Nasty nasty) {
        if(nasty.doMove(COLLISION_HANDLER)) {
            //The player's hit a nasty
            Executors.newSingleThreadExecutor().submit(() -> {parent.processShowstopper(nasty);});
        }
    }
}
