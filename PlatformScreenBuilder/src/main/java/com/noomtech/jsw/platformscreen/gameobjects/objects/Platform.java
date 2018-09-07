package com.noomtech.jsw.platformscreen.gameobjects.objects;

import com.noomtech.jsw.platformscreen.gameobjects.NonMovingObject;

import java.awt.*;
import java.io.IOException;
import java.util.Map;


/**
 * Represents a platform on the screen
 * @author Joshua Newman
 */
public class Platform extends NonMovingObject {


    public Platform(Rectangle collisionArea, Map<String,String> attributes) throws IOException {
        super(collisionArea, attributes);
    }


    @Override
    public String getImageDirectory() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void paintObject(Graphics g) {

        //if(!staticObjectsDontNeedToBeDrawnAgain) {
            Rectangle r = getCollisionArea();
            g.drawImage(image, r.x, r.y, r.width, r.height, Color.WHITE, null);
        //}
    }
}
