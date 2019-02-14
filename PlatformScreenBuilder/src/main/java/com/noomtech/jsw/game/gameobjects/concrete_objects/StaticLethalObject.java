package com.noomtech.jsw.game.gameobjects.concrete_objects;

import com.noomtech.jsw.game.gameobjects.IdleGameObject;
import com.noomtech.jsw.game.gameobjects.Lethal;

import java.awt.*;
import java.util.Map;


/**
 * Represents a non-moving lethal object
 */
public class StaticLethalObject extends IdleGameObject implements Lethal  {


    public StaticLethalObject(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
    }
}
