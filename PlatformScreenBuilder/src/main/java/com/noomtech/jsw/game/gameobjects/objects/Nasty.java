package com.noomtech.jsw.game.gameobjects.objects;

import com.noomtech.jsw.game.gameobjects.MovingGameObject;
import com.noomtech.jsw.game.gameobjects.AnimationFrame;
import com.noomtech.jsw.game.gameobjects.Lethal;

import java.awt.*;
import java.io.IOException;
import java.util.Map;


/**
 * Represents a moving, lethal object on the screen
 * @author Joshua Newman
 */
public class Nasty extends MovingGameObject implements Lethal {


    //The direction along the y-axis that this nasty is moving.  Start the nasty off moving down
    private volatile int moveYDirection = 1;
    //Num pixels the nasty can move before the animation frame is updated
    private static int pixelsPerFrameChange = 10;
    //The last position on the axis that this nasty moves along was on
    private int lastOrdinate;
    //The num pixels that this nasty has moved since its last frame change
    private volatile int amountMovedSinceLastFrameChange;
    private AnimationFrame[] animationFrames;
    private int currentFrameIdx;

    //There is only one type of movement for this
    private static final String ANIM_CATEGORY_MOVE = "move";
    private static final String[] ALL_ANIMATION_FRAME_CATEGORIES = new String[]{ANIM_CATEGORY_MOVE};
    private static final String ANIMATION_FRAMES_DIRECTORY_NAME = Nasty.class.getSimpleName();


    public Nasty(Rectangle collisionArea, Map<String,String> attributes) throws IOException {
        super(collisionArea, attributes);
        animationFrames = animationFramesMap.get(ANIM_CATEGORY_MOVE);
        setToStartingState();
    }

    @Override
    public void paintObject(Graphics graphics) {
        animationFrames[currentFrameIdx].draw(graphics, getCollisionArea());
    }


    public void setLocation(int x, int y) {
        Rectangle r = getCollisionArea();
        r.x = x;
        r.y = y;
    }

    /**
     * @return Which direction this nasty is moving in - 1 for down, -1 for up.
     */
    public int getMoveYDirection() {
        return moveYDirection;
    }

    /**
     * Gives this nasty the opportunity to update itself in reaction to a collision e.g. it might change direction
     */
    public void onCollision() {
        //Go from moving down to up or from moving up to down
        moveYDirection = -moveYDirection;
        //Reset the animation variables as we will now be moving in a different direction
        lastOrdinate = getY();
        amountMovedSinceLastFrameChange = 0;
    }

    public void setToStartingState() {
        getCollisionArea().setLocation(startingLocation);
        moveYDirection = 1;
        lastOrdinate = getY();
        amountMovedSinceLastFrameChange = 0;
        currentFrameIdx = 0;
    }

    @Override
    public String[] getAnimationFrameCategories() {
        return ALL_ANIMATION_FRAME_CATEGORIES;
    }

    @Override
    public String getAnimationFramesDirectoryName() {
        return ANIMATION_FRAMES_DIRECTORY_NAME;
    }

    /**
     * Works in the same way as {@link JSW#reactToMove()} except that this only
     * has one list of animation frames to cycle through
     */
    protected void reactToMove() {

        int amountMovedNow = getY() - lastOrdinate;
        amountMovedNow = amountMovedNow > 0 ? amountMovedNow : -(amountMovedNow);
        int totalAmountMovedSinceLastFrameChange = amountMovedNow + amountMovedSinceLastFrameChange;
        int numFramesMoved = (int)Math.floor(totalAmountMovedSinceLastFrameChange / pixelsPerFrameChange);

        if(numFramesMoved > 0) {
            //Cycle around the frame list by the required amount
            int totalIdx = currentFrameIdx + numFramesMoved;
            currentFrameIdx = totalIdx % animationFrames.length;
        }

        amountMovedSinceLastFrameChange = totalAmountMovedSinceLastFrameChange % pixelsPerFrameChange;
        lastOrdinate = getY();
    }
}
