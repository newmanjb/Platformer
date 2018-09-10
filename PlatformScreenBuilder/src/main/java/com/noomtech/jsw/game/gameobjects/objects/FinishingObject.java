package com.noomtech.jsw.game.gameobjects.objects;

import com.noomtech.jsw.game.gameobjects.NonMovingObject;

import java.awt.*;
import java.io.IOException;
import java.util.Map;


/**
 * An object that the player has to collect in order to finish the game
 * @author Joshua Newman
 */
public class FinishingObject extends NonMovingObject {


    public FinishingObject(Rectangle area, Map<String,String> attributes) throws IOException {
        super(area, attributes);
    }

    @Override
    public String getImageDirectory() {
        return this.getClass().getSimpleName();
    }

    public void paintObject(Graphics g) {
      //  if(!staticObjectsDontNeedToBeDrawnAgain) {
            Rectangle r = getArea();
            g.drawImage(image, r.x, r.y, r.width, r.height, Color.WHITE, null);
       // }
    }
}
