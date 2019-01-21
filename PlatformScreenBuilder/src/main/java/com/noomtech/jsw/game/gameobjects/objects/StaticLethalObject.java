package com.noomtech.jsw.game.gameobjects.objects;

import com.noomtech.jsw.game.gameobjects.Lethal;
import com.noomtech.jsw.game.gameobjects.NonMovingObject;

import java.awt.*;
import java.util.Map;


/**
 * Represents a non-moving lethal object
 * @author Joshua Newman
 */
public class StaticLethalObject extends NonMovingObject implements Lethal {


    public StaticLethalObject(Rectangle area, Map<String,String> attributes) {
        super(area, attributes);
    }

    @Override
    public String getImageDirectory() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void paintObject(Graphics g) {
     //   if(!staticObjectsDontNeedToBeDrawnAgain) {
        Rectangle r = getImageArea();
        g.drawImage(image, r.x, r.y, r.width, r.height, Color.WHITE, null);
      //  }
    }
}
