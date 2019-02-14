package com.noomtech.jsw.game.gameobjects.concrete_objects;

import com.noomtech.jsw.game.gameobjects.GameObjectStateFrame;
import com.noomtech.jsw.game.gameobjects.NonMovingAnimatedGameObject;

import java.awt.*;
import java.util.Map;

public class FinishingObject extends NonMovingAnimatedGameObject {


    private volatile GameObjectStateFrame[] currentAnimationFrames;
    private static final String STATE1_NAME = "waiting";
    private static final String[] ALL_STATES = new String[]{STATE1_NAME};


    public FinishingObject(Rectangle area, Map<String,String> attributes, long id) {
        super(area, attributes, id);
    }


    @Override
    protected GameObjectStateFrame[] getCurrentAnimationFrames() {
        return currentAnimationFrames;
    }

    @Override
    public void setToStartingState() {
        currentFrameIdx = 0;
        currentAnimationFrames = stateNameToStateObjectMap.get(STATE1_NAME);
    }

    @Override
    public String[] getGameObjectStateNames() {
        return ALL_STATES;
    }

    @Override
    protected void refreshAfterImageUpdate() {
        currentAnimationFrames = stateNameToStateObjectMap.get(STATE1_NAME);
    }
}
