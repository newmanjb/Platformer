package com.noomtech.jsw.game.gameobjects.objects;

import java.awt.*;
import java.util.Map;


/**
 * Represents an object that doesn't move, isn't animated and that has only one state ("nada") e.g. a static platform
 * @author Joshua Newman
 */
public class IdleGameObject extends GameObject implements Static {


    public static final String ONLY_STATE_NAME = "nada";
    private static final String[] ALL_STATES = new String[]{ONLY_STATE_NAME};

    protected GameObjectStateFrame onlyState;

    public IdleGameObject(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
    }


    @Override
    protected void refreshAfterImageUpdate() {
        onlyState = stateNameToStateObjectMap.entrySet().iterator().next().getValue()[0];
    }

    @Override
    public void paintObject(Graphics g) {
        onlyState.draw(g, getImageArea());
    }

    @Override
    public String[] getGameObjectStateNames() {
        return ALL_STATES;
    }
}
