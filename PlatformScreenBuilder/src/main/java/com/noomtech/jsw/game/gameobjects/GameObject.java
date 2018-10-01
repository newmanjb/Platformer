package com.noomtech.jsw.game.gameobjects;

import java.awt.*;
import java.util.Map;


/**
 * Represents an object on the screen e.g. player, platform.
 * @author Joshua Newman
 */
public abstract class GameObject {


    /** The boundaries of the object's image.  This is used to define its location **/
    private Rectangle imageArea;
    //The properties of the object
    private Map<String,String> attributes;
    //This is where the game object is first located when the game starts
    protected Point startingLocation;
    //-- Reinstate if needed --
    //Set to true once this object has been drawn.  Used by objects that never change in appearance during the game
    //protected boolean staticObjectsDontNeedToBeDrawnAgain;


    public GameObject(Rectangle area, Map<String,String> attributes) {
        this.imageArea = area;

        this.attributes = attributes;
        startingLocation = area.getLocation();
    }

    protected abstract void paintObject(Graphics g);

    public final void doPainting(Graphics g) {
        paintObject(g);

        //----Reinstate if needed----
        //Set to true if the painting is part of the actual game where these types of object won't change.  Otherwise don't
        //e.g. if the painting is part of the editor where these objects can be moved
        //staticObjectsDontNeedToBeDrawnAgain = CommonUtils.gameIsRunning;
    }

    public Rectangle getImageArea() {
        return imageArea;
    }

    public void setImageArea(Rectangle imageArea) {
        this.imageArea = imageArea;
    }

    public Map<String,String> getAttributes() {
        return attributes;
    }

    public int getHeight() {
        return imageArea.height;
    }

    public int getWidth() {
        return imageArea.width;
    }

    public int getX() {
        return imageArea.x;
    }

    public int getY() {
        return imageArea.y;
    }

    public void setLocation(int x, int y) {
        Rectangle r = getImageArea();
        r.x = x;
        r.y = y;
    }

    public Point getLocation() {
        return imageArea.getLocation();
    }
}
