package com.noomtech.platformscreen.utils;


import com.noomtech.platformscreen.gameobjects.GameObject;

/**
 * Contains useful methods
 * @author Joshua Newman
 */
public class Utils {


    /**
     * Checks if the object in the first param has collided with any of the objects in the second param while moving up or down.
     * @param gameObject The game object that may have collided with any of the game objects in the given array
     * @param possiblyCollidedWithThese The objects to check for collision with the first param e.g. these could include platforms or lethal objects
     * @return The object that has been oollided with or null if there has been no collision
     */
    public static GameObject getCollidedWhileMovingUpOrDown(GameObject gameObject, GameObject[] possiblyCollidedWithThese) {
        if(possiblyCollidedWithThese != null) {
            for (GameObject possiblyCollidedWith : possiblyCollidedWithThese) {
                if (gameObject != possiblyCollidedWith) {
                    int lhsOfJsw = gameObject.getX();
                    int rhsOfJsw = lhsOfJsw + gameObject.getWidth();
                    int lhsOfCp = possiblyCollidedWith.getX();
                    int rhsOfCp = lhsOfCp + possiblyCollidedWith.getWidth();
                    if(!(rhsOfJsw < lhsOfCp || lhsOfJsw > rhsOfCp)) {
                        return possiblyCollidedWith;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if the object in the first param has collided with any of the objects in the second param while moving left or right.
     * @param gameObject The game object that may have collided with any of the game objects in the given array
     * @param possiblyCollidedWithThese The objects to check for collision with the first param e.g. these could include platforms or lethal objects
     * @return The object that has been oollided with or null if there has been no collision
     */
    public static GameObject getCollidedWhileMovingAcross(GameObject gameObject, GameObject possiblyCollidedWithThese[]) {
        if(possiblyCollidedWithThese != null) {
            for (GameObject possiblyCollidedWith : possiblyCollidedWithThese) {
                if (gameObject != possiblyCollidedWith) {
                    int topOfGameObject = gameObject.getY();
                    int bottomOfGameObject = topOfGameObject + gameObject.getHeight();
                    int topOfCp = possiblyCollidedWith.getY();
                    int bottomOfCp = topOfCp + possiblyCollidedWith.getHeight();
                    if(!(bottomOfGameObject < topOfCp || topOfGameObject > bottomOfCp)) {
                        return possiblyCollidedWith;
                    }
                }
            }
        }
        return null;
    }

    public static void sleepAndCatchInterrupt(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch(InterruptedException e) {
            System.out.println("Should not happen");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
