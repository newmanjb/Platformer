package com.noomtech.jsw.game.gameobjects;

import com.noomtech.jsw.game.movement.CollisionHandler;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;


/**
 * Represents an object on the screen e.g. player, platform.
 * @author Joshua Newman
 */
public abstract class GameObject {


    /** A rectangle representing the boundaries and location of the object's image.  This is used to define its location **/
    private Rectangle imageArea;
    //The properties of the object
    private Map<String,String> attributes;
    //This is where the game object is first located when the game starts
    protected Point startingLocation;
    //-- Reinstate if needed --
    //Set to true once this object has been drawn.  Used by objects that never change in appearance during the game
    //protected boolean staticObjectsDontNeedToBeDrawnAgain;

    //Holds the collision areas associated with this game object.  The collision areas are the areas that if
    //intersected by another object's colllision area mean that a collision between the 2 objects has occurred.
    protected Rectangle[] collisionAreas;
    //Delegate that handles all calculations associated with determining if a collision has occurred.
    protected CollisionHandler collisionHandler;


    public GameObject(Rectangle imageArea, Map<String,String> attributes) {
        if(attributes == null) {
            throw new IllegalArgumentException("Null attributes not allowed.  Please specify an empty map");
        }

        this.imageArea = imageArea;

        this.attributes = attributes;
        startingLocation = imageArea.getLocation();

        //Default the collision areas to just be the entire area of the game object's image
        this.collisionAreas = new Rectangle[]{imageArea};

        collisionHandler = new CollisionHandler(this);
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

    public Rectangle[] getCollisionAreas() {
        return collisionAreas;
    }

    public void setCollisionAreas(Rectangle[] newCollisionAreas) {
        this.collisionAreas = newCollisionAreas;
        collisionHandler.buildCollisionAreasFromHost();
    }

    public void setLocation(int x, int y) {
        Rectangle r = getImageArea();
        r.x = x;
        r.y = y;
    }

    public Point getLocation() {
        return imageArea.getLocation();
    }

    public GameObject checkIfBottomIsTouching(GameObject[][] relevantBoundaries) {
        return collisionHandler.checkIfTouchingAnythingGoingDown(relevantBoundaries);
    }

    public GameObject checkIfRHSIsTouching(GameObject[][] relevantBoundaries) {
        return collisionHandler.checkIfTouchingAnythingGoingRight(relevantBoundaries);
    }

    public GameObject checkIfLHSIsTouching(GameObject[][] relevantBoundaries) {
        return collisionHandler.checkIfTouchingAnythingGoingLeft(relevantBoundaries);
    }

    public GameObject checkIfTopIsTouching(GameObject[][] relevantBoundaries) {
        return collisionHandler.checkIfTouchingAnythingGoingUp(relevantBoundaries);
    }

    @Override
    public String toString() {
        return "GameObject{" +
                "imageArea=" + imageArea +
                ", attributes=" + attributes +
                ", startingLocation=" + startingLocation +
                ", collisionAreas=" + Arrays.toString(collisionAreas) +
                ", collisionHandler=" + collisionHandler +
                '}';
    }
}
