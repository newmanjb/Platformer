package com.noomtech.jsw.game.gamethread;

import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.Lethal;
import com.noomtech.jsw.game.gameobjects.objects.*;
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


/**
 * This panel class is responsible for the painting and also acts as the controller.
 *
 * The game itself is a simple Manic Miner/Jet Set Willy type thing i.e. with controls of "left", "right" and "jump" in a screen full of
 * platforms.
 *
 * There is quite a lot of code around obtaining a high level of precision on the controls, as this is important in a platform game.
 *
 * Each object has a "collision area" which is represented as a {@link Rectangle}s instance that encloses the actual graphic for the
 * object.  These collision areas represent the boundaries and are used to determine when a collision between two objects e.g. the player and a
 * platform, has occurred.
 *
 * @author Joshua Newman
 */
public class GamePanel extends JPanel {


    private static final int SCREEN_SIZE = 4000;

    //Constants for movements
    private final int LEFT_RIGHT_NUM_PIXELS_PER_MOVEMENT = 1;
    private final int JUMP_NUM_PIXELS_PER_MOVEMENT = 1;
    private final int FALL_NUM_PIXELS_PER_MOVEMENT = 5;
    //The speed of the movements is scaled based on the existing screen size in relation to a 1920 x 1080 size e.g.
    //if this was being run on a much smaller screen of half the resolution (960 x 540) then these delays would have to
    //be twice as long as the distance for the player to walk across the screen for example would be half as much.
    private final int LEFT_RIGHT_NUM_MILLIS_BETWEEM_MOVEMENTS = GameUtils.getScaledMsToScreenWidthValue(BigDecimal.TEN);
    private final int JUMP_NUM_MILLIS_BETWEEN_MOVEMENTS = GameUtils.getScaledMsToScreenWidthValue(BigDecimal.TEN);
    private final int FALL_NUM_MILLIS_BETWEEN_MOVEMENTS = GameUtils.getScaledMsToScreenHeightValue(new BigDecimal("50"));
    private final int NASTY_NUM_MILLIS_BETWEEN_MOVEMENTS = GameUtils.getScaledMsToScreenHeightValue(BigDecimal.TEN);


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
    private final JSWControlsHandler jSWControlsHandler;
    //Handles the movements of the nasties
    private final NastiesHandler nastiesHandler;
    //True if the thread that moves the nasties should stop e.g. if the player has hit a lethal object and the "dying"
    //routine is run
    private volatile boolean stopNastiesThread;
    //Runs the nasties handler
    private Thread nastiesThread;
    //The colour of the background in the game
    private Color backgroundColor;
    //Not null if the screen should simply display a message e.g. if the player has finished the game
    private volatile String message;


    //Each of thes arrays below is as long as the screen size (which is currently square).  The relevant boundary of each static object that
    //can affect the player e.g. platform or lethal object, in relation to the direction that the player sprite happens to be moving
    //forms its index in the appropriate array.
    //These arrays are then used by the functionality that controls the player's movement in order to quickly detect
    //whether they have collided with something e.g.

    //if there is a platform with a collision area (rectangle) at x = 50, y=70 that is 20 wide and 10 high then the left boundary (x=50) is the only thing
    //that the player will be able to collide with when moving right, so this rectangle will be placed at index 50 in the
    //array that is checked when the play is moving right.  The lower boundary at y=80 (70 + 10) will be what the functionality
    //cares about when the player sprite is moving up, so the rectangle will be placed at index 80 in the list that is checked when
    //the spring is moving up the screen.

    private final GameObject[][] goingRightCaresAbout = new GameObject[SCREEN_SIZE][];
    private final GameObject[][] goingLeftCaresAbout = new GameObject[SCREEN_SIZE][];
    private final GameObject[][] goingUpCaresAbout = new GameObject[SCREEN_SIZE][];
    private final GameObject[][] goingDownCaresAbout = new GameObject[SCREEN_SIZE][];


