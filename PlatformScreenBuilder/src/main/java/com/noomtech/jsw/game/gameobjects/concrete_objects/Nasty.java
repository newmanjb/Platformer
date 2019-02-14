package com.noomtech.jsw.game.gameobjects.concrete_objects;

import com.noomtech.jsw.game.gameobjects.Lethal;
import com.noomtech.jsw.game.gameobjects.XorYMovingGameObject;

import java.awt.*;
import java.util.Map;


/**
 * Represents a horizontally or vertically moving, lethal object on the screen
 * @author Joshua Newman
 */
public class Nasty extends XorYMovingGameObject implements Lethal {


    public Nasty(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
    }

    protected boolean hittingPlayerIsShowstopper() {
        return true;
    }

}
