package com.noomtech.jsw.game.movement;

import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.objects.JSW;

import java.awt.*;
import java.util.List;


/**
 * Responsible for calculating if the collision area of a game object has collided with the collision area of
 * another game object.
 * @see GameObject
 * @see GameObject#getCollisionAreas()
 * @author Joshua Newman
 */
public class CollisionHandler {

    //Each of these arrays below is as long as the screen size (which is currently square).  The relevant boundary of each
    //static object's, e.g. platform or lethal object, collision areas in relation to the direction that the player sprite happens to be moving
    //forms its index in the appropriate array.
    //These arrays are then used by the functionality that controls the sprite movements in order to quickly detect
    //whether they have collided with something e.g.

    //if there is a platform with a collision area (rectangle) at x = 50, y=70 that is 20 wide and 10 high then the left boundary (x=50) is the only thing
    //that another game object's collision area will be able to collide with when moving right, so this rectangle will be placed at index 50 in the
    //array that is checked when something moves right.  The lower boundary at y=80 (70 + 10) will be what the functionality
    //cares about when something is moving up, so the rectangle will be placed at index 80 in the list that is checked when
    //the sprite is moving up the screen.

    //Each object in the array is a pair of the aforementioned boundary and the game object that owns it.  So the game object that
    //has been collided with can easily be found.

    //The API below allows a game object to be specified, the player for example.  The API will
    //use the arrays below to quickly determine if any collision areas from the game object in question are touching any other collision areas,
    //and if so what game object are they owned by.

    //So in summary this class holds the boundaries of all the collision areas of all the static objects in the game, and also
    //knows what static objects each of these collision area boundaries are part of.  Using this data it can quickly determine if
    //any given game object's collision areas are touching any of these boundaries and which game object e.g. a platform or static lethal object,
    //the collision area being touched is part of.

    private final CollisionAreaToGameObject[][] goingRightCaresAbout;
    private final CollisionAreaToGameObject[][] goingLeftCaresAbout;
    private final CollisionAreaToGameObject[][] goingUpCaresAbout;
    private final CollisionAreaToGameObject[][] goingDownCaresAbout;

    //The player in the game
    private final JSW jsw;


    /**
     * @param staticObjects List of objects in the game that the player can collide with and which do not themselves move e.g.
     *                      platforms, non-moving lethal objects etc..  The order of the list is not important
     * @param screenSize The game window is square so this one number is its size in pixels
     */
    public CollisionHandler(List<GameObject> staticObjects, int screenSize, JSW jsw) {

        goingRightCaresAbout = new CollisionAreaToGameObject[screenSize][];
        goingLeftCaresAbout = new CollisionAreaToGameObject[screenSize][];
        goingUpCaresAbout = new CollisionAreaToGameObject[screenSize][];
        goingDownCaresAbout = new CollisionAreaToGameObject[screenSize][];

        //Build the above arrays
        for(GameObject g : staticObjects) {
            Rectangle[] collisionAreasToAdd = g.getCollisionAreas();
            for(Rectangle collisionAreaToAdd : collisionAreasToAdd) {
                Point p = collisionAreaToAdd.getLocation();
                goingLeftCaresAbout[p.x + collisionAreaToAdd.width - 1] = populateBoundaries(goingLeftCaresAbout[p.x +
                        collisionAreaToAdd.width - 1], collisionAreaToAdd, g);
                goingRightCaresAbout[p.x] = populateBoundaries(goingRightCaresAbout[p.x],
                        collisionAreaToAdd, g);
                goingUpCaresAbout[p.y + collisionAreaToAdd.height - 1] = populateBoundaries(goingUpCaresAbout[p.y +
                        collisionAreaToAdd.height - 1], collisionAreaToAdd, g);
                goingDownCaresAbout[p.y] = populateBoundaries(goingDownCaresAbout[p.y], collisionAreaToAdd, g);
            }
        }

        this.jsw = jsw;
    }

    private static CollisionAreaToGameObject[] populateBoundaries(CollisionAreaToGameObject[] existing, Rectangle collisionAreaToAdd, GameObject gameObjectToAdd) {
        CollisionAreaToGameObject[] newCAtoGO;
        if(existing == null) {
            newCAtoGO = new CollisionAreaToGameObject[1];
            newCAtoGO[0] = new CollisionAreaToGameObject(collisionAreaToAdd, gameObjectToAdd);
        }
        else {
            newCAtoGO = new CollisionAreaToGameObject[existing.length+1];
            System.arraycopy(existing, 0, newCAtoGO, 0, existing.length);
                newCAtoGO[existing.length] = new CollisionAreaToGameObject(collisionAreaToAdd, gameObjectToAdd);
        }
        return newCAtoGO;
    }


    /**
     * Determine if the RHS of the given game object is touching anything
     */
    public GameObject checkIfTouchingAnythingGoingRight(GameObject go) {
        int rhsOfGo = go.getX() + go.getWidth() - 1;
        if(rhsOfGo > goingRightCaresAbout.length - 1) {
            throw new IllegalStateException("RHS of game object is out of bounds");
        }

        Rectangle[] goCollisionAreas = go.getCollisionAreas();
        for(Rectangle goCollisionArea : goCollisionAreas) {

            if(goCollisionArea.x + goCollisionArea.width - 1 != goingRightCaresAbout.length - 1) {
                GameObject touching = checkIfAnythingIntersectedWhenMovingAcross(go, goCollisionArea,
                        goingRightCaresAbout[goCollisionArea.x + goCollisionArea.width]);
                if(touching != null) {
                    return touching;
                }
            }
        }
        return null;
    }

