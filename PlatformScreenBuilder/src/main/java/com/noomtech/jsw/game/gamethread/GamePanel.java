package com.noomtech.jsw.game.gamethread;

import com.noomtech.jsw.game.frame.GameFrame;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.Lethal;
import com.noomtech.jsw.game.gameobjects.objects.*;
import com.noomtech.jsw.game.movement.CollisionHandler;
import com.noomtech.jsw.game.movement.NastiesHandler;
import com.noomtech.jsw.game.utils.GameUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;


/**
 * This panel class is responsible for the painting and also acts as the controller
 */
public class GamePanel extends JPanel {


    //This receives notifications from this class
    private GameFrame parent;

    /**
     * This handles collisions between objects during the game
     * @see CollisionHandler
     */
    private final CollisionHandler COLLISION_HANDLER;

    //Does the painting.  This can be swapped around according to what we need
    private volatile Consumer<Graphics> painter;
    private final MessagePainter messagePainter = new MessagePainter();
    private final StandardPainter standardPainter = new StandardPainter(Color.WHITE);

    //Constants for movements
    private final int LEFT_RIGHT_NUM_PIXELS_PER_MOVEMENT = 1;
    private final int JUMP_NUM_PIXELS_PER_MOVEMENT = 1;
    private final int FALL_NUM_PIXELS_PER_MOVEMENT = 5;
    //The speed of the movements is scaled based on the existing screen size in relation to a 1920 x 1080 size e.g.
    //if this was being run on a much smaller screen of half the resolution (960 x 540) then these delays would have to
    //be twice as long as the distance for the player to walk across the screen for example would be half as much.
    private final int LEFT_RIGHT_NUM_MILLIS_BETWEEM_MOVEMENTS = GameUtils.getScaledMsToScreenWidthValue(new BigDecimal("8"));
    private final int JUMP_NUM_MILLIS_BETWEEN_MOVEMENTS = GameUtils.getScaledMsToScreenWidthValue(new BigDecimal("8"));
    private final int FALL_NUM_MILLIS_BETWEEN_MOVEMENTS = GameUtils.getScaledMsToScreenHeightValue(new BigDecimal("50"));

    //Used for symchronization around the starting and stopping of the game
    private final Object START_STOP_MUTEX = new Object();

    //The platforms.
    private final List<Platform> platforms;
    //The nasties
    private final List<Nasty> nasties;
    //The lethal objects (static objects that kill the player when touched)
    private final List<StaticLethalObject> staticLethalObjects;
    //Objects that the player needs to complete the game
    private final List<FinishingObject> finishingObjects;
    //True if the game is running.  Used to keep the game loop going and to stop it
    private volatile boolean started = false;
    //The player sprite (jsw = "Jet Set Willy")
    private final JSW jsw;
    //Handles the core operations of the game
    private GameHandler gameHandler;
    //Coordinates the keyboard input and moves the player sprite in the appropriate direction
    private JSWControlsHandler jSWControlsHandler;
    //Handles the movements of the nasties
    private NastiesHandler nastiesHandler;
    //True if the thread that moves the nasties should stop e.g. if the player has hit a lethal object and the "dying"
    //routine is run
    private volatile boolean stopNastiesThread;
    //Runs the nasties handler
    private Thread nastiesThread;


    public GamePanel(GameFrame parent,
            List<GameObject> gameObjects) {

        this.parent = parent;
        this.platforms = new ArrayList<>();
        this.nasties = new ArrayList<>();
        this.staticLethalObjects = new ArrayList<>();
        this.finishingObjects = new ArrayList<>();
        JSW tempJSW = null;
        for(GameObject gameObject : gameObjects) {
            Class<?> gameObjectClass = gameObject.getClass();
            if(gameObjectClass == Nasty.class) {
                nasties.add((Nasty)gameObject);
            }
            else if(gameObjectClass == JSW.class) {
                if(tempJSW != null) {
                    throw new IllegalArgumentException("Only allowed one player!");
                }
                tempJSW = (JSW)gameObject;
            }
            else if(gameObjectClass == StaticLethalObject.class) {
                staticLethalObjects.add((StaticLethalObject)gameObject);
            }
            else if(gameObjectClass == Platform.class) {
                platforms.add((Platform)gameObject);
            }
            else if(gameObjectClass == FinishingObject.class) {
                finishingObjects.add((FinishingObject)gameObject);
            }
            else {
                throw new IllegalArgumentException("Unknown game object class " + gameObjectClass);
            }
        }

        if(tempJSW == null) {
            throw new IllegalStateException("Must have a JSW!");
        }
        jsw = tempJSW;

        List<GameObject> staticObjects = new ArrayList<>();
        staticObjects.addAll(platforms);
        staticObjects.addAll(staticLethalObjects);
        staticObjects.addAll(finishingObjects);

        COLLISION_HANDLER = new CollisionHandler(staticObjects, 4000, jsw);

        setFocusable(true);

        start();
    }

