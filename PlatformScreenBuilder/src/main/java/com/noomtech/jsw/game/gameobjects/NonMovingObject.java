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


    protected BufferedImage image;


    protected NonMovingObject(Rectangle r, Map<String,String> attributes, long id) {
        super(r, attributes, id);
    }

    /**
     * Loads the image for this game object using {@link #getImageFolderName()}
     * @see CommonUtils#getImageForStaticObject(String, long)
     * @see #getImageFolderName()
     */
    @Override
    public void onImageUpdated() {
        //Load the image
        String directoryName = getImageFolderName();
        try {
            image = ImageIO.read(CommonUtils.getImageForStaticObject(directoryName, getId()));
        }
        catch(Exception e) {
            throw new IllegalStateException("Could not load image", e);
        }
    }
}
