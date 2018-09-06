package com.noomtech.jsw.platformscreen.gameobjects.objects;

import com.noomtech.jsw.platformscreen.gameobjects.GameObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * Represents a platform on the screen
 * @author Joshua Newman
 */
//@todo - load images from classpath for static objects like for moving objects
public class Platform extends GameObject {


    private BufferedImage image;

    public Platform(Rectangle collisionArea, Map<String,String> attributes) throws IOException {
        super(collisionArea, attributes);
        image = ImageIO.read(new File("C:/temp/images/platform1.jpg"));
    }


    @Override
    public void paintObject(Graphics g) {

        //if(!staticObjectsDontNeedToBeDrawnAgain) {
            Rectangle r = getCollisionArea();
            g.drawImage(image, r.x, r.y, r.width, r.height, Color.WHITE, null);
        //}
    }
}
