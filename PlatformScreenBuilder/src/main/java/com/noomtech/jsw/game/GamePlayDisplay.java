package com.noomtech.jsw.game;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.Lethal;
import com.noomtech.jsw.game.gameobjects.Static;
import com.noomtech.jsw.game.gameobjects.concrete_objects.FinishingObject;
import com.noomtech.jsw.game.gameobjects.concrete_objects.JSW;
import com.noomtech.jsw.game.handlers.JSWControlsHandler;
import com.noomtech.jsw.game.handlers.CollisionHandler;
import com.noomtech.jsw.game.handlers.ComputerControlledMovementHandler;
import com.noomtech.jsw.game.gameobjects.ComputerControlledObject;
import com.noomtech.jsw.game.utils.GameUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


/**
 * This panel class is responsible for the painting of the game and also acts as a hub for events.
 * NOTE ON MOVEMENT - The movement of the player is handled by the {@link JSWControlsHandler}, which obviously has to
 * respond to key events.  The movement of anything else is computer-controlled and is handled by the {@link ComputerControlledMovementHandler}.
 * Each {@link GameObject}, which represents any object in the game, uses interfaces or abstract classes in
 * order to indicate whether it moves, is static, or is static but animated etc..
 * @see Static
 * @see ComputerControlledObject
 * @see com.noomtech.jsw.game.gameobjects.IdleGameObject
 * @see com.noomtech.jsw.game.gameobjects.NonMovingAnimatedGameObject
 * @see com.noomtech.jsw.game.gameobjects.XorYMovingGameObject
 */
public class GamePlayDisplay extends JPanel {


    //This receives notifications from this class
    private final GameFrame GAME_FRAME;

    /**
     * This handles collisions between objects during the game
     * @see CollisionHandler
     */
    private final CollisionHandler COLLISION_HANDLER;

    //Does the painting.  This can be swapped around according to what we need
    private volatile Consumer<Graphics> painter;
    private final MessagePainter MESSAGE_PAINTER = new MessagePainter();
    private final StandardPainter STANDARD_PAINTER = new StandardPainter(Color.WHITE);

    private final List<GameObject> ALL_GAME_OBJECTS_EXCEPT_JSW;
    //Used for symchronization around the starting and stopping of the game
    private final Object START_STOP_MUTEX = new Object();

    //True if the game is running.  Used to keep the game loop going and to stop it
    private volatile boolean started = false;
    //The player sprite (jsw = "Jet Set Willy")
    private final JSW JSW;
    //Coordinates the keyboard input and moves the player sprite in the appropriate direction
    private JSWControlsHandler jSWControlsHandler;
    //Handles the movements of every non-user-controlled object
    private ComputerControlledMovementHandler computerControlledMovementHandler;
    private final List<ComputerControlledObject> computerControlledGameObjects = new ArrayList<>();

    private final BufferedImage BACKGROUND_IMAGE;


    public GamePlayDisplay(GameFrame parent,
                           List<GameObject> gameObjects) throws IOException {

        this.GAME_FRAME = parent;
        this.ALL_GAME_OBJECTS_EXCEPT_JSW = gameObjects;
        List<GameObject> staticGameObjectsForCollisionHandler = new ArrayList<>();
        JSW tempJSW = null;
        for(GameObject gameObject : gameObjects) {
            if(gameObject instanceof ComputerControlledObject) {
                computerControlledGameObjects.add((ComputerControlledObject)gameObject);
            }

            if(gameObject instanceof Static) {
                staticGameObjectsForCollisionHandler.add(gameObject);
            }

            if(gameObject instanceof JSW) {
                if(tempJSW != null) {
                    throw new IllegalArgumentException("Only allowed one player!");
                }
                tempJSW = (JSW)gameObject;
            }
        }

        if(tempJSW == null) {
            throw new IllegalStateException("Must have a JSW!");
        }

        ALL_GAME_OBJECTS_EXCEPT_JSW.remove(tempJSW);
        JSW = tempJSW;
        //Build the collision handler using the game objects that don't move
        COLLISION_HANDLER = new CollisionHandler(staticGameObjectsForCollisionHandler, 4000, JSW);
        BACKGROUND_IMAGE = CommonUtils.getBackgroundImage();

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
            painter = STANDARD_PAINTER;
            //The game thread that repaints, sleeps and then repaints again until the game is stopped
            Thread painterThread = new Thread(() -> {
                while(started) {
                    try {
                        SwingUtilities.invokeLater(() -> repaint());
                        Thread.sleep(25);
                    }
                    catch(InterruptedException e) {
                        System.out.println("Game loop thread interrupted.  Stopping game");
                    }
                }
            }, "Painter");
            painterThread.start();
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

    private void startMovementComponents() {
        jSWControlsHandler = new JSWControlsHandler(JSW, COLLISION_HANDLER, this);
        computerControlledMovementHandler = new ComputerControlledMovementHandler(this,
                computerControlledGameObjects, COLLISION_HANDLER);
        addKeyListener(jSWControlsHandler);
        computerControlledMovementHandler.start();
    }

    private void stopMovementComponents() throws InterruptedException {
        removeKeyListener(jSWControlsHandler);
        jSWControlsHandler.shutdown();
        computerControlledMovementHandler.stop();
    }

    //This might be called after the player has died and needs to go back to the start, for example
    private void setGameObjectsToStartingState() {
        computerControlledMovementHandler.setEverythingToStartingState();
        jSWControlsHandler.setJSWToStartingState();
    }

    //Paints the game
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        painter.accept(g);
    }

