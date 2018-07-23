package com.noomtech.platformscreen.gameobjects;

import com.noomtech.platformscreen.movement.PlayerMovementType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * Represents a moving, lethal object on the screen
 * @author Joshua Newman
 */
public class Nasty extends AnimatedGameObject implements Lethal {


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


    public Nasty(Rectangle collisionArea) throws IOException {
        super(collisionArea);

        animationFrames = new AnimationFrame[8];

        BufferedImage b1 = ImageIO.read(new File("C:/temp/images/flamecollection1.jpg"));
        BufferedImage b2 = ImageIO.read(new File("C:/temp/images/flamecollection2.jpg"));
        BufferedImage b3 = ImageIO.read(new File("C:/temp/images/flamecollection3.jpg"));
        BufferedImage b4 = ImageIO.read(new File("C:/temp/images/flamecollection4.jpg"));
        BufferedImage b5 = ImageIO.read(new File("C:/temp/images/flamecollection5.jpg"));
        BufferedImage b6 = ImageIO.read(new File("C:/temp/images/flamecollection6.jpg"));
        BufferedImage b7 = ImageIO.read(new File("C:/temp/images/flamecollection7.jpg"));
        BufferedImage b8 = ImageIO.read(new File("C:/temp/images/flamecollection8.jpg"));

        animationFrames[0] = (g,r) -> {
            g.drawImage(b1, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        animationFrames[1] = (g,r) -> {
            g.drawImage(b2, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        animationFrames[2] = (g,r) -> {
            g.drawImage(b3, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        animationFrames[3] = (g,r) -> {
            g.drawImage(b4, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        animationFrames[4] = (g,r) -> {
            g.drawImage(b5, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        animationFrames[5] = (g,r) -> {
            g.drawImage(b6, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        animationFrames[6] = (g,r) -> {
            g.drawImage(b7, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
        animationFrames[7] = (g,r) -> {
            g.drawImage(b8, r.x, r.y, r.width, r.height, Color.WHITE, null);
        };
    }

    @Override
    public void paintIt(Graphics graphics) {
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
        super.setToStartingState();
        moveYDirection = 1;
        lastOrdinate = getY();
        amountMovedSinceLastFrameChange = 0;
        currentFrameIdx = 0;
    }

    //@todo sort out the movement class hierarchy (have player movement types and nasty movement types and then make
    //this method accept a generic type ?)
    /**
     * Works in the same way as {@link JSW#reactToMove(PlayerMovementType)} except that this only
     * has one list of animation frames to cycle through
     */
    protected void reactToMove(PlayerMovementType movementType) {

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
