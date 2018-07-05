package com.noomtech.platformscreen.gameobjects;

import java.awt.*;
import java.util.Map;


public abstract class GameObject {


    private Rectangle collisionArea;
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
}
