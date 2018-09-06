package com.noomtech.jsw.platformscreen.gameobjects.objects;

import com.noomtech.jsw.platformscreen.gameobjects.GameObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * An object that the player has to collect in order to finish the game
 * @author Joshua Newman
 */
public class FinishingObject extends GameObject {


    private BufferedImage image;


    public FinishingObject(Rectangle collisionArea, Map<String,String> attributes) throws IOException {
        super(collisionArea, attributes);
        image = ImageIO.read(new File("C:/temp/images/sky.png"));
    }


    public void paintObject(Graphics g) {
      //  if(!staticObjectsDontNeedToBeDrawnAgain) {
            Rectangle r = getCollisionArea();
            g.drawImage(image, r.x, r.y, r.width, r.height, Color.WHITE, null);
       // }
    }
}
