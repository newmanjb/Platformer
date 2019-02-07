package com.noomtech.jsw.game.gameobjects.objects;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.game.gameobjects.AnimationFrame;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.MovingGameObject;
import com.noomtech.jsw.game.movement.CollisionHandler;
import com.noomtech.jsw.game.movement.SelfControlledMovingObject;

import java.awt.*;
import java.util.Map;


/**
 * Represents a moving object on the screen that moves either vertically or horizontally.  The direction is
 * determined from the value in the attributes under the key "{@link #ATTRIBUTE_AXIS_KEY}"
 * @author Joshua Newman
 */
public abstract class XorYMovingGameMovingObject extends MovingGameObject implements SelfControlledMovingObject {

    //Which axis it moves along
    private static final String ATTRIBUTE_AXIS_KEY = "x_axis";
    //Which direction it starts moving in
    private static final String ATTRIBUTE_MOVE_DIRECTION = "movement_direction";
    //How fast this moves
    private static final String ATTRIBUTE_SPEED = "speed";

    //The direction along its x_axis that this object is moving.  Default to 1 i.e. right for x-x_axis.  Would be -1
    //for the y axis
    private volatile boolean move_direction_positive;
    //Num pixels the object can move before the animation frame is updated
    private int pixelsPerFrameChange = 10;
    //If this object moves along the x-axis (left and right) or y (up and down).  Defaults to y.
    private boolean x_axis;
    //The last position on the axis that this object moves along was on
    private int lastOrdinate;
    //The num pixels that this object has moved since its last frame change
    private volatile int amountMovedSinceLastFrameChange;
    private AnimationFrame[] animationFrames;
    private int currentFrameIdx;
    //How many pixes this moves each time a movement is made
    private int numPixelsPerMovement;

    //There is only one type of movement category for this since it only has one type of movement
    private static final String ANIM_CATEGORY_MOVE = "move";
    private static final String[] ALL_ANIMATION_FRAME_CATEGORIES = new String[]{ANIM_CATEGORY_MOVE};


    public XorYMovingGameMovingObject(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
        setToStartingState();
    }


    @Override
    public String[] getAnimationFrameCategories() {
        return ALL_ANIMATION_FRAME_CATEGORIES;
    }

    @Override
    public void refreshAfterImageUpdate() {
        animationFrames = animationFramesMap.get(ANIM_CATEGORY_MOVE);
    }

    @Override
    protected void paintObject(Graphics graphics) {
        animationFrames[currentFrameIdx].draw(graphics, getImageArea());
    }

    public void setToStartingState() {
        setLocation(startingLocation.x, startingLocation.y);
        x_axis = Boolean.parseBoolean(CommonUtils.getAttribute(attributes, ATTRIBUTE_AXIS_KEY, "false"));
        move_direction_positive = Boolean.parseBoolean(CommonUtils.getAttribute(attributes, ATTRIBUTE_MOVE_DIRECTION, "true"));
        numPixelsPerMovement = Integer.parseInt(CommonUtils.getAttribute(attributes, ATTRIBUTE_SPEED, "1"));
        lastOrdinate = getY();
        amountMovedSinceLastFrameChange = 0;
        currentFrameIdx = 0;
    }

    /**
     * Move the object and return true if the move has hit the player
     */
    public boolean doMove(CollisionHandler collisionHandler) {

        for(int i = 0 ; i < numPixelsPerMovement; i++) {
            GameObject collidedWith = x_axis ? move_direction_positive ? collisionHandler.checkIfTouchingAnythingGoingRight(this) :
                    collisionHandler.checkIfTouchingAnythingGoingLeft(this) :
                    move_direction_positive ? collisionHandler.checkIfTouchingAnythingGoingDown(this) :
                            collisionHandler.checkIfTouchingAnythingGoingUp(this);
            if (collidedWith != null) {
                //Reverse the moving direction
                this.move_direction_positive = !move_direction_positive;
                //Reset the animation variables as we will now be moving in a different direction
                lastOrdinate = getCurrentOrdinateOnMovementAxis();
                amountMovedSinceLastFrameChange = 0;
            }
            setLocation(x_axis ? getX() + (move_direction_positive ? 1 : -1) : getX(), x_axis ? getY() : getY() + (move_direction_positive ? 1 : -1));

            //Work out the number of animation frames we need to move on (if any)
            int amountMovedNow = getCurrentOrdinateOnMovementAxis() - lastOrdinate;
            amountMovedNow = amountMovedNow > 0 ? amountMovedNow : -(amountMovedNow);
            int totalAmountMovedSinceLastFrameChange = amountMovedNow + amountMovedSinceLastFrameChange;
            int numFramesMoved = (int) Math.floor(totalAmountMovedSinceLastFrameChange / pixelsPerFrameChange);

            if (numFramesMoved > 0) {
                //Cycle around the frame list by the required amount
                int totalIdx = currentFrameIdx + numFramesMoved;
                currentFrameIdx = totalIdx % animationFrames.length;
            }

            amountMovedSinceLastFrameChange = totalAmountMovedSinceLastFrameChange % pixelsPerFrameChange;
            lastOrdinate = getCurrentOrdinateOnMovementAxis();

            if(collisionHandler.hasMovingObjectHitJSW(this)) {
                return true;
            }
        }

        return false;
    }

    private int getCurrentOrdinateOnMovementAxis() {
        return x_axis ? getX() : getY();
    }
}
