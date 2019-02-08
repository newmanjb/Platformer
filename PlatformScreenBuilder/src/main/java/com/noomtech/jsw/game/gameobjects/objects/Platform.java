package com.noomtech.jsw.game.gameobjects.objects;

import java.awt.*;
import java.io.IOException;
import java.util.Map;


/**
 * Represents a platform on the screen
 * @author Joshua Newman
 */
public class Platform extends IdleGameObject {


    public Platform(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
    }
}
