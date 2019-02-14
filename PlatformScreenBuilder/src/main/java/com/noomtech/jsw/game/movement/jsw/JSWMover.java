package com.noomtech.jsw.game.movement.jsw;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Runnable class that updates the location of the JSW.  Used by the movement classes.
 */
public abstract class JSWMover implements Runnable {

    //the service used to run this runnable
    private final ExecutorService executorService;
    //true if this mover running
    protected volatile boolean running;
    //the distance that the sprite moves before pausing and the num millis the pause is for.  These 2 together
    //control the speed of the movements
    protected final int numPixelsBetweenSleeps;
    protected final int numMillisBetweenMovements;
    //true if a request has been made for the movement to stop e.g. if the key governing the movement has been released
    protected volatile boolean stopRequestReceived;
    //the process that is running the routine
    private Future future;

    public JSWMover(ExecutorService executorService,
                     int numPixelsPerMovement, int numMillisBetweenMovements) {
        this.executorService = executorService;
        this.numPixelsBetweenSleeps = numPixelsPerMovement;
        this.numMillisBetweenMovements = numMillisBetweenMovements;
    }


    public void start() {
        running = true;
        future = executorService.submit(this);
    }

    /**
     * Submits a request to stop the movement gracefully.  If the "wait" parameter is true then this blocks until the thread for the routine has completed.
     * Note that the {@link com.noomtech.jsw.game.handlers.JSWControlsHandler} will still be notified when this movement is finished either way.
     */
    public void requestStop(boolean wait) {
        stopRequestReceived = true;
        if(future != null) {
            if(wait) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void shutdown() {
        stopRequestReceived = true;
        executorService.shutdown();
    }
}