    public GamePanel(
            List<GameObject> gameObjects) {

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

        //Some hard-coded data for when we can't access cassandra and want to do some testing

//        jsw = new Rectangle(600,499,50,100);
//
//        Rectangle c1 = new Rectangle(150, 570, 100,30);
//        Rectangle c2 = new Rectangle(1200, 100, 200,500);
//        Rectangle c3 = new Rectangle(900, 380, 200,100);
//        Rectangle c4 = new Rectangle(0, 600, 1200,20);
//        Rectangle c5 = new Rectangle(0, 0, 20,600);
//        this.platforms.addAll(Arrays.asList(new Rectangle[]{c1,c2,c3,c4,c5}));

        List<GameObject> staticObjects = new ArrayList<>();
        staticObjects.addAll(platforms);
        staticObjects.addAll(staticLethalObjects);
        staticObjects.addAll(finishingObjects);
        for(GameObject g : staticObjects) {
            Rectangle r = g.getCollisionArea();
            Point p = g.getLocation();
            goingLeftCaresAbout[p.x + r.width] = buildNewGameObjects(goingLeftCaresAbout[p.x + r.width], g);
            goingRightCaresAbout[p.x] = buildNewGameObjects(goingRightCaresAbout[p.x], g);;
            goingUpCaresAbout[p.y + r.height] = buildNewGameObjects(goingUpCaresAbout[p.y + r.height], g);;
            goingDownCaresAbout[p.y] = buildNewGameObjects(goingDownCaresAbout[p.y], g);;
        }

        jSWControlsHandler = new JSWControlsHandler(new LeftMover(), new RightMover(), new JumpMover());
        nastiesHandler = new NastiesHandler(this, nasties, goingDownCaresAbout, goingUpCaresAbout, NASTY_NUM_MILLIS_BETWEEN_MOVEMENTS);
        gameHandler = new GameHandler(this, 25);

        this.addKeyListener(jSWControlsHandler);

        setFocusable(true);
    }

    //Returns false if the game is no longer running
    public boolean isRunning() {
        return started;
    }

    //Returns false if the nasties should stop moving
    public boolean shouldRunNastiesThread() {
        return started && !stopNastiesThread;
    }

    public void start() {
        started = true;
        backgroundColor = Color.WHITE;
        nastiesThread = new Thread(nastiesHandler);
        nastiesThread.setName("Nasties Handler");
        nastiesThread.start();
        Thread t1 = new Thread(gameHandler);
        t1.setName("Game Handler");
        t1.start();
    }

