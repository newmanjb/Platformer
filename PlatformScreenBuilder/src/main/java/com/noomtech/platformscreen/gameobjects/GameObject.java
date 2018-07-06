package com.noomtech.platformscreen.gameobjects;

import java.awt.*;
import java.util.Map;


/**
 * Represents an object on the screen e.g. player, platform
 * @author Joshua Newman
 */
public abstract class GameObject {


    //The boundaries of the object
    private Rectangle collisionArea;
    //The properties of the object
    private Map<String,String> attributes;


    public GameObject(Rectangle collisionArea) {
        this.collisionArea = collisionArea;
    }

    public abstract void paintIt(Graphics g);

    public Rectangle getCollisionArea() {
        return collisionArea;
    }

    public void setCollisionArea(Rectangle collisionArea) {
        this.collisionArea = collisionArea;
    }

    public void setAttributes(Map<String,String> attributes) {
        this.attributes = attributes;
    }

    public Map<String,String> getAttributes() {
        return attributes;
    }

    public int getHeight() {
        return collisionArea.height;
    }

    public int getWidth() {
        return collisionArea.width;
    }

    public int getX() {
        return collisionArea.x;
    }

    public int getY() {
        return collisionArea.y;
    }
}
