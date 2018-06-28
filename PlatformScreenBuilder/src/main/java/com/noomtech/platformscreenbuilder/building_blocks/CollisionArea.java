package com.noomtech.platformscreenbuilder.building_blocks;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CollisionArea {


    private Rectangle rectangle;
    private Map<String,String> params = new HashMap(0);
    private boolean isBeingMoved;
    private boolean isSelected;
    private String theClass = "Not assigned yet";
    private long id;

    public CollisionArea(Rectangle rectangle, long id) {
        this.rectangle = rectangle;
        this.id = id;
    }

    public CollisionArea(Rectangle rectangle, Map<String, String> params, String theClass, long id) {
        this.rectangle = rectangle;
        this.params = params;
        this.theClass = theClass;
        this.id = id;
    }

    public boolean isBeingMoved() {
        return isBeingMoved;
    }

    public void setBeingMoved(boolean beingMoved) {
        isBeingMoved = beingMoved;
    }

    public void setRectangle(Rectangle r) {
        this.rectangle = r;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setParams(Map<String,String> params) {
        this.params = params;
    }

    public Map<String,String> getParams() {
        return params;
    }

    public String getTheClass() {
        return theClass;
    }

    public void setTheClass(String theClass) {
        this.theClass = theClass;
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
}
