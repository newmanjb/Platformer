package com.noomtech.jsw.editor.building_blocks;

import com.noomtech.jsw.common.utils.db.DBConstants;
import com.noomtech.jsw.editor.gui.DrawingSettings;
import com.noomtech.jsw.editor.gui.Editable;
import com.noomtech.jsw.editor.gui.Saveable;
import com.noomtech.jsw.game.gameobjects.GameObject;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class RootObject implements Editable, Saveable {


    private boolean isBeingMoved;
    private boolean isSelected;
    private long id;
    private GameObject gameObject;
    private List<CollisionAreaEditableObject> collisionAreaEditableObjectList;
    private static final String COLLECTION_NAME = DBConstants.COLLECTION_NAME_GAME_OBJECTS;

    public RootObject(GameObject gameObject, long id) {
        this.id = id;
        setGameObject(gameObject);
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

    public Rectangle getArea() {
        return gameObject.getImageArea();
    }

    public void setLocation(Rectangle rectangle) {
        gameObject.setLocation(rectangle.x, rectangle.y);
        refreshFromGameObject();
    }

    public GameObject getGameObject() {
        return gameObject;
    }

    public void setGameObject(GameObject gameObject) {
        this.gameObject = gameObject;
        refreshFromGameObject();
    }

    public void addCollisionArea(CollisionAreaEditableObject collisionAreaEditableObject) {
        Rectangle[] dest = new Rectangle[gameObject.getCollisionAreas().length + 1];
        dest[dest.length - 1] = collisionAreaEditableObject.getArea();
        System.arraycopy(gameObject.getCollisionAreas(), 0, dest, 0, gameObject.getCollisionAreas().length);
        gameObject.setCollisionAreas(dest);

        collisionAreaEditableObjectList.add(collisionAreaEditableObject);
    }

    //Refresh the game object's collision areas in relation to the list held in this class
    public void updateCollisionAreas() {
        Rectangle[] dest = new Rectangle[collisionAreaEditableObjectList.size()];
        for(int i = 0 ; i < dest.length; i++) {
            dest[i] = collisionAreaEditableObjectList.get(i).getArea();
        }
        gameObject.setCollisionAreas(dest);
    }

    public void removeCollisionArea(CollisionAreaEditableObject toRemove) {
        collisionAreaEditableObjectList.remove(toRemove);
        Rectangle[] dest = new Rectangle[collisionAreaEditableObjectList.size()];
        for(int i = 0 ; i < dest.length; i++) {
            dest[i] = collisionAreaEditableObjectList.get(i).getArea();
        }
        gameObject.setCollisionAreas(dest);
    }

    public List<CollisionAreaEditableObject> getCollisionAreas() {
        return collisionAreaEditableObjectList;
    }

    @Override
    public void doPainting(Graphics g, DrawingSettings drawingSettings) {
        gameObject.doPainting(g);
        for(CollisionAreaEditableObject collisionAreaEditableObject : getCollisionAreas()) {
            collisionAreaEditableObject.doPainting(g, drawingSettings);
        }
    }

    public int hashCode() {
        return (int)id;
    }

    public String getCollectionName() {
        return COLLECTION_NAME;
    }

    public boolean equals(Object other) {
        return other instanceof RootObject && ((RootObject)other).getId() == getId();
    }

    @Override
    public String toString() {
        return "RootObject{" +
                "isBeingMoved=" + isBeingMoved +
                ", isSelected=" + isSelected +
                ", id=" + id +
                ", gameObject=" + gameObject +
                ", collisionAreaEditableObjectList=" + collisionAreaEditableObjectList +
                '}';
    }


    private void refreshFromGameObject() {
        collisionAreaEditableObjectList = Arrays.stream(gameObject.getCollisionAreas()).map(r -> {
            return new CollisionAreaEditableObject(r);
        }).collect(Collectors.toList());
    }
}
