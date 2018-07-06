package com.noomtech.platformscreen.frame;

import com.noomtech.platformscreen.gameobjects.GameObject;
import com.noomtech.platformscreen.gameobjects.JSW;
import com.noomtech.platformscreen.gameobjects.Nasty;
import com.noomtech.platformscreen.gameobjects.Platform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Currently this is where it all happens.  This panel class is responsible for the painting, control handling and collision detection in the game.
 *
 * The game itself is a simple Manic Miner/Jet Set Willy type thing i.e. with controls of "left", "right" and "jump" in a screen full of
 * platforms.
 *
 * There is quite a lot of code around obtaining a high level of precision on the controls, as this is important in a platform game.
 *
 * Platforms or "collision areas" are represented on screen as {@link Rectangle}s and the main sprite is too, with the idea
 * that these will be replaced with something nicer looking where only their boundaries are defined by rectangles.
 *
 * @author Joshua Newman
 */
public class GamePanel extends JPanel {


    private static final int SCREEN_SIZE = 1920;

    //The platforms.
    private final List<Platform> platforms;
    //The nasties
    private final List<Nasty> nasties;
    //True if the game is running.  Used to keep the game loop going and to stop it
    private volatile boolean started = false;
    //The player sprite (jsw = "Jet Set Willy")
    private final JSW jsw;
    //Coordinates the keyboard input and moves the player sprite in the appropriate direction
    private final JSWControlsHandler jSWControlsHandler;

    //Each of thes arrays below is as long as the screen size (which is currently square).  The relevant boundary of each
    //platform in relation to the direction that the player sprite happens to be moving forms its index in the appropriate array.
    //These arrays are then used by the functionality that controls the player's movement in order to quickly detect
    //whether they have collided with something e.g.

    //if there is a platform (rectangle) at x = 50, y=70 that is 20 wide and 10 high then the left boundary (x=50) is the only thing
    //that the player will be able to collide with when moving right, so this rectangle will be placed at index 50 in the
    //array that is checked when the play is moving right.  The lower boundary at y=80 (70 + 10) will be what the functionality
    //cares about when the player sprite is moving up, so the rectangle will be placed at index 80 in the list that is checked when
    //the spring is moving up the screen.

    private final Rectangle[] goingRightCaresAbout = new Rectangle[SCREEN_SIZE];
    private final Rectangle[] goingLeftCaresAbout = new Rectangle[SCREEN_SIZE];
    private final Rectangle[] goingUpCaresAbout = new Rectangle[SCREEN_SIZE];
    private final Rectangle[] goingDownCaresAbout = new Rectangle[SCREEN_SIZE];


    GamePanel(List<JSW> player, List<Platform> platforms, List<Nasty> nasties) {

        jsw = player.get(0);
        this.platforms = platforms;
        this.nasties = nasties;

        //Some hard-coded data for when we can't access cassandra and want to do some testing

//        jsw = new Rectangle(600,499,50,100);
//
//        Rectangle c1 = new Rectangle(150, 570, 100,30);
//        Rectangle c2 = new Rectangle(1200, 100, 200,500);
//        Rectangle c3 = new Rectangle(900, 380, 200,100);
//        Rectangle c4 = new Rectangle(0, 600, 1200,20);
//        Rectangle c5 = new Rectangle(0, 0, 20,600);
//        this.platforms.addAll(Arrays.asList(new Rectangle[]{c1,c2,c3,c4,c5}));

        for(Platform platform : platforms) {
            Rectangle r = platform.getCollisionArea();
            Point p = r.getLocation();
            goingLeftCaresAbout[p.x + r.width] = r;
            goingRightCaresAbout[p.x] = r;
            goingUpCaresAbout[p.y + r.height] = r;
            goingDownCaresAbout[p.y] = r;
        }

        jSWControlsHandler = new JSWControlsHandler(new LeftMover(), new RightMover(), new JumpMover());

        this.addKeyListener(jSWControlsHandler);

        setFocusable(true);
    }

    void start() {
        //The game thread that repaints, sleeps and then repaints again until the game is stopped
        started = true;
        while(started) {
            try {
                SwingUtilities.invokeAndWait(() -> repaint());
                Thread.sleep(25);
            }
            catch(InterruptedException e) {
                System.out.println("Game loop thread interrupted.  Stopping game");
                started = false;
            }
            catch(InvocationTargetException e) {
                System.out.println(e);
            }
        }
    }