    //Callback for when the the player has hit a lethal object
    private void playerHitLethalObject(Lethal lethalThatWasHit) {

        MESSAGE_PAINTER.backgroundColor = Color.RED;
        MESSAGE_PAINTER.message = null;
        painter = MESSAGE_PAINTER;

        try {stopMovementComponents();} catch(InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        GameUtils.sleepAndCatchInterrupt(300);
        MESSAGE_PAINTER.backgroundColor = Color.ORANGE;
        GameUtils.sleepAndCatchInterrupt(300);
        MESSAGE_PAINTER.backgroundColor = Color.WHITE;

        setGameObjectsToStartingState();
        painter = STANDARD_PAINTER;
        startMovementComponents();
    }

    //Callback for when the the player has hit a finishing object
    private void playerHitFinishingObject(GameObject finishingObject) {

        try {
            MESSAGE_PAINTER.backgroundColor = Color.BLUE;
            MESSAGE_PAINTER.message = "WELL DONE!!";
            painter = MESSAGE_PAINTER;

            GameUtils.sleepAndCatchInterrupt(500);
            MESSAGE_PAINTER.backgroundColor = Color.GREEN;
            GameUtils.sleepAndCatchInterrupt(500);
            MESSAGE_PAINTER.backgroundColor = Color.WHITE;

            stop();

            GAME_FRAME.onLevelFinished();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Check if anything needs to be done depending on what's been hit.  Return true if it's a show-stopper e.g.
    //player has died
    public boolean playerHitSomething(Object beenHit) {
        if(beenHit instanceof Lethal) {
            removeKeyListener(jSWControlsHandler);
            Executors.newSingleThreadExecutor().submit(()->{playerHitLethalObject((Lethal)beenHit);});
            return true;
        }
        else if(beenHit instanceof FinishingObject){
            removeKeyListener(jSWControlsHandler);
            Executors.newSingleThreadExecutor().submit(()->{playerHitFinishingObject((FinishingObject)beenHit);});
            return true;
        }

        return false;
    }

    //Paints the game objects in their respective location etc..  Does the painting most of the time.
    private class StandardPainter implements Consumer<Graphics> {

        //The colour of the background in the game
        private Color backgroundColor;

        public StandardPainter(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public void accept(Graphics g) {

            //@todo - if there is an issue with slowness then it could be that the static objects like the platforms are being
            //repainted every time when they don't need to be.  See the GameObject.doPainting and the static game object's paintObject methods.  There is
            //commented out code in these methods that only paints the object once.  This didn't work when it was implemented because the
            //object would get painted once and then, when the next iteration of the game loop was called and this code here was called,
            //they would get wiped out by the background drawn below and then never painted again.  So if there is slowness then this code could be reinstated and
            //this rectangle should never be drawn.  Instead the moving objects only could be rubbed out and repainted.
            Dimension screenSize = getSize();
            if(BACKGROUND_IMAGE == null) {
                g.setColor(Color.lightGray);
                g.fillRect(0, 0, screenSize.width, screenSize.height);
            }
            else {
                g.drawImage(BACKGROUND_IMAGE, 0, 0, screenSize.width, screenSize.height, null);
            }


            JSW.doPainting(g);

            for(GameObject gameObject : ALL_GAME_OBJECTS_EXCEPT_JSW) {
                gameObject.doPainting(g);
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
