package com.noomtech.jsw.game.handlers;

import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.concrete_objects.JSW;
import com.noomtech.jsw.game.gameobjects.concrete_objects.Platform;
import com.noomtech.jsw.game.GamePlayDisplay;
import com.noomtech.jsw.game.movement.jsw.*;
import com.noomtech.jsw.game.utils.GameUtils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;

/**
 * Handles the keyboard input for the player sprite.  Is also responsible for reacting to collisions involving the
 * player sprite e.g. when the player sprite hits a platform or a lethal object.
 */
public class JSWControlsHandler implements KeyListener {

    private final char KEY_LEFT = 'o';
    private final char KEY_RIGHT = 'p';
    private final char KEY_JUMP = 'm';

    //Each of these instances handle the movement of the sprite in a particular direction.  They are Runnable instances
    //that are run on separate threads that update the location of the player sprite in order to move it.
    //The sprite is then repainted by the repainting loop meaning that the player sprite is seen to move around the screen.
    private final JSWMover LEFT_MOVER;
    private final JSWMover RIGHT_MOVER;
    //Handles the movement for when the player sprite is falling
    private final JSWMover FALL_OVER;
    private final JSWMover JUMP_OVER;

    private final CollisionHandler COLLISION_HANDLER;

    private final JSW JSW;

    private final GamePlayDisplay GAME_PLAY_DISPLAY;

    public volatile boolean leftKeyPressed;
    public volatile boolean rightKeyPressed;
    public volatile boolean jumpKeyPressed;

    //Constants for movements
    private final int LEFT_RIGHT_NUM_PIXELS_PER_MOVEMENT = 1;
    private final int JUMP_NUM_PIXELS_PER_MOVEMENT = 1;
    private final int FALL_NUM_PIXELS_PER_MOVEMENT = 1;
    //The speed of the movements is scaled based on the existing screen size in relation to a 1920 x 1080 size e.g.
    //if this was being run on a much smaller screen of half the resolution (960 x 540) then these delays would have to
    //be twice as long as the distance for the player to walk across the screen for example would be half as much.
    private final int LEFT_RIGHT_NUM_MILLIS_BETWEEM_MOVEMENTS = GameUtils.getScaledMsToScreenWidthValue(new BigDecimal("6"));
    private final int JUMP_NUM_MILLIS_BETWEEN_MOVEMENTS = GameUtils.getScaledMsToScreenWidthValue(new BigDecimal("6"));
    private final int FALL_NUM_MILLIS_BETWEEN_MOVEMENTS = GameUtils.getScaledMsToScreenHeightValue(new BigDecimal("2"));