    public void stop() {
        started = false;
        jSWControlsHandler.shutdown();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
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

        if(message != null) {
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 10f));
            g.drawString(message, (int)(screenSize.width * 0.3), (int)(screenSize.height * 0.5));
        }
    }

    public Rectangle getPlayerCollisionArea() {
        return jsw.getCollisionArea();
    }

    //Callback for when the the player has hit a lethal object
    public void playerHitLethalObject(Lethal nastyThatWasHit) {
        new Thread(() ->
            {
                stopNastiesThread = true;
                jSWControlsHandler.freezeJSW();

                backgroundColor = Color.RED;
                GameUtils.sleepAndCatchInterrupt(300);
                backgroundColor = Color.ORANGE;
                GameUtils.sleepAndCatchInterrupt(300);
                backgroundColor = Color.WHITE;

                nastiesThread = new Thread(nastiesHandler);
                nastiesThread.setName("Nasties Handler");
                for (Nasty nasty : nasties) {
                    nasty.setToStartingState();
                }
                jsw.setToStartingState();

                stopNastiesThread = false;
                jSWControlsHandler.unfreezeJSW();
                nastiesThread.start();
            }).start();
    }

    //Callback for when the the player has hit a finishing object
    public void playerHitFinishingObject(FinishingObject finishingObject) {
        new Thread(() ->
        {
            stopNastiesThread = true;
            jSWControlsHandler.freezeJSW();

            message = "WELL DONE!!";
            backgroundColor = Color.BLUE;
            GameUtils.sleepAndCatchInterrupt(500);
            backgroundColor = Color.GREEN;
            GameUtils.sleepAndCatchInterrupt(500);
            backgroundColor = Color.WHITE;
            message = null;

            nastiesThread = new Thread(nastiesHandler);
            nastiesThread.setName("Nasties Handler");
            for (Nasty nasty : nasties) {
                nasty.setToStartingState();
            }
            jsw.setToStartingState();

            stopNastiesThread = false;
            jSWControlsHandler.unfreezeJSW();
            nastiesThread.start();
        }).start();
    }

    private static GameObject[] buildNewGameObjects(GameObject[] existingGameObjects, GameObject gameObjectToAdd) {
        GameObject[] newGameObjects;
        if(existingGameObjects == null) {
            newGameObjects = new GameObject[1];
            newGameObjects[0] = gameObjectToAdd;
        }
        else {
            newGameObjects = new GameObject[existingGameObjects.length+1];
            System.arraycopy(existingGameObjects, 0, newGameObjects, 0, existingGameObjects.length);
            newGameObjects[existingGameObjects.length] = gameObjectToAdd;
        }
        return newGameObjects;
    }

    private class JSWControlsHandler implements KeyListener {

        private final char KEY_LEFT = 'o';
        private final char KEY_RIGHT = 'p';
        private final char KEY_JUMP = 'm';

        //Each of these instances handle the movement of the sprite in a particular direction.  They are Runnable instances
        //that are run on separate threads that update the location of the player sprite in order to move it in particular directions.
        //The sprite is then repainted by the repainting loop meaning that the player sprite is seen to move around the screen.
        private final JSWMover leftMover;
        private final JSWMover rightMover;
        //Handles the movement for when the player sprite is falling
        private final JSWMover fallMover;
        private final JSWMover jumpMover;

        private volatile boolean leftKeyPressed;
        private volatile boolean rightKeyPressed;
        private volatile boolean jumpKeyPressed;

        //True if we should not initiate any new movement
        private volatile boolean freeze;

        private JSWControlsHandler(JSWMover leftMover, JSWMover rightMover, JumpMover jumpMover) {
            this.leftMover = leftMover;
            this.rightMover = rightMover;
            this.jumpMover = jumpMover;
            this.fallMover = new FallMover();
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        private void unfreezeJSW() {
            freeze = false;
            //Make sure that the player responds to any keys that have been pressed throughout the freeze e.g.
            //if the freeze was because the player had walked into a nasty and the walking key is still being held then
            //make the player walk
            movementFinished(PlayerMovementType.MOVED_BACK_TO_START, false);
        }

        private void freezeJSW() {
            freeze = true;
            leftMover.stopImmediately();
            rightMover.stopImmediately();
            jumpMover.stopImmediately();
            fallMover.stopImmediately();
        }

        @Override
        public void keyPressed(KeyEvent e) {

            switch (e.getKeyChar()) {
                case (KEY_LEFT): {
                    leftKeyPressed = true;
                    if(!freeze) {
                        //Can't move left if we're already moving left or already moving in any other direction
                        if (!leftMover.isRunning() && !rightMover.isRunning() && !jumpMover.isRunning() && !fallMover.isRunning()) {
                            leftMover.start();
                        }
                    }
                    break;
                }
                case (KEY_RIGHT): {
                    rightKeyPressed = true;
                    if(!freeze) {
                        if (!leftMover.isRunning() && !rightMover.isRunning() && !jumpMover.isRunning() && !fallMover.isRunning()) {
                            rightMover.start();
                        }
                    }
                    break;
                }
                case (KEY_JUMP): {
                    jumpKeyPressed = true;
                    if(!freeze) {
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
                    }
                    break;
                }
                default: {
                }
            }

        }

        //Called when a movement has finished.  This could be because the player has collided with something, released
        //the key for that movement or is now falling e.g. if they have walked off the end of a platform
        private void movementFinished(PlayerMovementType movementType, boolean falling) {
            if(!freeze) {
                //If we need to fall then this takes priority over everything else
                if (falling && movementType != PlayerMovementType.FALL) {
                    fallMover.start();
                } else if (movementType == PlayerMovementType.JUMP) {
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
                    //if we've just finished moving left or right and the user has been holding down the key for moving
                    //in the opposite direction then start moving in that direction
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
        }

        //Stop the movement for that key when it's released
        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyChar()) {
                case (KEY_LEFT): {
                    leftKeyPressed = false;
                    if(!freeze) {
                        leftMover.requestStop(false);
                    }
                    break;
                }
                case (KEY_RIGHT): {
                    rightKeyPressed = false;
                    if(!freeze) {
                        rightMover.requestStop(false);
                    }
                    break;
                }
                case (KEY_JUMP): {
                    jumpKeyPressed = false;
                    if(!freeze) {
                        jumpMover.requestStop(false);
                    }
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

    //Runnable class that updates the location of the JSW
    private abstract class JSWMover implements Runnable {


        private final PlayerMovementType movementType;
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

        private JSWMover(PlayerMovementType movementType, ExecutorService executorService,
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
         * Submits a request to stop the movement gracefully.  If the "wait" parameter is true then this blocks until the thread for the routine has completed.
         * Note that the {@link JSWControlsHandler} will still be notified when this movement is finished.
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

        public void stopImmediately() {
            if(future != null) {
                future.cancel(true);
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

            try {
                while (!stopMoving) {
                    boolean moveFinished = doMove();
                    jsw.onMove();
                    //@todo - should this be determined in here?  Can't we determine it when the movement is finished?
                    shouldFall = shouldStartFalling();
                    stopMoving = moveFinished && (stopRequestReceived || shouldFall);
                    if (!stopMoving) {
                        Thread.sleep(numMillisBetweenMovements);
                    }
                }

                running = false;
                jSWControlsHandler.movementFinished(movementType, shouldFall);
            }
            catch(InterruptedException e) {
                System.out.println("Movement stopped forcefully");
                running = false;
            }
        }

        protected abstract boolean doMove();

        private boolean shouldStartFalling() {
            if(movementType == PlayerMovementType.FALL) {
                return false;
            }
            Rectangle jswCollisionArea = jsw.getCollisionArea();
            //@todo check that the player hasn't walked on to a lethal object otherwise if you walk the player on to a lethal object that is at
            //exactly the same level as the platform then it won't detect it.
            return GameUtils.getCollidedWhileMovingUpOrDown(jsw, goingDownCaresAbout[jswCollisionArea.y +
                            jswCollisionArea.height + 1]) == null;
        }

        private void shutdown() {
            stopRequestReceived = true;
            executorService.shutdown();
        }

        //Returns false if the collision is a movement stopping event e.g. they've collided with a lethal object
        protected boolean checkPlayerCollisionOK(GameObject collidedWith) {
            if(collidedWith instanceof Lethal) {
                playerHitLethalObject((Lethal)collidedWith);
                return true;
            }
            else if(collidedWith instanceof FinishingObject) {
                playerHitFinishingObject((FinishingObject)collidedWith);
                return true;
            }

            return false;
        }
    }

    //Handles the movement of a sprite when its falling
    private class FallMover extends JSWMover {

        private FallMover() {
            super(PlayerMovementType.FALL, Executors.newSingleThreadExecutor(),
                    FALL_NUM_PIXELS_PER_MOVEMENT,
                    FALL_NUM_MILLIS_BETWEEN_MOVEMENTS);
        }
        @Override
        protected boolean doMove() {
            int startCheckpoint = jsw.getY() + jsw.getHeight() + 1;
            int endCheckPoint = jsw.getY() + jsw.getHeight() + numPixelsPerMovement;
            GameObject collidedWith = null;
            int i;
            for(i = startCheckpoint ; i <= endCheckPoint && collidedWith == null ;i++) {
                collidedWith = GameUtils.getCollidedWhileMovingUpOrDown(jsw, goingDownCaresAbout[i]);
                if(collidedWith == null) {
                    //We can fall
                    jsw.setLocation(jsw.getCollisionArea().x, i-jsw.getCollisionArea().height);
                }
                else {
                    checkPlayerCollisionOK(collidedWith);
                    //@todo - this is stopping itself rather than the loop in the superclass doing it.  Can this be done
                    //better?
                    requestStop(false);
                }
            }

            return true;
        }
    }

    //Handles the movement of the sprite when its jumping
    private class JumpMover extends JSWMover {

        //Defines the increments that need to be made to the sprite's location in order to get them to jump left, right
        //or up.

        private int[][] trajectoryRight;
        private int[][] trajectoryLeft;
        private int[][] trajectoryUp;

        private int startIndexInTrajectory;

        private int[][] trajectoryBeingUsed;


        private JumpMover() {

            super(PlayerMovementType.JUMP,Executors.newSingleThreadExecutor(), JUMP_NUM_PIXELS_PER_MOVEMENT,
                    JUMP_NUM_MILLIS_BETWEEN_MOVEMENTS);

            //Jumping consists of 3 types of movement 1: up and across, 2: across 3: down and across.
            //Each movement is only 1 pixel up/down and/or across but the number of these movements needs to be scaled
            // in relation to the screen size.
            int numOfMovementsUpAndAcross = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(100));
            int numOfMovementsAcross = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(100));
            int numOfMovementsDownAndAcross = GameUtils.getScaledPixelsToScreenWidthValue(new BigDecimal(100));
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
            GameObject collidedWith;
            for(int i = startIndexInTrajectory; i < endIndex && !movementTotallyBlocked; i++) {
                int[] movements = trajectoryBeingUsed[i];
                Point proposedNewLocation = new Point(jsw.getX() + movements[0], jsw.getY() + movements[1]);

                if(movements[1] != 0) {
                    int yOrdinateToCheck = movements[1] < 0
                            ?proposedNewLocation.y : proposedNewLocation.y +
                            jsw.getHeight();
                    GameObject[] possiblyCollidedWith = movements[1] < 0 ? goingUpCaresAbout[yOrdinateToCheck] : goingDownCaresAbout[yOrdinateToCheck];
                    collidedWith = GameUtils.getCollidedWhileMovingUpOrDown(jsw, possiblyCollidedWith);
                    if(collidedWith == null) {
                        jsw.setLocation(jsw.getX(), proposedNewLocation.y);
                    }
                    else {
                        //JSW has hit something whilepocant like a lethal object
                        //2: is only going up or down
                        //3: is moving across and down (we don't want them sliding along the ground)
                        movementTotallyBlocked = checkPlayerCollisionOK(collidedWith) || movements[0] == 0 || movements[1] > 0;
                    }
                }

                if(movements[0] != 0 && !movementTotallyBlocked) {
                    collidedWith = GameUtils.getCollidedWhileMovingAcross(jsw, movements[0] < 0 ? goingLeftCaresAbout[proposedNewLocation.x] :
                            goingRightCaresAbout[proposedNewLocation.x + jsw.getWidth()]);
                    if(collidedWith == null) {
                        jsw.setLocation(proposedNewLocation.x, jsw.getY());
                    }
                    else {
                        //JSW has hit something while moving left or right.  Block the movement if the JSW :
                        //1: Has hit something significant like a lethal object
                        //2: is only moving left or right
                        movementTotallyBlocked = checkPlayerCollisionOK(collidedWith) || movements[1] == 0;
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
    private class LeftMover extends JSWMover {


        private LeftMover() {
            super(PlayerMovementType.WALK_LEFT,
                    Executors.newSingleThreadExecutor(), LEFT_RIGHT_NUM_PIXELS_PER_MOVEMENT,
                    LEFT_RIGHT_NUM_MILLIS_BETWEEM_MOVEMENTS);
        }

        @Override
        protected boolean doMove() {

            int startCheckpoint = jsw.getX() - 1;
            int endCheckPoint = jsw.getX() - numPixelsPerMovement;

            GameObject collidedWith = null;
            int i;
            for(i = startCheckpoint ; i >= endCheckPoint && collidedWith == null ;i--) {
                collidedWith = GameUtils.getCollidedWhileMovingAcross(jsw, goingLeftCaresAbout[i]);
                if(collidedWith == null) {
                    //We can move
                    jsw.setLocation(i, jsw.getY());
                }
                else {
                    checkPlayerCollisionOK(collidedWith);
                }
            }
            return true;
        }
    };

    //Moves a sprite right
    private class RightMover extends JSWMover {


        private RightMover() {
            super(PlayerMovementType.WALK_RIGHT, Executors.newSingleThreadExecutor(),
                    LEFT_RIGHT_NUM_PIXELS_PER_MOVEMENT,
                    LEFT_RIGHT_NUM_MILLIS_BETWEEM_MOVEMENTS);
        }

        @Override
        protected boolean doMove() {
            int startCheckpoint = jsw.getX() + jsw.getWidth() + 1;
            int endCheckPoint = jsw.getX() + jsw.getWidth() + numPixelsPerMovement;

            GameObject collidedWith = null;
            int i;
            for(i = startCheckpoint ; i <= endCheckPoint && collidedWith == null ;i++) {
                collidedWith = GameUtils.getCollidedWhileMovingAcross(jsw, goingRightCaresAbout[i]);
                if(collidedWith == null) {
                    //We can move
                    jsw.setLocation(i - jsw.getWidth(), jsw.getY());
                }
                else {
                    checkPlayerCollisionOK(collidedWith);
                }
            }
            return true;
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
}
