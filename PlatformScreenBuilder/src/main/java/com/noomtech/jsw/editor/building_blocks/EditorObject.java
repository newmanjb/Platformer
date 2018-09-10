package com.noomtech.jsw.editor.building_blocks;

import com.noomtech.jsw.game.gameobjects.GameObject;

import java.awt.*;

public class EditorObject {


    private boolean isBeingMoved;
    private boolean isSelected;
    private long id;
    private final GameObject gameObject;

    public EditorObject(GameObject gameObject, long id) {
        this.id = id;
        this.gameObject = gameObject;
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
        return gameObject.getCollisionArea();
    }

    public void setRectangle(Rectangle rectangle) {
        gameObject.setCollisionArea(rectangle);
    }

    public GameObject getGameObject() {
        return gameObject;
    }
}
