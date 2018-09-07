package com.noomtech.jsw.platformscreen.gameobjects;

import com.noomtech.jsw.common.utils.Utils;

import java.awt.*;
import java.util.Map;


/**
 * Represents an object on the screen e.g. player, platform
 * @author Joshua Newman
 */
//@todo fix a bug where player not reset back to starting position and nasty speeds up after player collects finishing 
    //object.  This will be because the setToStartingState implementation in this class was removed.  It's functionality 
    //needs to be put into the JSW and Nasty classes (or the MovingObject classes).  See the history of this class
public abstract class GameObject {


    //The boundaries of the object
    private Rectangle collisionArea;
    //The properties of the object
    private Map<String,String> attributes;
    //This is where the game object is first located when the game starts
    protected Point startingLocation;
    //Set to true once this object has been drawn.  Used by objects that never change in appearance during the game
    protected boolean staticObjectsDontNeedToBeDrawnAgain;


    public GameObject(Rectangle collisionArea, Map<String,String> attributes) {
        this.collisionArea = collisionArea;
        this.attributes = attributes;
        startingLocation = collisionArea.getLocation();
    }

    protected abstract void paintObject(Graphics g);

    public final void doPainting(Graphics g) {
        paintObject(g);

        //----Reinstate if needed----
        //Set to true if the painting is part of the actual game where these types of object won't change.  Otherwise don't
        //e.g. if the painting is part of the editor where these objects can be moved
        //staticObjectsDontNeedToBeDrawnAgain = Utils.gameIsRunning;
    }

    public Rectangle getCollisionArea() {
        return collisionArea;
    }

    public void setCollisionArea(Rectangle collisionArea) {
        this.collisionArea = collisionArea;
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

    public Point getLocation() {
        return collisionArea.getLocation();
    }
}
