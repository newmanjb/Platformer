package com.noomtech.platformscreenbuilder.building_blocks;

import com.noomtech.platformscreen.gameobjects.GameObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EditorObject {


    private boolean isBeingMoved;
    private boolean isSelected;
    private long id;
    private GameObject gameObject;
    private Rectangle rectangle;

    public EditorObject(Rectangle rectangle, long id) {
        this.rectangle = rectangle;
        this.id = id;
    }


    public boolean isBeingMoved() {
        return isBeingMoved;
    }

    public void setBeingMoved(boolean beingMoved) {
        isBeingMoved = beingMoved;
    }

    public long getId() {
        return id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
        if(gameObject != null) {
            gameObject.setCollisionArea(rectangle);
        }
    }

    public GameObject getGameObject() {
        return gameObject;
    }

    public void setGameObject(GameObject gameObject) {
        this.gameObject = gameObject;
    }
}