    /**
     * Determine if the LHS of the given game object is touching anything
     */
    public GameObject checkIfTouchingAnythingGoingLeft(GameObject go) {
        if(go.getX() < 0) {
            throw new IllegalStateException("LHS of game object is out of bounds");
        }

        Rectangle[] goCollisionAreas = go.getCollisionAreas();
        for(Rectangle goCollisionArea : goCollisionAreas) {

            if(goCollisionArea.x != 0) {
                GameObject touching = checkIfAnythingIntersectedWhenMovingAcross(go, goCollisionArea,
                        goingLeftCaresAbout[goCollisionArea.x - 1]);
                if (touching != null) {
                    return touching;
                }
            }
        }
        return null;
    }

    /**
     * Determine if the top of the given game object is touching anything
     */
    public GameObject checkIfTouchingAnythingGoingUp(GameObject go) {

        if(go.getY() < 0) {
            throw new IllegalStateException("Top of game object is out of bounds");
        }

        Rectangle[] goCollisionAreas = go.getCollisionAreas();
        for(Rectangle goCollisionArea : goCollisionAreas) {
            if(goCollisionArea.y != 0) {
                GameObject touching = checkIfAnythingIntersectedWhenMovingUpOrDown(go, goCollisionArea,
                        goingUpCaresAbout[goCollisionArea.y - 1]);
                if(touching != null) {
                    return touching;
                }
            }
        }
        return null;
    }

    /**
     * Determine if the bottom the given game object is touching anything
     */
    public GameObject checkIfTouchingAnythingGoingDown(GameObject go) {
        int bottomOfGo = go.getY() + go.getHeight() - 1;
        if(bottomOfGo > goingDownCaresAbout.length - 1) {
            throw new IllegalStateException("Bottom of game object is out of bounds");
        }

        Rectangle[] goCollisionAreas = go.getCollisionAreas();
        for(Rectangle goCollisionArea : goCollisionAreas) {
            if(goCollisionArea.y + goCollisionArea.height - 1 == goingDownCaresAbout.length - 1) {
                return null;
            }
            GameObject touching = checkIfAnythingIntersectedWhenMovingUpOrDown(go, goCollisionArea,
                    goingDownCaresAbout[goCollisionArea.y + goCollisionArea.height]);
            if(touching != null) {
                return touching;
            }
        }
        return null;
    }

    //Has a moving object hit the JSW?
    public boolean hasSomethingHitJSW(GameObject movingGameObject) {
        return jsw.getImageArea().intersects(movingGameObject.getImageArea());
    }


    private GameObject checkIfAnythingIntersectedWhenMovingAcross(GameObject movingGameObject, Rectangle movingCollisionArea, CollisionAreaToGameObject[] collisionAreaToGameObjects) {
        if(collisionAreaToGameObjects != null) {
            for (CollisionAreaToGameObject collisionAreaToGameObject : collisionAreaToGameObjects) {
                GameObject possiblyTouchingThis = collisionAreaToGameObject.getGameObject();
                if(movingGameObject != possiblyTouchingThis) {
                    Rectangle collisionArea_possiblyTouching = collisionAreaToGameObject.getCollisionArea();

                    int topOfMovingCA = movingCollisionArea.y;
                    int bottomOfMovingCA = topOfMovingCA + movingCollisionArea.height - 1;
                    double topOfPT = collisionArea_possiblyTouching.getY();
                    double bottomOfPT = topOfPT + collisionArea_possiblyTouching.getHeight() - 1;
                    if (!(bottomOfMovingCA < topOfPT || topOfMovingCA > bottomOfPT)) {
                        return possiblyTouchingThis;
                    }
                }
            }
        }
        return null;
    }

    private GameObject checkIfAnythingIntersectedWhenMovingUpOrDown(GameObject movingGameObject, Rectangle movingCollisionArea, CollisionAreaToGameObject[] collisionAreaToGameObjects) {
        if(collisionAreaToGameObjects != null) {
            for (CollisionAreaToGameObject collisionAreaToGameObject : collisionAreaToGameObjects) {
                GameObject possiblyTouchingThis = collisionAreaToGameObject.getGameObject();
                if(movingGameObject != possiblyTouchingThis) {
                    Rectangle collisionArea_possiblyTouching = collisionAreaToGameObject.getCollisionArea();
                    int lhsOfMovingCA = movingCollisionArea.x;
                    int rhsOfMovingCA = lhsOfMovingCA + (movingCollisionArea.width - 1);
                    double lhsOfPT = collisionArea_possiblyTouching.getX();
                    double rhsOfOT = lhsOfPT + (collisionArea_possiblyTouching.getWidth() - 1);
                    if (!(rhsOfMovingCA < lhsOfPT || lhsOfMovingCA > rhsOfOT)) {
                        return possiblyTouchingThis;
                    }
                }
            }
        }
        return null;
    }
}
