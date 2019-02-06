package com.noomtech.jsw.game.gameobjects.objects;

import com.noomtech.jsw.game.gameobjects.Lethal;

import java.awt.*;
import java.util.Map;


/**
 * Represents a horizontally or vertically moving, lethal object on the screen
 * @author Joshua Newman
 */
public class Nasty extends XorYMovingGameMovingObject implements Lethal {



    private static final String ANIMATION_FRAMES_DIRECTORY_NAME = Nasty.class.getSimpleName();


    public Nasty(Rectangle area, Map<String,String> attributes) {
        super(area, attributes);
        setToStartingState();
    }


    @Override
    public String getAnimationFramesDirectoryName() {
        return ANIMATION_FRAMES_DIRECTORY_NAME;
    }
}
