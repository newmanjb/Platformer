package com.noomtech.platformscreen.utils;


import com.noomtech.platformscreen.gameobjects.GameObject;

/**
 * Contains useful methods
 * @author Joshua Newman
 */
public class Utils {


    /**
     * Returns true if there has NOT been a collision between the first param and any of the game objects in the second param
     * @param gameObject The game object to examine for a collision
     * @param possiblyCollidedWithThese The objects to check for collision with the first param e.g. a {@link com.noomtech.platformscreen.gameobjects.Platform}
     *                             or {@link com.noomtech.platformscreen.gameobjects.Nasty}
     * @return True if there has NOT been a collision
     */
    public static boolean checkNotCollidedWhileMovingUpOrDown(GameObject gameObject, GameObject[] possiblyCollidedWithThese) {
        if(possiblyCollidedWithThese != null) {
            for (GameObject possiblyCollidedWith : possiblyCollidedWithThese) {
                if (gameObject != possiblyCollidedWith) {
                    int lhsOfJsw = gameObject.getX();
                    int rhsOfJsw = lhsOfJsw + gameObject.getWidth();
                    int lhsOfCp = possiblyCollidedWith.getX();
                    int rhsOfCp = lhsOfCp + possiblyCollidedWith.getWidth();
                    if(!(rhsOfJsw < lhsOfCp || lhsOfJsw > rhsOfCp)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns true if there has NOT been a collision between the first param and any of the game objects in the second param
     * @param gameObject The game object to examine for a collision
     * @param possiblyCollidedWithThese The objects to check for collision with the first param e.g. a {@link com.noomtech.platformscreen.gameobjects.Platform}
     *                             or {@link com.noomtech.platformscreen.gameobjects.Nasty}
     * @return True if there has NOT been a collision
     */
    public static boolean checkNotCollidedWhileMovingAcross(GameObject gameObject, GameObject possiblyCollidedWithThese[]) {
        if(possiblyCollidedWithThese != null) {
            for (GameObject possiblyCollidedWith : possiblyCollidedWithThese) {
                if (gameObject != possiblyCollidedWith) {
                    int topOfGameObject = gameObject.getY();
                    int bottomOfGameObject = topOfGameObject + gameObject.getHeight();
                    int topOfCp = possiblyCollidedWith.getY();
                    int bottomOfCp = topOfCp + possiblyCollidedWith.getHeight();
                    if(!(bottomOfGameObject < topOfCp || topOfGameObject > bottomOfCp)) {
                        return false;
                    }
                }
            }
        }
        return true;
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
