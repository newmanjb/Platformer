package com.noomtech.jsw.game.gameobjects;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.game.handlers.CollisionHandler;


import java.awt.*;
import java.util.Map;

public abstract class NonMovingAnimatedGameObject extends GameObject implements ComputerControlledObject,Static {

    //How fast this is animated
    private static final String ATTRIBUTE_SPEED = "speed";
    private long timeBetweenFrames;
    private long lastTimeMoveWasCalled;
    protected volatile int currentFrameIdx;


    public NonMovingAnimatedGameObject(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
        timeBetweenFrames = Long.parseLong(CommonUtils.getAttribute(attributes, ATTRIBUTE_SPEED, "150"));
        setToStartingState();
    }

    @Override
    protected void paintObject(Graphics graphics) {
        getCurrentAnimationFrames()[currentFrameIdx].draw(graphics, getImageArea());
    }

    protected abstract GameObjectStateFrame[] getCurrentAnimationFrames();

    public abstract void setToStartingState();

    public boolean doMove(CollisionHandler collisionHandler) {

        if(lastTimeMoveWasCalled == 0) {
            lastTimeMoveWasCalled = System.currentTimeMillis();
        }
        else {
            long timeElapsedSinceLastMove = System.currentTimeMillis() - lastTimeMoveWasCalled;
            if(timeElapsedSinceLastMove > timeBetweenFrames) {
                int numFrameChanges = (int) Math.floorDiv(timeElapsedSinceLastMove, timeBetweenFrames);
                GameObjectStateFrame[] currentAnimationFrames = getCurrentAnimationFrames();
                int numFramesToMove = numFrameChanges % currentAnimationFrames.length;
                currentFrameIdx =
                        (numFramesToMove + currentFrameIdx < currentAnimationFrames.length ? numFramesToMove + currentFrameIdx :
                                numFramesToMove - ((currentAnimationFrames.length) - currentFrameIdx));
                lastTimeMoveWasCalled = System.currentTimeMillis();
            }
        }

        return false;
    }
}
