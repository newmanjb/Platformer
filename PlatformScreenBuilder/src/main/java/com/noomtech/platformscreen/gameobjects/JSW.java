package com.noomtech.platformscreen.gameobjects;

import com.noomtech.platformscreen.movement.PlayerMovementType;

import java.awt.*;

/**
 * The player
 * @author Joshua Newman
 */
public class JSW extends AnimatedGameObject {
    ;
    //Running total of the amount of pixels that the player has moved since the last frame change
    private int numPixelsMovedLeft;
    private int numPixelsMovedRight;
    //This is the num pixels that the player can move before the next frame is displayed
    private static int pixelsPerFrameChange = 20;
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



    public JSW(Rectangle collisionArea) {
        super(collisionArea);

        //Build the animation frames.  One list for going left, another for going right.
        rightFrames = new AnimationFrame[3];
        rightFrames[0] = (g,r) -> {
            int inset = 0;
            g.setColor(Color.RED);
            g.fillRect(r.x + inset,r.y + inset,r.width - (2*inset),r.height-(2*inset));
        };
        rightFrames[1] = (g,r) -> {
            int inset = 4;
            g.setColor(Color.ORANGE);
            g.fillRect(r.x + inset,r.y + inset,r.width - (2*inset),r.height-(2*inset));
        };
        rightFrames[2] = (g,r) -> {
            int inset = 8;
            g.setColor(Color.YELLOW);
            g.fillRect(r.x + inset,r.y + inset,r.width - (2*inset),r.height-(2*inset));
        };

        leftFrames = new AnimationFrame[3];
        leftFrames[0] = (g,r) -> {
            int inset = 0;
            g.setColor(Color.MAGENTA);
            g.fillRect(r.x + inset,r.y + inset,r.width - (2*inset),r.height-(2*inset));
        };
        leftFrames[1] = (g,r) -> {
            int inset = 4;
            g.setColor(Color.BLUE);
            g.fillRect(r.x + inset,r.y + inset,r.width - (2*inset),r.height-(2*inset));
        };
        leftFrames[2] = (g,r) -> {
            int inset = 8;
            g.setColor(Color.CYAN);
            g.fillRect(r.x + inset,r.y + inset,r.width - (2*inset),r.height-(2*inset));
        };

        setToStartingState();
    }

    @Override
    public void paintIt(Graphics graphics) {
        currentFrameList[currentFrameIdx].draw(graphics, getCollisionArea());
    }

    public void setLocation(int x, int y) {
        Rectangle r = getCollisionArea();
        r.x = x;
        r.y = y;
    }

    public void reactToMove(PlayerMovementType movementType) {
        int xdiff = getX() - lastXLocation;
        int numFramesMoved;
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
        else {
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
        super.setToStartingState();
        currentFrameList = rightFrames;
        currentFrameIdx = 0;;
        numPixelsMovedRight = 0;
        numPixelsMovedLeft = 0;
        lastXLocation = getX();
    }
}
