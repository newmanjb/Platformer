package com.noomtech.jsw.platformscreen.gameobjects;

import com.noomtech.jsw.common.utils.CommonUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Superclass for anything that moves (literally!).
 * @author Joshua Newman
 */
public abstract class MovingGameObject extends GameObject {


    protected Map<String, AnimationFrame[]> animationFramesMap;


    /**
     * Moving objects must have animation frames (images) associated with them.  There should be a set of images
     * for each type of movement e.g. going left, moving down, jumping, dying etc..  The type of movement is
     * referred to as a "category" and the frames are stored in lists in a map keyed under the
     * category e.g. the map of a player object's animation frames might consist of
     * ("Left"->{imgL1,imgL2,imgL3} "Right"->{imgR1,imgR2,imgR3}, "Jump"->{imgJ1,imgJ2,imgJ3,imgJ4})
     * @see CommonUtils#getAnimationImages(String, String[])
     * @see #getAnimationFrameCategories()
     * @see #getAnimationFramesDirectoryName()
     */
    public MovingGameObject(Rectangle collisionArea, Map<String, String> attributes) {
        super(collisionArea, attributes);
        try {


            String animationDirectory = getAnimationFramesDirectoryName();
            String[] animationCategories = getAnimationFrameCategories();
            //This method obtains the files for each image and they are then converted to images when the map is built below
            Map<String, File[]> keysToFiles = CommonUtils.getAnimationImages(animationDirectory, animationCategories);

            animationFramesMap = new HashMap(keysToFiles.size());
            for (Map.Entry<String, File[]> entries : keysToFiles.entrySet()) {
                File[] files = entries.getValue();
                AnimationFrame[] animationFrames = new AnimationFrame[files.length];
                for (int i = 0; i < animationFrames.length; i++) {
                    BufferedImage b = ImageIO.read(files[i]);
                    animationFrames[i] = (g, r) -> {
                        g.drawImage(b, r.x, r.y, r.width, r.height, Color.WHITE, null);
                    };
                }
                animationFramesMap.put(entries.getKey(), animationFrames);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not set attributes", e);
        }

    }

    /**
     * Callback for when the object has moved
     */
    public void onMove() {
        reactToMove();
    }

    /**
     * @return The animation frame categories for this game object
     * @see #MovingGameObject(Rectangle, Map)
     * @see #getAnimationFramesDirectoryName()
     */
    public abstract String[] getAnimationFrameCategories();

    /**
     * @return The name of the root directory that the subdirectories for the animation categories are stored under
     * @see CommonUtils#getAnimationImages(String, String[])
     * @see #getAnimationFrameCategories()
     * @see #MovingGameObject(Rectangle, Map)
     */
    public abstract String getAnimationFramesDirectoryName();

    /**
     * Subclasses should override in order to process a movement.  They can detect the type of movement by examining
     * their coordinates in related to their last set of coordinates on the previous move.
     */
    protected abstract void reactToMove();

    /**
     * Should set this object back the state it was in when it was drawn at the start of the game e.g. it should return
     * to its default location and display its default animation frame.
     */
    public abstract void setToStartingState();
}
