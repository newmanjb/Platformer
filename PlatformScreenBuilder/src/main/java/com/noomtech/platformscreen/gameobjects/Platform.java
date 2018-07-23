package com.noomtech.platformscreen.gameobjects;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * Represents a platform on the screen
 * @author Joshua Newman
 */
public class Platform extends GameObject {
//@todo - put all images in resources folder and have a default one for when object is first created in editor

    private static final Color COLOR = Color.GREEN;
    private BufferedImage image;

    public Platform(Rectangle collisionArea) throws IOException {
        super(collisionArea);
        image = ImageIO.read(new File("C:/temp/images/platform1.jpg"));
    }


    @Override
    public void paintIt(Graphics g) {
        Rectangle r = getCollisionArea();
        g.drawImage(image, r.x, r.y, r.width, r.height,Color.WHITE, null);
    }
}
