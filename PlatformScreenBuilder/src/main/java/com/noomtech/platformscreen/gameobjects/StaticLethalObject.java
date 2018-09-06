package com.noomtech.jsw.platformscreen.gameobjects.objects;

import com.noomtech.jsw.platformscreen.gameobjects.GameObject;
import com.noomtech.jsw.platformscreen.gameobjects.Lethal;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * Represents a non-moving lethal object
 * @author Joshua Newman
 */
public class StaticLethalObject extends GameObject implements Lethal {

    private BufferedImage bufferedImage;

    public StaticLethalObject(Rectangle collisionArea, Map<String,String> attributes) throws IOException {
        super(collisionArea, attributes);
        bufferedImage = ImageIO.read(new File("C:/temp/images/water.png"));
    }

    @Override
    public void paintObject(Graphics g) {
     //   if(!staticObjectsDontNeedToBeDrawnAgain) {
            Rectangle r = getCollisionArea();
            g.drawImage(bufferedImage, r.x, r.y, r.width, r.height, Color.WHITE, null);
      //  }
    }
}