    //Starts the game panel
    private void start() {

        synchronized (START_STOP_MUTEX) {
            if(started) {
                throw new IllegalStateException("Application already started");
            }

            started = true;
            startMovementComponents();
            painter = standardPainter;
            gameHandler = new GameHandler(this, 25);
            Thread t1 = new Thread(gameHandler);
            t1.setName("Game Handler");
            t1.start();
        }
    }

    //Stops the game panel
    private void stop() throws InterruptedException {
        synchronized (START_STOP_MUTEX) {
            if(!started) {
                throw new IllegalArgumentException("Application has not been started");
            }
            started = false;
            stopMovementComponents();
        }
    }

    //Returns false if the game is no longer running
    public boolean isRunning() {
        return started;
    }

    //Returns false if the nasties should stop moving
    public boolean shouldRunNastiesThread() {
        return started && !stopNastiesThread;
    }


    private void startMovementComponents() {
        jSWControlsHandler = new JSWControlsHandler(new LeftMover(), new RightMover(), new JumpMover());
        nastiesHandler = new NastiesHandler(this, nasties, COLLISION_HANDLER);
        addKeyListener(jSWControlsHandler);
        stopNastiesThread = false;
        nastiesThread = new Thread(nastiesHandler);
        nastiesThread.setName("Nasties Handler");
        nastiesThread.start();
    }

    private void stopMovementComponents() throws InterruptedException {
        removeKeyListener(jSWControlsHandler);
        stopNastiesThread = true;
        jSWControlsHandler.shutdown();
        nastiesThread.join();
    }

    private void setGameObjectsToStartingState() {
        for (Nasty nasty : nasties) {
            nasty.setToStartingState();
        }
        jsw.setToStartingState();
    }

