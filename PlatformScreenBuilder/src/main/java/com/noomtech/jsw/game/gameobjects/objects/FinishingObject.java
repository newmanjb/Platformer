package com.noomtech.jsw.game.gameobjects.objects;

import java.awt.*;
import java.util.Map;


/**
 * An object that the player has to collect in order to finish the game
 * @author Joshua Newman
 */
public class FinishingObject extends IdleGameObject {


    public FinishingObject(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
    }
}
