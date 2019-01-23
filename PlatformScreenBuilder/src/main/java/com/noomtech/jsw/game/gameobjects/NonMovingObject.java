package com.noomtech.jsw.game.gameobjects;

import com.noomtech.jsw.common.utils.CommonUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;


/**
 * Represents all {@link GameObject}s that do no move in any way.
 */
public abstract class NonMovingObject extends GameObject {


    protected final BufferedImage image;


    /**
     * Loads the image for this game object using {@link #getImageDirectory()}
     * @see #getImageDirectory()
     */
    protected NonMovingObject(Rectangle r, Map<String,String> attributes) {
        super(r, attributes);

        //Load the image
        String directoryName = getImageDirectory();
        File[] files = CommonUtils.getImagesForStaticObjects(directoryName);
        File imageFile = files[0];

        try {
            image = ImageIO.read(imageFile);
        }
        catch(Exception e) {
            throw new IllegalStateException("Could not load image", e);
        }
    }

    /**
     * @return The name of the directory under {@link CommonUtils#STATIC_OBJECT_IMAGES_FOLDER} in which
     * the image file for this object resides.
     * @see CommonUtils#getAnimationImages(String, String[])
     */
    public abstract String getImageDirectory();
}
