package com.noomtech.platformscreen.gameobjects;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * An object that the player has to collect in order to finish the game
 * @author Joshua Newman
 */
public class FinishingObject extends GameObject {

//todo - put all image files in resources folder and get it from classpath
    private BufferedImage image;


    public FinishingObject(Rectangle collisionArea) throws IOException {
        super(collisionArea);
        image = ImageIO.read(new File("C:/temp/images/sky.png"));
    }


    public void paintIt(Graphics g) {
        Rectangle r = getCollisionArea();
        g.drawImage(image, r.x, r.y, r.width, r.height, Color.WHITE, null);
    }
}
