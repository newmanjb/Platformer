package com.noomtech.jsw.game.gameobjects.objects;

import com.noomtech.jsw.game.gameobjects.Lethal;

import java.awt.*;
import java.util.Map;


/**
 * Represents a horizontally or vertically moving, lethal object on the screen
 * @author Joshua Newman
 */
public class Nasty extends XorYMovingGameMovingObject implements Lethal {


    public Nasty(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
    }
}
