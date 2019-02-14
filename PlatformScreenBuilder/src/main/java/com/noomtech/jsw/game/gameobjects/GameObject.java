package com.noomtech.jsw.game.gameobjects;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.game.handlers.CollisionHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents an object on the screen e.g. player, platform.
 * A game object can have one or more states.  Each state corresponds to what it's currently
 * doing e.g. the player could be walking left or walking right.  Static objects like platforms only have one state,
 * since they are technically only ever doing one thing i.e. just sitting there (see {@link com.noomtech.jsw.game.gameobjects.IdleGameObject}.
 * The states are used, among other things, to paint the appropriate image for the object on the screen e.g. if the player is
 * in the "walking left" state then the game knows to use the animation frames representing the player walking left.
 *
 * The images for a game object's state are stored in the images directory in the config under a directory named after the
 * game object's class.  There be separate subdirectories for each state e.g. for the player moving left they could be
 * in "C:/game/my_config/images/JSW/left", and for the player moving right they could be in "C:/game/my_config/images/JSW/right"
 *
 * @see GameObjectStateFrame
 * @see com.noomtech.jsw.game.gameobjects.IdleGameObject
 * @see XorYMovingGameObject
 * @see com.noomtech.jsw.game.gameobjects.concrete_objects.JSW
 * @author Joshua Newman
 */
public abstract class GameObject {


    //Unique id of this game object
    private long id;
    //A rectangle representing the boundaries and location of the object's image.  This is used to define its location
    private Rectangle imageArea;
    //The properties of the object
    protected Map<String,String> attributes;
    //This is where the game object is first located when the game starts
    protected Point startingLocation;
    //Stores lists of the frames appertaining to each state
    protected Map<String, GameObjectStateFrame[]> stateNameToStateObjectMap;

    protected Rectangle[] collisionAreas;


    public GameObject(Rectangle imageArea, Map<String,String> attributes, long id) {
        if(attributes == null) {
            throw new IllegalArgumentException("Null attributes not allowed.  Please specify an empty map");
        }

        this.imageArea = imageArea;
        this.attributes = attributes;
        startingLocation = imageArea.getLocation();
        this.id = id;

        //Default the collision areas to be the entire area of the game object's image
        this.collisionAreas = new Rectangle[]{imageArea};
        onImageUpdated();
    }


    protected abstract void paintObject(Graphics g);

    //Build the map containing lists of state frames stored under their state names
    public final void onImageUpdated() {
        try {
            String imageDir = getImageFolderName();
            String[] stateNames = getGameObjectStateNames();
            //This method obtains the files for each image and they are then converted to image objects that are used in the state frames
            //when the map is built below
            Map<String, File[]> statesToFiles = CommonUtils.getGameObjectStateImages(imageDir, stateNames, getId());

            stateNameToStateObjectMap = new HashMap(statesToFiles.size());
            for (Map.Entry<String, File[]> entries : statesToFiles.entrySet()) {
                File[] files = entries.getValue();
                GameObjectStateFrame[] theGameObjectStates = new GameObjectStateFrame[files.length];
                for (int i = 0; i < theGameObjectStates.length; i++) {
                    BufferedImage b = ImageIO.read(files[i]);
                    theGameObjectStates[i] = (g, r) -> {
                        g.drawImage(b, r.x, r.y, r.width, r.height, Color.WHITE, null);
                    };
                }
                stateNameToStateObjectMap.put(entries.getKey(), theGameObjectStates);
            }

            refreshAfterImageUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Could not set attributes", e);
        }
    }

    public final void doPainting(Graphics g) {
        paintObject(g);
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
     * @see CollisionHandler
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

    /**
     * @return The name of the root directory that holds this object's state subdirectories
     */
    public final String getImageFolderName() {
        return getClass().getSimpleName();
    }

    /**
     * @return The names of all the possible states for this game object
     */
    public abstract String[] getGameObjectStateNames();

    protected abstract void refreshAfterImageUpdate();

    @Override
    public String toString() {
        return "GameObject{" +
                "id=" + id +
                ", imageArea=" + imageArea +
                ", attributes=" + attributes +
                ", startingLocation=" + startingLocation +
                ", stateNameToStateObjectMap=" + stateNameToStateObjectMap +
                ", collisionAreas=" + Arrays.toString(collisionAreas) +
                '}';
    }
}
