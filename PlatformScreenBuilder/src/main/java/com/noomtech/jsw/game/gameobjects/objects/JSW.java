package com.noomtech.jsw.game.gameobjects.objects;

import com.noomtech.jsw.game.gameobjects.MovingGameObject;
import com.noomtech.jsw.game.gameobjects.AnimationFrame;

import java.awt.*;
import java.util.Map;

/**
 * The player
 * @author Joshua Newman
 */
public class JSW extends MovingGameObject {


    //Running total of the amount of pixels that the player has moved since the last frame change
    private int numPixelsMovedLeft;
    private int numPixelsMovedRight;
    //This is the num pixels that the player can move before the next frame is displayed
    private static int pixelsPerFrameChange = 12;
    //The frames for moving left
    private AnimationFrame[] leftFrames;
    //The frames for moving right
    private AnimationFrame[] rightFrames;
    //The current list of frames being used
    private AnimationFrame[] currentFrameList;
    //This points to the frame currently being displayed in the list
    private int currentFrameIdx;
    //The x ordinate of the JSW from the last movement
    private int lastXLocation;

    private static final String ANIM_FRAMES_DIRECTORY = JSW.class.getSimpleName();
    private static final String ANIM_CATEGORY_LEFT = "Left";
    private static final String ANIM_CATEGORY_RIGHT = "Right";
    private static String[] ALL_ANIMATION_FRAME_CATEGORIES = new String[]{ANIM_CATEGORY_LEFT, ANIM_CATEGORY_RIGHT};


    public JSW(Rectangle area, Map<String,String> attributes) {
        super(area, attributes);
        leftFrames = animationFramesMap.get(ANIM_CATEGORY_LEFT);
        rightFrames = animationFramesMap.get(ANIM_CATEGORY_RIGHT);
        currentFrameList = rightFrames;
        setToStartingState();
    }

    @Override
    public void paintObject(Graphics graphics) {
        currentFrameList[currentFrameIdx].draw(graphics, getImageArea());
    }

    @Override
    public String[] getAnimationFrameCategories() {
        return ALL_ANIMATION_FRAME_CATEGORIES;
    }

    @Override
    public String getAnimationFramesDirectoryName() {
        return ANIM_FRAMES_DIRECTORY;
    }

    public void reactToMove() {
        int xdiff = getX() - lastXLocation;
        int numFramesMoved = 0;
        if(xdiff > 0) {
            //We're going right
            currentFrameList = rightFrames;
            numPixelsMovedLeft = 0;
            //the total number we've moved in this direction since the last frame change or direction
            //change
            int moved = numPixelsMovedRight+xdiff;
            //Calculate the num frames we need to move on by
            numFramesMoved = (int)Math.floor(moved / pixelsPerFrameChange);
            //Add the left-over pixels on to the running total
            numPixelsMovedRight = moved % pixelsPerFrameChange;
        }
        else if(xdiff < 0) {
            //As above except for going left
            currentFrameList = leftFrames;
            numPixelsMovedRight = 0;
            int moved = numPixelsMovedLeft+(-xdiff);
            numFramesMoved = (int)Math.floor(moved / pixelsPerFrameChange);
            numPixelsMovedLeft = moved % pixelsPerFrameChange;
        }

        if(numFramesMoved > 0) {
            //Cycle around the frame list by the required amount
            int totalIdx = currentFrameIdx + numFramesMoved;
            currentFrameIdx = totalIdx % currentFrameList.length;
        }

        lastXLocation = getX();
    }

    public void setToStartingState() {
        setLocation(startingLocation.x, startingLocation.y);
        currentFrameList = rightFrames;
        currentFrameIdx = 0;;
        numPixelsMovedRight = 0;
        numPixelsMovedLeft = 0;
        lastXLocation = getX();
    }
}
