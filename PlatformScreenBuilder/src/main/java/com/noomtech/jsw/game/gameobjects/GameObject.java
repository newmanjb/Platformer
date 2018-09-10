package com.noomtech.jsw.game.gameobjects;

import java.awt.*;
import java.util.Map;


/**
 * Represents an object on the screen e.g. player, platform
 * @author Joshua Newman
 */
public abstract class GameObject {


    /** The boundaries of the object's image.  This is used to define its location **/
    private Rectangle area;
    //The properties of the object
    private Map<String,String> attributes;
    //This is where the game object is first located when the game starts
    protected Point startingLocation;
    //-- Reinstate if needed --
    //Set to true once this object has been drawn.  Used by objects that never change in appearance during the game
    //protected boolean staticObjectsDontNeedToBeDrawnAgain;
    /**
     * These define the areas into which other objects have to move into to be considered as "touching" the object.  This may
     * not necessarily be the same as the "area" field, as it depends on the shape of the image that represents the
     * object.  There could be several of these on one object if, for example, the image for the object is a particularly
     * irregular shape as opposed to being squarish.
     */
    protected Rectangle[] collisionAreas;


    public GameObject(Rectangle area, Map<String,String> attributes) {
        this.area = area;
        //Default the collision area to be the area occupied by the entire image
        this.collisionAreas = new Rectangle[]{this.area};
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

    public Rectangle getArea() {
        return area;
    }

    public Rectangle[] getCollisionAreas() {
        return collisionAreas;
    }

    public void setCollisionAreas(Rectangle[] collisionAreas) {
        this.collisionAreas = collisionAreas;
    }

    public void setArea(Rectangle area) {
        this.area = area;
    }

    public Map<String,String> getAttributes() {
        return attributes;
    }

    public int getHeight() {
        return area.height;
    }

    public int getWidth() {
        return area.width;
    }

    public int getX() {
        return area.x;
    }

    public int getY() {
        return area.y;
    }

    public Point getLocation() {
        return area.getLocation();
    }
}
