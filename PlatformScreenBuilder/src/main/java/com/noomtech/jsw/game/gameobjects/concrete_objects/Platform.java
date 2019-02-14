package com.noomtech.jsw.game.gameobjects.concrete_objects;

import com.noomtech.jsw.game.gameobjects.IdleGameObject;

import com.noomtech.jsw.game.gameobjects.IdleGameObject;

import java.awt.*;
import java.util.Map;


/**
 * Represents a platform on the screen
 */
public class Platform extends IdleGameObject {


    public Platform(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
    }
}
