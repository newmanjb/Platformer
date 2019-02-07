package com.noomtech.jsw.game.gameobjects;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;


/**
 * Represents an object on the screen e.g. player, platform.
 * @author Joshua Newman
 */
public abstract class GameObject {


    //Unique id of this game object
    private long id;
    /** A rectangle representing the boundaries and location of the object's image.  This is used to define its location **/
    private Rectangle imageArea;
    //The properties of the object
    protected Map<String,String> attributes;
    //This is where the game object is first located when the game starts
    protected Point startingLocation;
    //-- Reinstate if needed --
    //Set to true once this object has been drawn.  Used by objects that never change in appearance during the game
    //protected boolean staticObjectsDontNeedToBeDrawnAgain;

    protected Rectangle[] collisionAreas;


    public GameObject(Rectangle imageArea, Map<String,String> attributes, long id) {
        if(attributes == null) {
            throw new IllegalArgumentException("Null attributes not allowed.  Please specify an empty map");
        }

        this.imageArea = imageArea;

        this.attributes = attributes;
        startingLocation = imageArea.getLocation();
        this.id = id;

        //Default the collision areas to just be the entire area of the game object's image
        this.collisionAreas = new Rectangle[]{imageArea};
        onImageUpdated();
    }


    protected abstract void paintObject(Graphics g);

    public abstract void onImageUpdated();

    public final void doPainting(Graphics g) {
        paintObject(g);

        //----Reinstate if needed----
        //Set to true if the painting is part of the actual game where these types of object won't change.  Otherwise don't
        //e.g. if the painting is part of the editor where these objects can be moved
        //staticObjectsDontNeedToBeDrawnAgain = CommonUtils.gameIsRunning;
    }

    public final long getId() {
        return id;
    }

    public Rectangle getImageArea() {
        return imageArea;
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

    /**
     * Returns the game object's "collision areas".  These are rectangles that represent the physical boundaries of the game
     * object.  For example, something rectangular like a platform might only have one collision area, which will be the
     * same as the boundaries of its image, but a more complex sprite with an irregular shape might have many in order
     * to properly define its boundaries.
     * If a collision area from one object is touching the collision are of another object then the two objects are
     * considered as touching each other by the game
     * @return The collision areas of this game object
     * @see com.noomtech.jsw.game.movement.CollisionHandler
     */
    public Rectangle[] getCollisionAreas() {
        return collisionAreas;
    }

    public void setCollisionAreas(Rectangle[] newCollisionAreas) {
        this.collisionAreas = newCollisionAreas;
    }

    public void setLocation(int x, int y) {
        Rectangle r = getImageArea();
        int xDiff = x - r.x;
        int yDiff = y - r.y;
        r.x = x;
        r.y = y;

        for(Rectangle ca : collisionAreas) {
            ca.x += xDiff;
            ca.y += yDiff;
        }
    }

    public Point getLocation() {
        return imageArea.getLocation();
    }

    /**
     * @return The name of the root directory that holds this objects images
     */
    public final String getImageFolderName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "GameObject{" +
                "imageArea=" + imageArea +
                ", attributes=" + attributes +
                ", startingLocation=" + startingLocation +
                ", collisionAreas=" + Arrays.toString(collisionAreas) +
                '}';
    }
}
