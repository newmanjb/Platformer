package com.noomtech.jsw.game.movement;

import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gamethread.GamePanel;

import java.awt.*;


/**
 * Has a 1-1 relationship with a game object (referred to as the "host").  This is responsible for calculating if the game object has collided with
 * a given object.
 * @see GameObject#checkIfBottomIsTouching(GameObject[][])
 * @see GameObject#checkIfTopIsTouching(GameObject[][])
 * @see GameObject#checkIfLHSIsTouching(GameObject[][])
 * @see GameObject#checkIfRHSIsTouching(GameObject[][])
 * @see GamePanel#goingDownCaresAbout
 * @see GamePanel#goingUpCaresAbout
 * @see GamePanel#goingLeftCaresAbout
 * @see GamePanel#goingRightCaresAbout
 * @author Joshua Newman, NChain Ltd, October 2018
 */
public class CollisionHandler {


    private GameObject host;

    //These arrays are built from the game object's collision areas and work on the same principal as their equivalents
    //in the main game panel, except they cover the area within this game object.  When an object is detected to have
    //touched the boundary of the host game object for this class (defined by the area covered by its image) it means
    //that the object could potentially have collided with any of these collision areas, so these arrays are used to
    //determine that.
    private Rectangle[][] goingUpCaresAbout;
    private Rectangle[][] goingDownCaresAbout;
    private Rectangle[][] goingLeftCaresAbout;
    private Rectangle[][] goingRightCaresAbout;


    public CollisionHandler(GameObject host) {
        this.host = host;
        buildCollisionAreasFromHost();
    }


    //Builds the boundary arrays defined above using the rectangles of the host's collision areas
    public void buildCollisionAreasFromHost() {
        Rectangle[] collisionAreas = host.getCollisionAreas();
        int hostHeight = host.getHeight();
        int hostWidth = host.getWidth();

        goingUpCaresAbout = new Rectangle[hostHeight][];
        goingDownCaresAbout = new Rectangle[hostHeight][];
        goingLeftCaresAbout = new Rectangle[hostWidth][];
        goingRightCaresAbout = new Rectangle[hostWidth][];
        for(Rectangle collisionArea : collisionAreas) {
            addCollisionAreaToBoundaryLines(collisionArea, collisionArea.x, goingRightCaresAbout, true);
            addCollisionAreaToBoundaryLines(collisionArea, collisionArea.x + (hostWidth - 1), goingLeftCaresAbout, true);
            addCollisionAreaToBoundaryLines(collisionArea, collisionArea.y, goingDownCaresAbout, false);
            addCollisionAreaToBoundaryLines(collisionArea, collisionArea.y + (hostHeight - 1), goingUpCaresAbout, false);
        }
    }


    private void addCollisionAreaToBoundaryLines(Rectangle collisionArea, int ordinate, Rectangle[][] boundaryLinesToPopulate, boolean across) {

        int ordinateInRelationToArea = ordinate - (across ? host.getX() : host.getY());

        Rectangle[] existingCollisionBoundaries = boundaryLinesToPopulate[ordinateInRelationToArea];
        if(existingCollisionBoundaries != null) {
            Rectangle[] dest = new Rectangle[existingCollisionBoundaries.length + 1];
            System.arraycopy(existingCollisionBoundaries, 0, dest, 0, existingCollisionBoundaries.length);
            dest[dest.length - 1] = collisionArea;
            existingCollisionBoundaries = dest;
        }
        else {
            existingCollisionBoundaries = new Rectangle[]{collisionArea};
        }
        boundaryLinesToPopulate[ordinateInRelationToArea] = existingCollisionBoundaries;
    }

    //Determine if the RHS of the host is touching any of the objects provided in the parameters
    public GameObject checkIfTouchingAnythingGoingRight(GameObject[][] objectsInField) {
        int rhsOfHost = host.getX() + host.getWidth() - 1;
        if(rhsOfHost == objectsInField.length - 1) {
            return null;
        }
        else if(rhsOfHost > objectsInField.length - 1) {
            throw new IllegalStateException("RHS of host is out of bounds");
        }
        return checkIfAnythingIntersectedWhenMovingAcross(objectsInField[host.getX() + host.getWidth()]);
    }

    //Determine if the LHS of the host is touching any of the objects provided in the parameters
    public GameObject checkIfTouchingAnythingGoingLeft(GameObject[][] objectsInField) {
        if(host.getX() == 0) {
            return null;
        }
        else if(host.getX() < 0) {
            throw new IllegalStateException("LHS of host is out of bounds");
        }
        return checkIfAnythingIntersectedWhenMovingAcross(objectsInField[host.getX() -1]);
    }

    //Determine if the top of the host is touching any of the objects provided in the parameters
    public GameObject checkIfTouchingAnythingGoingUp(GameObject[][] objectsInField) {
        if(host.getY() == 0) {
            return null;
        }
        else if(host.getY() < 0) {
            throw new IllegalStateException("Top of host is out of bounds");
        }
        return checkIfAnythingIntersectedWhenMovingUpOrDown(objectsInField[host.getY() - 1]);
    }

    //Determine if the bottom of the host is touching any of the objects provided in the parameters
    public GameObject checkIfTouchingAnythingGoingDown(GameObject[][] objectsInField) {
        int bottomOfHost = host.getY() + host.getHeight() - 1;
        if(bottomOfHost == objectsInField.length - 1) {
            return null;
        }
        else if(bottomOfHost > objectsInField.length - 1) {
            throw new IllegalStateException("Bottom of host is out of bounds");
        }
        return checkIfAnythingIntersectedWhenMovingUpOrDown(objectsInField[host.getY() + host.getHeight()]);
    }

    private GameObject checkIfAnythingIntersectedWhenMovingAcross(GameObject[] possiblyTouchingThese) {
        if(possiblyTouchingThese != null) {
            for (GameObject possiblyTouchingThis : possiblyTouchingThese) {
                if (host != possiblyTouchingThis) {
                    int topOfGameObject = host.getY();
                    int bottomOfGameObject = topOfGameObject + host.getHeight() - 1;
                    int topOfCp = possiblyTouchingThis.getY();
                    int bottomOfCp = topOfCp + possiblyTouchingThis.getHeight() - 1;
                    if(!(bottomOfGameObject < topOfCp || topOfGameObject > bottomOfCp)) {
                        return possiblyTouchingThis;
                    }
                }
            }
        }
        return null;
    }

    private GameObject checkIfAnythingIntersectedWhenMovingUpOrDown(GameObject[] possiblyTouchingThese) {
        if(possiblyTouchingThese != null) {
            for (GameObject possiblyTouchingThis : possiblyTouchingThese) {
                if (host != possiblyTouchingThis) {
                    int lhsOfJsw = host.getX();
                    int rhsOfJsw = lhsOfJsw + (host.getWidth() - 1);
                    int lhsOfCp = possiblyTouchingThis.getX();
                    int rhsOfCp = lhsOfCp + (possiblyTouchingThis.getWidth() - 1);
                    if(!(rhsOfJsw < lhsOfCp || lhsOfJsw > rhsOfCp)) {
                        return possiblyTouchingThis;
                    }
                }
            }
        }
        return null;
    }
}
