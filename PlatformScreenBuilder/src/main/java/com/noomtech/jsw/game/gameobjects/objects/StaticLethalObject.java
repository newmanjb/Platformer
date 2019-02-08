package com.noomtech.jsw.game.gameobjects.objects;

import com.noomtech.jsw.game.gameobjects.Lethal;

import java.awt.*;
import java.util.Map;


/**
 * Represents a non-moving lethal object
 * @author Joshua Newman
 */
public class StaticLethalObject extends IdleGameObject implements Lethal  {


    public StaticLethalObject(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
    }
}