    public JSWControlsHandler(JSW jsw, CollisionHandler collisionHandler, GamePlayDisplay gameDisplay) {
        this.LEFT_MOVER = new LeftMover(collisionHandler, this, jsw, LEFT_RIGHT_NUM_PIXELS_PER_MOVEMENT,
                LEFT_RIGHT_NUM_MILLIS_BETWEEM_MOVEMENTS);
        this.RIGHT_MOVER = new RightMover(collisionHandler, this, jsw, LEFT_RIGHT_NUM_PIXELS_PER_MOVEMENT,
                LEFT_RIGHT_NUM_MILLIS_BETWEEM_MOVEMENTS);
        this.JUMP_OVER = new JumpMover(jsw, JUMP_NUM_PIXELS_PER_MOVEMENT, JUMP_NUM_MILLIS_BETWEEN_MOVEMENTS, collisionHandler, this);
        this.FALL_OVER = new FallMover(jsw, this, FALL_NUM_PIXELS_PER_MOVEMENT, FALL_NUM_MILLIS_BETWEEN_MOVEMENTS, collisionHandler);
        this.COLLISION_HANDLER = collisionHandler;
        this.JSW = jsw;
        this.GAME_PLAY_DISPLAY = gameDisplay;
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
                if (!LEFT_MOVER.isRunning() && !RIGHT_MOVER.isRunning() && !JUMP_OVER.isRunning() && !FALL_OVER.isRunning()) {
                    LEFT_MOVER.start();
                }

                break;
            }
            case (KEY_RIGHT): {
                rightKeyPressed = true;
                if (!LEFT_MOVER.isRunning() && !RIGHT_MOVER.isRunning() && !JUMP_OVER.isRunning() && !FALL_OVER.isRunning()) {
                    RIGHT_MOVER.start();
                }
                break;
            }
            case (KEY_JUMP): {
                jumpKeyPressed = true;
                //Can't jump if we're falling or already jumping
                if (!FALL_OVER.isRunning() && !JUMP_OVER.isRunning()) {
                    //If we're already moving left or right we will jump in that direction but we must stop the
                    //movement first otherwise it will interfere with the jumping movment
                    if (LEFT_MOVER.isRunning()) {
                        LEFT_MOVER.requestStop(true);
                    } else if (RIGHT_MOVER.isRunning()) {
                        RIGHT_MOVER.requestStop(true);
                    }
                    JUMP_OVER.start();
                }
                break;
            }
            default: {
            }
        }

    }

    //Called to notify the controller that the jsw has hit something while falling.  Returns true if the movement should
    //be stopped.
    public boolean hitWhileFalling(GameObject hit) {

        if (GAME_PLAY_DISPLAY.playerHitSomething(hit)) {
            return true;
        }

        if (hit instanceof Platform) {
            movementFinished(PlayerMovementType.FALL);
            return true;
        }

        return false;
    }

    //Called to notify the controller that the jsw has hit something while walking
    public boolean hitWhileWalking(GameObject hit, PlayerMovementType playerMovementType) {
        if (GAME_PLAY_DISPLAY.playerHitSomething(hit)) {
            return true;
        }

        if (hit instanceof Platform) {
            movementFinished(playerMovementType);
            return true;
        }

        return false;
    }

    //Called to notify the controller that the jsw has hit something while jumping.  As jumping involves moving in
    //both the X and Y directions this callback needs to behave differently depending on which direction the
    //player was moving at the time.
    public JumpMovementResult hitWhileJumping(GameObject hit, int[] movements, boolean movingInYDirection) {
        if (GAME_PLAY_DISPLAY.playerHitSomething(hit)) {
            return JumpMovementResult.STOP_COMPLETELY;
        }

        JumpMovementResult result = JumpMovementResult.MOVE;
        if (hit instanceof Platform) {
            //If the player has hit a platform then it could either stop them moving in this direction or stop
            //them entirely
            result = JumpMovementResult.DONT_MOVE_IN_THIS_DIRECTION;
            if (movingInYDirection) {
                //Stop the jump completely if the JSW:
                //1: is only going up or down
                //2: is moving across and down (we don't want them sliding along the ground)
                if (movements[0] == 0 || movements[1] > 0) {
                    result = JumpMovementResult.STOP_COMPLETELY;
                }
            } else {
                //Stop the jump if the jsw is only moving left or right and has hit something
                if (movements[1] == 0) {
                    result = JumpMovementResult.STOP_COMPLETELY;
                }
            }
        }

        if (result == JumpMovementResult.STOP_COMPLETELY) {
            //The jump is stopped so check if the jsw needs to fall
            if (!doFallCheck()) {
                //The jump got stopped and the jsw does not need to fall, so the movement's finished
                movementFinished(PlayerMovementType.JUMP);
            }
        }

        return result;
    }

    //Called when a jump movement completed without being stopped prematurely e.g. by the player hitting a platform
    public void jumpFinishedWithoutBeingStopped() {
        if (!doFallCheck()) {
            movementFinished(PlayerMovementType.JUMP);
        }
    }

    //Called when a left./right movement completed without being stopped prematurely e.g. by the player hitting a platform
    public void leftRightMoveFinishedWithoutBeingStopped(PlayerMovementType playerMovementType) {
        if (!doFallCheck()) {
            movementFinished(playerMovementType);
        }
    }

    //Returns true if the player should fall e.g. if they have nothing underneath them
    public boolean doFallCheck() {
        GameObject standingOn = COLLISION_HANDLER.checkIfTouchingAnythingGoingDown(JSW);
        if (standingOn == null || !(standingOn instanceof Platform)) {
            FALL_OVER.start();
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
                LEFT_MOVER.start();
            } else if (rightKeyPressed) {
                RIGHT_MOVER.start();
            }
        } else if (movementType == PlayerMovementType.FALL || movementType == PlayerMovementType.MOVED_BACK_TO_START) {
            //if we've just finished falling or dying and the user has been holding down the left, right or jump key then start the
            //player sprite moving accordingly
            if (jumpKeyPressed) {
                JUMP_OVER.start();
            } else if (leftKeyPressed) {
                LEFT_MOVER.start();
            } else if (rightKeyPressed) {
                RIGHT_MOVER.start();
            }
        }
        //if we've just finished moving left or right and the user has been holding down the key for moving
        //in the opposite direction then start moving in that direction
        else if (movementType == PlayerMovementType.WALK_LEFT) {
            if (rightKeyPressed && jumpKeyPressed) {
                JUMP_OVER.start();
            } else if (rightKeyPressed) {
                RIGHT_MOVER.start();
            }
        } else if (movementType == PlayerMovementType.WALK_RIGHT) {
            if (leftKeyPressed && jumpKeyPressed) {
                JUMP_OVER.start();
            } else if (leftKeyPressed) {
                LEFT_MOVER.start();
            }
        }
    }

    //Stop the movement for that key when it's released
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyChar()) {
            case (KEY_LEFT): {
                leftKeyPressed = false;
                LEFT_MOVER.requestStop(false);
                break;
            }
            case (KEY_RIGHT): {
                rightKeyPressed = false;
                RIGHT_MOVER.requestStop(false);
                break;
            }
            case (KEY_JUMP): {
                jumpKeyPressed = false;
                JUMP_OVER.requestStop(false);
                break;
            }
            default: {
            }
        }

    }

    //This may be called when the player dies for example and needs to go back to the start of the level
    public void setJSWToStartingState() {
        JSW.setToStartingState();
    }

    public void shutdown() {
        LEFT_MOVER.shutdown();
        RIGHT_MOVER.shutdown();
        JUMP_OVER.shutdown();
        FALL_OVER.shutdown();
    }
}