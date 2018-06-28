package com.noomtech.platformscreen.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class GamePanel extends JPanel {


    private static final int SCREEN_SIZE = 1920;

    private final List<Rectangle> collisionAreas;
    private volatile boolean started = false;
    private final Rectangle jsw;
    private final JSWControlsHandler jSWControlsHandler;
    private final Rectangle[] goingRightCaresAbout = new Rectangle[SCREEN_SIZE];
    private final Rectangle[] goingLeftCaresAbout = new Rectangle[SCREEN_SIZE];
    private final Rectangle[] goingUpCaresAbout = new Rectangle[SCREEN_SIZE];
    private final Rectangle[] goingDownCaresAbout = new Rectangle[SCREEN_SIZE];


    GamePanel(List<Rectangle> collisionAreas) {
        //this.collisionAreas = new ArrayList<>();

        this.collisionAreas = collisionAreas;

        jsw = collisionAreas.remove(0);

//        jsw = new Rectangle(600,499,50,100);
//
//        Rectangle c1 = new Rectangle(150, 570, 100,30);
//        Rectangle c2 = new Rectangle(1200, 100, 200,500);
//        Rectangle c3 = new Rectangle(900, 380, 200,100);
//        Rectangle c4 = new Rectangle(0, 600, 1200,20);
//        Rectangle c5 = new Rectangle(0, 0, 20,600);
//        this.collisionAreas.addAll(Arrays.asList(new Rectangle[]{c1,c2,c3,c4,c5}));

        for(Rectangle r : this.collisionAreas) {
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

        g.setColor(Color.BLUE);
        g.fillRect(jsw.x, jsw.y, jsw.width, jsw.height);

        g.setColor(Color.RED);
        for(Rectangle r : collisionAreas) {
            g.drawRect(r.x, r.y, r.width, r.height);
        }
    }

    private class JSWControlsHandler implements KeyListener {

        private final char KEY_LEFT = 'o';
        private final char KEY_RIGHT = 'p';
        private final char KEY_JUMP = 'm';

        private final Mover fallMover;
        private final Mover leftMover;
        private final Mover rightMover;
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
                    if(!fallMover.isRunning() && !jumpMover.isRunning()) {
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

        private void actionFinished(MovementType movementType, boolean falling) {

            if(falling && movementType != MovementType.FALL) {
                fallMover.start();
            }
            else if(movementType == MovementType.JUMP) {
                if(leftKeyPressed) {
                    leftMover.start();
                }
                else if(rightKeyPressed) {
                    rightMover.start();
                }
            }
            else if(movementType == MovementType.FALL) {
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
            else if(movementType == MovementType.WALK_LEFT) {
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

    private enum MovementType {
        WALK_LEFT,
        WALK_RIGHT,
        JUMP,
        FALL;
    }

    private abstract class Mover implements Runnable {


        private final MovementType movementType;
        private final ExecutorService executorService;
        private volatile boolean running;
        protected final int numPixelsPerMovement;
        private final int numMillisBetweenMovements;
        private volatile boolean stopRequestReceived;
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

        protected void requestStop() {
            stopRequestReceived = true;
        }

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

        private MovementType getMovementType() {
            return movementType;
        }

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
            jSWControlsHandler.actionFinished(movementType, shouldFall);
        }

        protected abstract boolean doMove();

        private boolean shouldStartFalling() {
            if(movementType == MovementType.FALL) {
                return false;
            }
            return checkNotCollidedWhileMovingUpOrDown(jsw.y + jsw.height + 1,
                    false);
        }

        private void shutdown() {
            stopRequestReceived = true;
            executorService.shutdown();
        }
    }


    private class FallMover extends Mover {

        private FallMover() {
            super(MovementType.FALL, Executors.newSingleThreadExecutor(), 5, 50);
        }
        @Override
        protected boolean doMove() {
            int startCheckpoint = jsw.y + jsw.height + 1;
            int endCheckPoint = jsw.y + jsw.height + numPixelsPerMovement;
            boolean notCollided = true;
            int i;
            for(i = startCheckpoint ; i <= endCheckPoint && notCollided ;i++) {
                notCollided = checkNotCollidedWhileMovingUpOrDown(i, false);
                if(notCollided) {
                    //We can fall
                    jsw.setLocation(jsw.x, i-jsw.height);
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


    private class JumpMover extends Mover {


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

            if(trajectoryBeingUsed == null) {
                trajectoryBeingUsed = jSWControlsHandler.leftKeyPressed ? trajectoryLeft :
                        jSWControlsHandler.rightKeyPressed ? trajectoryRight : trajectoryUp;
            }

            int endIndex = startIndexInTrajectory + numPixelsPerMovement;
            boolean movementTotallyBlocked = false;
            boolean didntCollideWithAnythingGoingUpOrDown = true;
            for(int i = startIndexInTrajectory; i < endIndex && !movementTotallyBlocked; i++) {
                int[] movements = trajectoryBeingUsed[i];
                Point proposedNewLocation = new Point(jsw.x + movements[0], jsw.y + movements[1]);

                if(movements[1] != 0) {
                    didntCollideWithAnythingGoingUpOrDown = checkNotCollidedWhileMovingUpOrDown(movements[1] < 0
                            ?proposedNewLocation.y : proposedNewLocation.y +
                            jsw.height, movements[1] < 0);
                    if(didntCollideWithAnythingGoingUpOrDown) {
                        jsw.y = proposedNewLocation.y;
                    }
                    else {
                        movementTotallyBlocked = movements[0] == 0 || movements[1] > 0;
                    }
                }

                if(movements[0] != 0 && !movementTotallyBlocked) {
                    if(checkNotCollidedWhileMovingAcross(movements[0] < 0 ? proposedNewLocation.x : proposedNewLocation.x +
                                    jsw.width, movements[0] > 0)) {
                        jsw.x = proposedNewLocation.x;
                    }
                    else {
                        movementTotallyBlocked = movements[1] == 0 || !didntCollideWithAnythingGoingUpOrDown;
                    }
                }

            }

            boolean finishedMovement = endIndex == trajectoryBeingUsed.length  || movementTotallyBlocked;

            if(finishedMovement) {
                startIndexInTrajectory = 0;
                trajectoryBeingUsed = null;
            }
            else {
                startIndexInTrajectory = endIndex;
            }
            return finishedMovement;
        }
    }

    private class LeftMover extends Mover {


        private LeftMover() {
            super(MovementType.WALK_LEFT, Executors.newSingleThreadExecutor(), 1, 10);
        }

        @Override
        protected boolean doMove() {

            int startCheckpoint = jsw.x - 1;
            int endCheckPoint = jsw.x - numPixelsPerMovement;

            boolean notCollided = true;
            int i;
            for(i = startCheckpoint ; i >= endCheckPoint && notCollided ;i--) {
                notCollided = checkNotCollidedWhileMovingAcross(i, false);
                if(notCollided) {
                    //We can move
                    jsw.setLocation(i, jsw.y);
                }
            }
            return true;
        }
    };
    private class RightMover extends Mover {


        private RightMover() {
            super(MovementType.WALK_RIGHT, Executors.newSingleThreadExecutor(), 1, 10);
        }

        @Override
        protected boolean doMove() {
            int startCheckpoint = jsw.x + jsw.width + 1;
            int endCheckPoint = jsw.x + jsw.width + numPixelsPerMovement;

            boolean notCollided = true;
            int i;
            for(i = startCheckpoint ; i <= endCheckPoint && notCollided ;i++) {
                notCollided = checkNotCollidedWhileMovingAcross(i, true);
                if(notCollided) {
                    //We can move
                    jsw.setLocation(i - jsw.width, jsw.y);
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
            int topOfJsw = jsw.y;
            int bottomOfJsw = topOfJsw + jsw.height;
            int topOfCp = cp.y;
            int bottomOfCp = topOfCp + cp.height;
            return (bottomOfJsw < topOfCp || topOfJsw > bottomOfCp);
        }
        return true;
    }

    private boolean checkNotCollidedWhileMovingUpOrDown(int yLocation, boolean goingUp) {
        Rectangle cp = goingUp ? goingUpCaresAbout[yLocation] : goingDownCaresAbout[yLocation];
        if(cp != null) {
            int lhsOfJsw = jsw.x;
            int rhsOfJsw = lhsOfJsw + jsw.width;
            int lhsOfCp = cp.x;
            int rhsOfCp = lhsOfCp + cp.width;
            return (rhsOfJsw < lhsOfCp || lhsOfJsw > rhsOfCp);
        }
        return true;
    }
}