    //Paints the game
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        painter.accept(g);
    }

    //Callback for when the the player has hit a lethal object
    public void playerHitLethalObject(GameObject lethalThatWasHit) {

        messagePainter.backgroundColor = Color.RED;
        messagePainter.message = null;
        painter = messagePainter;

        try {stopMovementComponents();} catch(InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        GameUtils.sleepAndCatchInterrupt(300);
        messagePainter.backgroundColor = Color.ORANGE;
        GameUtils.sleepAndCatchInterrupt(300);
        messagePainter.backgroundColor = Color.WHITE;

        setGameObjectsToStartingState();
        painter = standardPainter;
        startMovementComponents();
    }

    //Callback for when the the player has hit a finishing object
    public void playerHitFinishingObject(GameObject finishingObject) {

        try {
            messagePainter.backgroundColor = Color.BLUE;
            messagePainter.message = "WELL DONE!!";
            painter = messagePainter;

            GameUtils.sleepAndCatchInterrupt(500);
            messagePainter.backgroundColor = Color.GREEN;
            GameUtils.sleepAndCatchInterrupt(500);
            messagePainter.backgroundColor = Color.WHITE;

            stop();

            parent.onLevelFinished();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    //Handles the keyboard input for the player sprite and is also responsible for reacting to collisions involving the
    //player sprite e.g. when the player sprite hits a platform or a lethal object
    private class JSWControlsHandler implements KeyListener {

        private final char KEY_LEFT = 'o';
        private final char KEY_RIGHT = 'p';
        private final char KEY_JUMP = 'm';

        //Each of these instances handle the movement of the sprite in a particular direction.  They are Runnable instances
        //that are run on separate threads that update the location of the player sprite in order to move it.
        //The sprite is then repainted by the repainting loop meaning that the player sprite is seen to move around the screen.
        private final JSWMover leftMover;
        private final JSWMover rightMover;
        //Handles the movement for when the player sprite is falling
        private final JSWMover fallMover;
        private final JSWMover jumpMover;

        private volatile boolean leftKeyPressed;
        private volatile boolean rightKeyPressed;
        private volatile boolean jumpKeyPressed;

        private JSWControlsHandler(JSWMover leftMover, JSWMover rightMover, JumpMover jumpMover) {
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

            switch (e.getKeyChar()) {
                case (KEY_LEFT): {
                    leftKeyPressed = true;
                    //Can't move left if we're already moving left or already moving in any other direction
                    if (!leftMover.isRunning() && !rightMover.isRunning() && !jumpMover.isRunning() && !fallMover.isRunning()) {
                        leftMover.start();
                    }

                    break;
                }
                case (KEY_RIGHT): {
                    rightKeyPressed = true;
                    if (!leftMover.isRunning() && !rightMover.isRunning() && !jumpMover.isRunning() && !fallMover.isRunning()) {
                        rightMover.start();
                    }
                    break;
                }
                case (KEY_JUMP): {
                    jumpKeyPressed = true;
                    //Can't jump if we're falling or already jumping
                    if (!fallMover.isRunning() && !jumpMover.isRunning()) {
                        //If we're already moving left or right we will jump in that direction but we must stop the
                        //movement first otherwise it will interfere with the jumping movment
                        if (leftMover.isRunning()) {
                            leftMover.requestStop(true);
                        } else if (rightMover.isRunning()) {
                            rightMover.requestStop(true);
                        }
                        jumpMover.start();
                    }
                    break;
                }
                default: {
                }
            }

        }

        //Called to notify the controller that the jsw has hit something while falling.  Returns true if the movement should
        //be stopped.
        private boolean hitWhileFalling(GameObject hit) {
            if(hit instanceof Lethal || hit instanceof FinishingObject) {
                processShowstopper(hit);
                return true;
            }

            if(hit instanceof Platform) {
                movementFinished(PlayerMovementType.FALL);
                return true;
            }

            return false;
        }

        //Called to notify the controller that the jsw has hit something while walking
        private boolean hitWhileWalking(GameObject hit, PlayerMovementType playerMovementType) {
            if(hit instanceof Lethal || hit instanceof FinishingObject) {
                processShowstopper(hit);
                return true;
            }

            if(hit instanceof Platform) {
                movementFinished(playerMovementType);
                return true;
            }

            return false;
        }

        //Called to notify the controller that the jsw has hit something while jumping.  As jumping involves moving in
        //both the X and Y directions this callback needs to behave differently depending on which direction the
        //player was moving at the time.
        private JumpMovementResult hitWhileJumping(GameObject hit, int[] movements, boolean movingInYDirection) {
            if(hit instanceof Lethal || hit instanceof FinishingObject) {
                processShowstopper(hit);
                return JumpMovementResult.STOP_COMPLETELY;
            }

            JumpMovementResult result = JumpMovementResult.MOVE;
            if (hit instanceof Platform) {
                //If the player has hit a platform then it could either stop them moving in this direction or stop
                //them entirely
                result = JumpMovementResult.DONT_MOVE_IN_THIS_DIRECTION;
                if(movingInYDirection) {
                    //Stop the jump completely if the JSW:
                    //1: is only going up or down
                    //2: is moving across and down (we don't want them sliding along the ground)
                    if(movements[0] == 0 || movements[1] > 0) {
                        result = JumpMovementResult.STOP_COMPLETELY;
                    }
                }
                else {
                    //Stop the jump if the jsw is only moving left or right and has hit something
                    if(movements[1] == 0) {
                        result = JumpMovementResult.STOP_COMPLETELY;
                    }
                }
            }

            if(result == JumpMovementResult.STOP_COMPLETELY) {
                //The jump is stopped so check if the jsw needs to fall
                if (!doFallCheck()) {
                    //The jump got stopped and the jsw does not need to fall, so the movement's finished
                    movementFinished(PlayerMovementType.JUMP);
                }
            }

            return result;
        }

        //Called when a jump movement completed without being stopped prematurely e.g. by the player hitting a platform
        private void jumpFinishedWithoutBeingStopped() {
            if(!doFallCheck()) {
                jSWControlsHandler.movementFinished(PlayerMovementType.JUMP);
            }
        }

        //Called when a left./right movement completed without being stopped prematurely e.g. by the player hitting a platform
        private void leftRightMoveFinishedWithoutBeingStopped(PlayerMovementType playerMovementType) {
            if(!doFallCheck()) {
                jSWControlsHandler.movementFinished(playerMovementType);
            }
        }

        //Returns true if the player should fall e.g. if they have nothing underneath them
        private boolean doFallCheck() {
            GameObject standingOn = COLLISION_HANDLER.checkIfTouchingAnythingGoingDown(jsw);
            if(standingOn == null || !(standingOn instanceof Platform)) {
                fallMover.start();
                return true;
            }
            return false;
        }

        //Called when a movement has finished.
        private void movementFinished(PlayerMovementType movementType) {

            if (movementType == PlayerMovementType.JUMP) {
                //if we've just finished a jump and the user has been holding down the left or right key then start the
                //player sprite moving in that direction
                if (leftKeyPressed) {
                    leftMover.start();
                } else if (rightKeyPressed) {
                    rightMover.start();
                }
            } else if (movementType == PlayerMovementType.FALL || movementType == PlayerMovementType.MOVED_BACK_TO_START) {
                //if we've just finished falling or dying and the user has been holding down the left, right or jump key then start the
                //player sprite moving accordingly
                if (jumpKeyPressed) {
                    jumpMover.start();
                } else if (leftKeyPressed) {
                    leftMover.start();
                } else if (rightKeyPressed) {
                    rightMover.start();
                }
            }
            //if we've just finished moving left or right and the user has been holding down the key for moving
            //in the opposite direction then start moving in that direction
            else if (movementType == PlayerMovementType.WALK_LEFT) {
                if (rightKeyPressed && jumpKeyPressed) {
                    jumpMover.start();
                } else if (rightKeyPressed) {
                    rightMover.start();
                }
            } else if (movementType == PlayerMovementType.WALK_RIGHT) {
                if (leftKeyPressed && jumpKeyPressed) {
                    jumpMover.start();
                } else if (leftKeyPressed) {
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
                    leftMover.requestStop(false);
                    break;
                }
                case (KEY_RIGHT): {
                    rightKeyPressed = false;
                    rightMover.requestStop(false);
                    break;
                }
                case (KEY_JUMP): {
                    jumpKeyPressed = false;
                    jumpMover.requestStop(false);
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

    //Perform the relevant actions for when the player has hit something e.g. a finishing object
    public void processShowstopper(GameObject showStopper) {
        if(showStopper instanceof Lethal) {
            removeKeyListener(jSWControlsHandler);
            Executors.newSingleThreadExecutor().submit(()->{playerHitLethalObject(showStopper);});
        }
        else if(showStopper instanceof FinishingObject){
            removeKeyListener(jSWControlsHandler);
            Executors.newSingleThreadExecutor().submit(()->{playerHitFinishingObject(showStopper);});
        }
        else {
            throw new IllegalArgumentException("Unknown type of showstopper " + showStopper);
        }
    }

    //Runnable class that updates the location of the JSW.  Used by the movement classes.
    private abstract class JSWMover implements Runnable {

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

        private JSWMover(ExecutorService executorService,
                         int numPixelsPerMovement, int numMillisBetweenMovements) {
            this.executorService = executorService;
            this.numPixelsBetweenSleeps = numPixelsPerMovement;
            this.numMillisBetweenMovements = numMillisBetweenMovements;
        }


        private void start() {
            running = true;
            future = executorService.submit(this);
        }

        /**
         * Submits a request to stop the movement gracefully.  If the "wait" parameter is true then this blocks until the thread for the routine has completed.
         * Note that the {@link JSWControlsHandler} will still be notified when this movement is finished either way.
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

        private boolean isRunning() {
            return running;
        }

        private void shutdown() {
            stopRequestReceived = true;
            executorService.shutdown();
        }
    }

    //Handles the movement of a sprite when its falling
    private class FallMover extends JSWMover {

        private FallMover() {
            super(Executors.newSingleThreadExecutor(),
                    FALL_NUM_PIXELS_PER_MOVEMENT,
                    FALL_NUM_MILLIS_BETWEEN_MOVEMENTS);
        }

        @Override
        public void run() {

            stopRequestReceived = false;
            boolean stopMoving = false;
            int numPixelsFallen = 0;
            while (!stopMoving) {
                GameObject touching = COLLISION_HANDLER.checkIfTouchingAnythingGoingDown(jsw);
                if(touching != null) {
                    stopMoving = jSWControlsHandler.hitWhileFalling(touching);
                }
                if(!stopMoving) {
                    jsw.setLocation(jsw.getImageArea().x, jsw.getImageArea().y + 1);
                    numPixelsFallen++;
                    jsw.onMove();
                }

                stopMoving = stopMoving || stopRequestReceived;
                if (!stopMoving && (numPixelsFallen % numPixelsBetweenSleeps == 0)) {
                    GameUtils.sleepAndCatchInterrupt(numMillisBetweenMovements);
                }
            }
            running = false;
        }
    }

    private enum JumpMovementResult {
        STOP_COMPLETELY,
        DONT_MOVE_IN_THIS_DIRECTION,
        MOVE;
    }

    //Handles the movement of the sprite when its jumping
    private class JumpMover extends JSWMover {

        //Defines the increments that need to be made to the sprite's location in order to get them to jump left, right
        //or up.

        private int[][] trajectoryRight;
        private int[][] trajectoryLeft;
        private int[][] trajectoryUp;


        private JumpMover() {

            super(Executors.newSingleThreadExecutor(), JUMP_NUM_PIXELS_PER_MOVEMENT,
                    JUMP_NUM_MILLIS_BETWEEN_MOVEMENTS);

            int playerWidth = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(jsw.getWidth()));
            int playerHeight = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(jsw.getHeight()));

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
            int [][]trajectoryBeingUsed = jSWControlsHandler.leftKeyPressed ? trajectoryLeft :
                    jSWControlsHandler.rightKeyPressed ? trajectoryRight : trajectoryUp;


            //Step along the trajectory array making the increments to the sprite's location.  If something blocks
            //our path then refer to the controller.
            int i;
            for (i = 0; i < trajectoryBeingUsed.length && !stoppedBeforeFinishing; i++) {
                int[] movements = trajectoryBeingUsed[i];
                Point proposedNewLocation  = new Point(jsw.getX() + movements[0], jsw.getY() + movements[1]);

                if (movements[1] != 0) {
                    //JSW needs to move up or down
                    GameObject touching = movements[1] < 0 ? COLLISION_HANDLER.checkIfTouchingAnythingGoingUp(jsw) : COLLISION_HANDLER.checkIfTouchingAnythingGoingDown(jsw);
                    JumpMovementResult jumpMovementResult = JumpMovementResult.MOVE;
                    if(touching != null) {
                        //If the jsw has hit something while jumping then it might stop the entire movement or just movement
                        //in this direction
                        jumpMovementResult = jSWControlsHandler.hitWhileJumping(touching, movements, true);
                        stoppedBeforeFinishing = jumpMovementResult == JumpMovementResult.STOP_COMPLETELY;
                    }

                    if(jumpMovementResult == JumpMovementResult.MOVE) {
                        updateLocationY(proposedNewLocation);
                    }
                }

                if (!stoppedBeforeFinishing && movements[0] != 0) {
                    //JSW needs to moves left or right
                    GameObject touching = movements[0] < 0 ? COLLISION_HANDLER.checkIfTouchingAnythingGoingLeft(jsw) : COLLISION_HANDLER.checkIfTouchingAnythingGoingRight(jsw);
                    JumpMovementResult jumpMovementResult = JumpMovementResult.MOVE;
                    if(touching != null) {
                        jumpMovementResult = jSWControlsHandler.hitWhileJumping(touching, movements, false);
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
                jSWControlsHandler.jumpFinishedWithoutBeingStopped();
            }

            running = false;
        }

        private void updateLocationY(Point proposedLocation) {
            jsw.setLocation(jsw.getX(), proposedLocation.y);
            jsw.onMove();
        }

        private void updateLocationX(Point proposedLocation) {
            jsw.setLocation(proposedLocation.x, jsw.getY());
            jsw.onMove();
        }
    }

    //Moves a sprite left
    private class LeftMover extends JSWMover {


        private LeftMover() {
            super(Executors.newSingleThreadExecutor(), LEFT_RIGHT_NUM_PIXELS_PER_MOVEMENT,
                LEFT_RIGHT_NUM_MILLIS_BETWEEM_MOVEMENTS);
        }

        @Override
        public void run() {
            boolean hasBeenStopped = false;
            stopRequestReceived = false;
            int numPixelsMoved = 0;
            while(!hasBeenStopped) {
                GameObject touching = COLLISION_HANDLER.checkIfTouchingAnythingGoingLeft(jsw);
                if (touching == null) {
                    //Not touching anything so jsw can move
                    numPixelsMoved = move(numPixelsMoved);
                    //Now that it's moved dheck if JSW should fall
                    hasBeenStopped = jSWControlsHandler.doFallCheck();
                }
                else {
                    //Jsw is touching something so see if this is something that will stop up
                    hasBeenStopped = jSWControlsHandler.hitWhileWalking(touching, PlayerMovementType.WALK_LEFT);
                    if(!hasBeenStopped) {
                        numPixelsMoved = move(numPixelsMoved);
                        //Now that it's moved dheck if JSW should fall
                        hasBeenStopped = jSWControlsHandler.doFallCheck();
                    }
                }

                if(!hasBeenStopped) {
                    //Movement has finished without being stopped prematurely
                    if(stopRequestReceived) {
                        hasBeenStopped = true;
                        jSWControlsHandler.leftRightMoveFinishedWithoutBeingStopped(PlayerMovementType.WALK_LEFT);
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
            jsw.setLocation(jsw.getX() - 1, jsw.getY());
            jsw.onMove();
            return ++numberPixelsMoved;
        }
    };

    //Moves a sprite right.  Identical to the left mover except this goes in the other direction
    private class RightMover extends JSWMover {


        private RightMover() {
            super(Executors.newSingleThreadExecutor(),
                    LEFT_RIGHT_NUM_PIXELS_PER_MOVEMENT,
                    LEFT_RIGHT_NUM_MILLIS_BETWEEM_MOVEMENTS);
        }

        @Override
        public void run() {
            boolean hasBeenStopped = false;
            stopRequestReceived = false;
            int numPixelsMoved = 0;
            while(!hasBeenStopped) {
                GameObject touching = COLLISION_HANDLER.checkIfTouchingAnythingGoingRight(jsw);
                if (touching == null) {
                    numPixelsMoved = move(numPixelsMoved);
                    hasBeenStopped = jSWControlsHandler.doFallCheck();
                }
                else {
                    hasBeenStopped = jSWControlsHandler.hitWhileWalking(touching, PlayerMovementType.WALK_RIGHT);
                    if(!hasBeenStopped) {
                        numPixelsMoved = move(numPixelsMoved);
                        hasBeenStopped = jSWControlsHandler.doFallCheck();
                    }
                }

                if(!hasBeenStopped) {
                    if(stopRequestReceived) {
                        hasBeenStopped = true;
                        jSWControlsHandler.leftRightMoveFinishedWithoutBeingStopped(PlayerMovementType.WALK_RIGHT);
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
            jsw.setLocation(jsw.getX() + 1, jsw.getY());
            jsw.onMove();
            return ++numberPixelsMoved;
        }
    };

    private enum PlayerMovementType {
        WALK_LEFT,
        WALK_RIGHT,
        JUMP,
        FALL,
        //e.g. For when the player dies
        MOVED_BACK_TO_START;
    }

    //Paints the game objects in their respective location etc..  Does the painting most of the time.
    private class StandardPainter implements Consumer<Graphics> {

        //The colour of the background in the game
        private Color backgroundColor;

        public StandardPainter(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public void accept(Graphics g) {

            g.setColor(backgroundColor);
            Dimension screenSize = getSize();
            //@todo - if there is an issue with slowness then it could be that the static objects like the platforms are being
            //repainted every time when they don't need to be.  See the GameObject.doPainting and the static game object's paintObject methods.  There is
            //commented out code in these methods that only paints the object once.  This didn't work when it was implemented because the
            //object would get painted once and then, when the next iteration of the game loop was called and this code here was called,
            //they would get wiped out by the rectangle drawn below and then never painted again.  So if there is slowness then this code could be reinstated and
            //this rectangle should never be drawn.  Instead the moving objects only could be rubbed out and repainted.
            g.fillRect(0, 0, screenSize.width, screenSize.height);

            for (Platform platform : platforms) {
                platform.doPainting(g);
            }

            for (StaticLethalObject staticLethalObject : staticLethalObjects) {
                staticLethalObject.doPainting(g);
            }

            for (FinishingObject finishingObject : finishingObjects) {
                finishingObject.doPainting(g);
            }

            jsw.doPainting(g);

            for (Nasty nasty : nasties) {
                nasty.doPainting(g);
            }
        }
    }

    //Used to display a particular background and an optional message
    private class MessagePainter implements Consumer<Graphics> {
        //Not null if the screen should simply display a message e.g. if the player has finished the game
        private volatile String message;
        private volatile Color backgroundColor;
        public void accept(Graphics g) {
            g.setColor(backgroundColor);
            Dimension screenSize = getSize();
            g.fillRect(0, 0, screenSize.width, screenSize.height);
            if(message != null) {
                g.setColor(Color.BLACK);
                g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 10f));
                g.drawString(message, (int) (screenSize.width * 0.3), (int) (screenSize.height * 0.5));
            }
        }
    }
}
