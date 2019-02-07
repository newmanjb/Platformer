package com.noomtech.jsw.game.gameobjects.objects;

import com.noomtech.jsw.game.gameobjects.NonMovingObject;

import java.awt.*;
import java.io.IOException;
import java.util.Map;


/**
 * Represents a platform on the screen
 * @author Joshua Newman
 */
public class Platform extends NonMovingObject {


    public Platform(Rectangle area, Map<String,String> attributes, long id) throws IOException {
        super(area, attributes, id);
    }


    @Override
    public String getImageDirectory() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void paintObject(Graphics g) {

        //if(!staticObjectsDontNeedToBeDrawnAgain) {
        Rectangle r = getImageArea();
        g.drawImage(image, r.x, r.y, r.width, r.height, Color.WHITE, null);
    }
}