    void stop() {
        started = false;
        jSWControlsHandler.shutdown();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.WHITE);
        Dimension size = getSize();
        g.fillRect(0,0,size.width,size.height);

        jsw.paintIt(g);
        for(Platform platform : platforms) {
            platform.paintIt(g);
        }

        for(Nasty nasty : nasties) {
            nasty.paintIt(g);
        }
    }

    private class JSWControlsHandler implements KeyListener {

        private final char KEY_LEFT = 'o';
        private final char KEY_RIGHT = 'p';
        private final char KEY_JUMP = 'm';

        //Each of these instances handle the movement of the sprite in a particular direction.  They are Runnable instances
        //that are run on separate threads that update the location of the player sprite in order to move it in particular directions.
        //The sprite is then repainted by the repainting loop meaning that the player sprite is seen to move around the screen.
        private final Mover leftMover;
        private final Mover rightMover;
        //Handles the movement for when the player sprite is falling
        private final Mover fallMover;
        private final Mover jumpMover;

        private volatile boolean leftKeyPressed;
        private volatile boolean rightKeyPressed;
        private volatile boolean jumpKeyPressed;


        private JSWControlsHandler(Mover leftMover, Mover rightMover, JumpMover jumpMover) {
            this.leftMover = leftMover;
            this.rightMover = rightMover;
            this.jumpMover = jumpMover;
            this.fallMover = new FallMover();
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch(e.getKeyChar()) {
                case(KEY_LEFT): {
                    leftKeyPressed = true;
                    //Can't move left if we're already moving left or already moving in any other direction
                    if(!leftMover.isRunning() && !rightMover.isRunning() && !jumpMover.isRunning() && !fallMover.isRunning()) {
                        leftMover.start();
                    }
                    break;
                }
                case(KEY_RIGHT): {
                    rightKeyPressed = true;
                    if(!leftMover.isRunning() && !rightMover.isRunning() && !jumpMover.isRunning() && !fallMover.isRunning()) {
                        rightMover.start();
                    }
                    break;
                }
                case(KEY_JUMP): {
                    jumpKeyPressed = true;
                    //Can't jump if we're falling or already jumping
                    if(!fallMover.isRunning() && !jumpMover.isRunning()) {
                        //If we're already moving left or right we will jump in that direction but we must stop the
                        //movement first otherwise it will interfere with the jumping movment
                        if(leftMover.isRunning()) {
                            leftMover.requestStopAndWait();
                        }
                        else if(rightMover.isRunning()) {
                            rightMover.requestStopAndWait();
                        }
                        jumpMover.start();
                    }
                    break;
                }
                default:{}
            }
        }

        //Called when a movement has finished.  This could be because the player has collided with something, released
        //the key for that movement or is now falling e.g. if they have walked off the end of a platform
        private void movementFinished(MovementType movementType, boolean falling) {

            //If we need to fall then this takes priority over everything else
            if(falling && movementType != MovementType.FALL) {
                fallMover.start();
            }
            else if(movementType == MovementType.JUMP) {
                //if we've just finished a jump and the user has been holding down the left or right key then start the
                //player sprite moving in that direction
                if(leftKeyPressed) {
                    leftMover.start();
                }
                else if(rightKeyPressed) {
                    rightMover.start();
                }
            }
            else if(movementType == MovementType.FALL) {
                //if we've just finished falling and the user has been holding down the left, right or jump key then start the
                //player sprite moving accordingly
                if(jumpKeyPressed) {
                    jumpMover.start();
                }
                else if(leftKeyPressed) {
                    leftMover.start();
                }
                else if(rightKeyPressed) {
                    rightMover.start();
                }
            }
            //if we've just finished moving left or right and the user has been holding down the key for moving
            //in the opposite direction then start moving in that direction
            else if(movementType == MovementType.WALK_LEFT) {
                //if we've just finished moving left or right and the user has been holding down the key for moving
                //in the opposite direction then start moving in that direction
                if(rightKeyPressed && jumpKeyPressed) {
                    jumpMover.start();
                }
                else if(rightKeyPressed) {
                    rightMover.start();
                }
            }
            else if(movementType == MovementType.WALK_RIGHT) {
                if(leftKeyPressed && jumpKeyPressed) {
                    jumpMover.start();
                }
                else if(leftKeyPressed) {
                    leftMover.start();
                }
            }
        }

        //Stop the movement for that key when it's released
        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyChar()) {
                case (KEY_LEFT): {
                    leftKeyPressed = false;
                    leftMover.requestStop();
                    break;
                }
                case (KEY_RIGHT): {
                    rightKeyPressed = false;
                    rightMover.requestStop();
                    break;
                }
                case (KEY_JUMP): {
                    jumpKeyPressed = false;
                    jumpMover.requestStop();
                    break;
                }
                default: {
                }
            }
        }

        private void shutdown() {
            leftMover.shutdown();
            rightMover.shutdown();
            jumpMover.shutdown();
            fallMover.shutdown();
        }
    }

    //Represents all the possible movements of the player sprite
    private enum MovementType {
        WALK_LEFT,
        WALK_RIGHT,
        JUMP,
        FALL;
    }

    //Runnable class that updates the location of a sprite
    private abstract class Mover implements Runnable {


        private final MovementType movementType;
        //the service used to run this runnable
        private final ExecutorService executorService;
        //true if this mover running
        private volatile boolean running;
        //the distance that the sprite moves before sleeping or checking other variables
        protected final int numPixelsPerMovement;
        //the num millis to sleep between the aforementioned movements (if applicable).  This is for controlling the
        //speed at which the sprite moves.
        private final int numMillisBetweenMovements;
        //true if a request has been made for the movement to stop e.g. if the key governing the movement has been released
        private volatile boolean stopRequestReceived;
        //the process that is running the routine
        private Future future;


        private Mover(MovementType movementType, ExecutorService executorService,
                      int numPixelsPerMovement, int numMillisBetweenMovements) {
            this.movementType = movementType;
            this.executorService = executorService;
            this.numPixelsPerMovement = numPixelsPerMovement;
            this.numMillisBetweenMovements = numMillisBetweenMovements;
        }


        private void start() {
            running = true;
            future = executorService.submit(this);
        }

        /**
         * Submits a request to stop and returns.  Doesn't block.  The {@link JSWControlsHandler} will be notified
         * when this movement is finished.
         */
        protected void requestStop() {
            stopRequestReceived = true;
        }

        /**
         * As for {@link #requestStop()} except this blocks until the thread for the routine has completed.
         * The {@link JSWControlsHandler} will still be notified when this movement is finished.
         */
        private void requestStopAndWait() {
            stopRequestReceived = true;
            try {
                future.get();
            }
            catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        private boolean isRunning() {
            return running;
        }

        //Call the subclasses movement routine.  This will move the sprite the minimum distance required
        //before the routine needs to pause.  If it returns true then the entire move has finished and we can
        //stop moving if we have received a request to stop, otherwise we can start moving again.  For example, a jump may consist
        //of many of these move-pause, move-pause, move-pause cycles before it finishes and returns true, as jumping moves
        // the player sprite across quite a long distance, whereas moving left or right may only consist of one cycle.
        // Once the movement has finished then a check is made to see if the sprite should now fall, and if so this
        //will stop the movement even if it has not been requested to stop.
        //Also note that most movement subclasses will move the sprite less than the minimumdistanc and immediately return
        //true if they detect that the sprite has collided with something.
        public void run() {
            stopRequestReceived = false;
            boolean shouldFall = false;
            boolean stopMoving = false;
            while(!stopMoving) {
                boolean moveFinished = doMove();
                shouldFall = shouldStartFalling();
                stopMoving = moveFinished && (stopRequestReceived || shouldFall);
                if(!stopMoving) {
                    sleepAndShouldNotBeInterrupted(numMillisBetweenMovements);
                }
            }

            running = false;
            jSWControlsHandler.movementFinished(movementType, shouldFall);
        }

        protected abstract boolean doMove();

        private boolean shouldStartFalling() {
            if(movementType == MovementType.FALL) {
                return false;
            }
            Rectangle jswCollisionArea = jsw.getCollisionArea();
            return checkNotCollidedWhileMovingUpOrDown(jswCollisionArea.y +
                            jswCollisionArea.height + 1,
                    false);
        }

        private void shutdown() {
            stopRequestReceived = true;
            executorService.shutdown();
        }
    }

    //Handles the movement of a sprite when its falling
    private class FallMover extends Mover {

        private FallMover() {
            super(MovementType.FALL, Executors.newSingleThreadExecutor(), 5, 50);
        }
        @Override
        protected boolean doMove() {
            int startCheckpoint = jsw.getY() + jsw.getHeight() + 1;
            int endCheckPoint = jsw.getY() + jsw.getHeight() + numPixelsPerMovement;
            boolean notCollided = true;
            int i;
            for(i = startCheckpoint ; i <= endCheckPoint && notCollided ;i++) {
                notCollided = checkNotCollidedWhileMovingUpOrDown(i, false);
                if(notCollided) {
                    //We can fall
                    jsw.setLocation(jsw.getCollisionArea().x, i-jsw.getCollisionArea().height);
                }
                else {
                    //@todo - this is stopping itself rather than the loop in the superclass doing it.  Can this be done
                    //better?
                    requestStop();
                }
            }

            return true;
        }
    }

    //Handles the movement of the sprite when its jumping
    private class JumpMover extends Mover {

        //Defines the increments that need to be made to the sprite's location in order to get them to jump left, right
        //or up.

        private final int[][] trajectoryRight;
        private final int[][] trajectoryLeft;
        private final int[][] trajectoryUp;

        private int startIndexInTrajectory;

        private int[][] trajectoryBeingUsed;


        private JumpMover() {

            super(MovementType.JUMP,Executors.newSingleThreadExecutor(), 1, 10);

            trajectoryRight = new int[150][2];
            for(int i = 0 ; i < 50 ; i++) {
                trajectoryRight[i] = new int[]{1,-1};
            }
            for(int i = 50 ; i < 100 ; i++) {
                trajectoryRight[i] = new int[]{1,0};
            }
            for(int i = 100 ; i < 150 ; i++) {
                trajectoryRight[i] = new int[]{1,1};
            }
            trajectoryLeft = new int[150][2];
            for(int i = 0 ; i < 50 ; i++) {
                trajectoryLeft[i] = new int[]{-1,-1};
            }
            for(int i = 50 ; i < 100 ; i++) {
                trajectoryLeft[i] = new int[]{-1,0};
            }
            for(int i = 100 ; i < 150 ; i++) {
                trajectoryLeft[i] = new int[]{-1,1};
            }
            trajectoryUp = new int[100][2];
            for(int i = 0 ; i < 50 ; i++) {
                trajectoryUp[i] = new int[]{0,-1};
            }
            for(int i = 50 ; i < 100 ; i++) {
                trajectoryUp[i] = new int[]{0,1};
            }
        }

        @Override
        public boolean doMove() {

            //Get the trajectory depending on which direction we are jumping in
            if(trajectoryBeingUsed == null) {
                trajectoryBeingUsed = jSWControlsHandler.leftKeyPressed ? trajectoryLeft :
                        jSWControlsHandler.rightKeyPressed ? trajectoryRight : trajectoryUp;
            }

            //Only move the sprite the defined number of pixels for each movement before stopping
            int endIndex = startIndexInTrajectory + numPixelsPerMovement;
            //Step along the trajectory array making the increments to the sprite's location.  If something blocks
            //our path in one direction don't move the sprite in that direction but continue moving in the other
            //direction e.g. if something's in front of us but not above us then don't move forward but continue
            //any upwards movements.  A special case is made for when the sprite is moving downwards on the latter
            //half of the jump and something blocks its downward movement.  If this is the case then the movement is
            //stopped as if it's been totally blocked.  This is so as the sprite doesn't "slide" along platforms if it
            //has jumped upwards on to one.
            // If something blocks us in both directions then stop.
            boolean movementTotallyBlocked = false;
            boolean didntCollideWithAnythingGoingUpOrDown = true;
            for(int i = startIndexInTrajectory; i < endIndex && !movementTotallyBlocked; i++) {
                int[] movements = trajectoryBeingUsed[i];
                Point proposedNewLocation = new Point(jsw.getX() + movements[0], jsw.getY() + movements[1]);

                if(movements[1] != 0) {
                    didntCollideWithAnythingGoingUpOrDown = checkNotCollidedWhileMovingUpOrDown(movements[1] < 0
                            ?proposedNewLocation.y : proposedNewLocation.y +
                            jsw.getHeight(), movements[1] < 0);
                    if(didntCollideWithAnythingGoingUpOrDown) {
                        jsw.setLocation(jsw.getX(), proposedNewLocation.y);
                    }
                    else {
                        movementTotallyBlocked = movements[0] == 0 || movements[1] > 0;
                    }
                }

                if(movements[0] != 0 && !movementTotallyBlocked) {
                    if(checkNotCollidedWhileMovingAcross(movements[0] < 0 ? proposedNewLocation.x : proposedNewLocation.x +
                                    jsw.getWidth(), movements[0] > 0)) {
                        jsw.setLocation(proposedNewLocation.x, jsw.getY());
                    }
                    else {
                        movementTotallyBlocked = movements[1] == 0 || !didntCollideWithAnythingGoingUpOrDown;
                    }
                }

            }

            boolean finishedMovement = endIndex == trajectoryBeingUsed.length  || movementTotallyBlocked;

            if(finishedMovement) {
                //If we're finished jumping then reset these variables so we're ready to start a new jump
                startIndexInTrajectory = 0;
                trajectoryBeingUsed = null;
            }
            else {
                //If we haven't finished jumping yet then make sure we remember where we left off
                startIndexInTrajectory = endIndex;
            }
            return finishedMovement;
        }
    }

    //Moves a sprite left
    private class LeftMover extends Mover {


        private LeftMover() {
            super(MovementType.WALK_LEFT, Executors.newSingleThreadExecutor(), 1, 10);
        }

        @Override
        protected boolean doMove() {

            int startCheckpoint = jsw.getX() - 1;
            int endCheckPoint = jsw.getX() - numPixelsPerMovement;

            boolean notCollided = true;
            int i;
            for(i = startCheckpoint ; i >= endCheckPoint && notCollided ;i--) {
                notCollided = checkNotCollidedWhileMovingAcross(i, false);
                if(notCollided) {
                    //We can move
                    jsw.setLocation(i, jsw.getY());
                }
            }
            return true;
        }
    };

    //Moves a sprite right
    private class RightMover extends Mover {


        private RightMover() {
            super(MovementType.WALK_RIGHT, Executors.newSingleThreadExecutor(), 1, 10);
        }

        @Override
        protected boolean doMove() {
            int startCheckpoint = jsw.getX() + jsw.getWidth() + 1;
            int endCheckPoint = jsw.getX() + jsw.getWidth() + numPixelsPerMovement;

            boolean notCollided = true;
            int i;
            for(i = startCheckpoint ; i <= endCheckPoint && notCollided ;i++) {
                notCollided = checkNotCollidedWhileMovingAcross(i, true);
                if(notCollided) {
                    //We can move
                    jsw.setLocation(i - jsw.getWidth(), jsw.getY());
                }
            }
            return true;
        }
    };

    private void sleepAndShouldNotBeInterrupted(int numMillis) {
        try {
            Thread.sleep(numMillis);
        }
        catch(InterruptedException e) {
            System.out.println("Should not happen " + e);
            System.exit(1);
        }
    }

    private boolean checkNotCollidedWhileMovingAcross(int xLocation, boolean goingRight) {
        Rectangle cp = goingRight ? goingRightCaresAbout[xLocation] : goingLeftCaresAbout[xLocation];
        if(cp != null) {
            int topOfJsw = jsw.getY();
            int bottomOfJsw = topOfJsw + jsw.getHeight();
            int topOfCp = cp.y;
            int bottomOfCp = topOfCp + cp.height;
            return (bottomOfJsw < topOfCp || topOfJsw > bottomOfCp);
        }
        return true;
    }

    private boolean checkNotCollidedWhileMovingUpOrDown(int yLocation, boolean goingUp) {
        Rectangle cp = goingUp ? goingUpCaresAbout[yLocation] : goingDownCaresAbout[yLocation];
        if(cp != null) {
            int lhsOfJsw = jsw.getX();
            int rhsOfJsw = lhsOfJsw + jsw.getWidth();
            int lhsOfCp = cp.x;
            int rhsOfCp = lhsOfCp + cp.width;
            return (rhsOfJsw < lhsOfCp || lhsOfJsw > rhsOfCp);
        }
        return true;
    }
}
