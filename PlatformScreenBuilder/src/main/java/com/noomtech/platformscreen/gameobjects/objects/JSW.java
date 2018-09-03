package com.noomtech.platformscreen.gameobjects.objects;

import com.noomtech.platformscreen.gameobjects.AnimatedGameObject;
import com.noomtech.platformscreen.gameobjects.AnimationFrame;
import com.noomtech.platformscreen.movement.PlayerMovementType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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



    //@todo scale the image up-front so save having to scale it every time it's drawn, which is a lot
    public JSW(Rectangle collisionArea) throws IOException {
        super(collisionArea);

        BufferedImage b1 = ImageIO.read(new File("C:/temp/images/WalkingStickMan1.jpg"));
        BufferedImage b2 = ImageIO.read(new File("C:/temp/images/WalkingStickMan2.jpg"));
        BufferedImage b3 = ImageIO.read(new File("C:/temp/images/WalkingStickMan3.jpg"));
        BufferedImage b4 = ImageIO.read(new File("C:/temp/images/WalkingStickMan4.jpg"));
        BufferedImage b5 = ImageIO.read(new File("C:/temp/images/WalkingStickMan5.jpg"));
        BufferedImage b6 = ImageIO.read(new File("C:/temp/images/WalkingStickMan6.jpg"));
        BufferedImage b7 = ImageIO.read(new File("C:/temp/images/WalkingStickMan7.jpg"));
        BufferedImage b8 = ImageIO.read(new File("C:/temp/images/WalkingStickMan8.jpg"));

        BufferedImage b1l = ImageIO.read(new File("C:/temp/images/WalkingStickMan1L.jpg"));
        BufferedImage b2l = ImageIO.read(new File("C:/temp/images/WalkingStickMan2L.jpg"));
        BufferedImage b3l = ImageIO.read(new File("C:/temp/images/WalkingStickMan3L.jpg"));
        BufferedImage b4l = ImageIO.read(new File("C:/temp/images/WalkingStickMan4L.jpg"));
        BufferedImage b5l = ImageIO.read(new File("C:/temp/images/WalkingStickMan5L.jpg"));
        BufferedImage b6l = ImageIO.read(new File("C:/temp/images/WalkingStickMan6L.jpg"));
        BufferedImage b7l = ImageIO.read(new File("C:/temp/images/WalkingStickMan7L.jpg"));
        BufferedImage b8l = ImageIO.read(new File("C:/temp/images/WalkingStickMan8L.jpg"));

        //Build the animation frames.  One list for going left, another for going right.
        rightFrames = new AnimationFrame[8];
        rightFrames[0] = (g,r) -> {
            g.drawImage(b1, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        rightFrames[1] = (g,r) -> {
            g.drawImage(b2, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        rightFrames[2] = (g,r) -> {
            g.drawImage(b3, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        rightFrames[3] = (g,r) -> {
            g.drawImage(b4, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        rightFrames[4] = (g,r) -> {
            g.drawImage(b5, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        rightFrames[5] = (g,r) -> {
            g.drawImage(b6, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        rightFrames[6] = (g,r) -> {
            g.drawImage(b7, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        rightFrames[7] = (g,r) -> {
            g.drawImage(b8, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };

        leftFrames = new AnimationFrame[8];
        leftFrames[0] = (g,r) -> {
            g.drawImage(b1l, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        leftFrames[1] = (g,r) -> {
            g.drawImage(b2l, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        leftFrames[2] = (g,r) -> {
            g.drawImage(b3l, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        leftFrames[3] = (g,r) -> {
            g.drawImage(b4l, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        leftFrames[4] = (g,r) -> {
            g.drawImage(b5l, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        leftFrames[5] = (g,r) -> {
            g.drawImage(b6l, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        leftFrames[6] = (g,r) -> {
            g.drawImage(b7l, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        leftFrames[7] = (g,r) -> {
            g.drawImage(b8l, r.x, r.y, r.width, r.height, Color.WHITE, null);
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
