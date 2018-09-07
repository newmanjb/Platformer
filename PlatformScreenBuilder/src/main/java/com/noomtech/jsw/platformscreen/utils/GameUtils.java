package com.noomtech.jsw.platformscreen.utils;


import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.platformscreen.gameobjects.GameObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Contains useful methods
 * @author Joshua Newman
 */
public class GameUtils {


    /**See {@link #getScaledMsToScreenWidthValue(BigDecimal)}**/
    private final static BigDecimal BASE_ON_THIS_SCREEN_WIDTH = new BigDecimal("1920");
    /**See {@link #getScaledMsToScreenHeightValue(BigDecimal)}**/
    private final static BigDecimal BASE_ON_THIS_SCREEN_HEIGHT = new BigDecimal("1080");
    private final static MathContext SCALING_TO_SCREEN_MATH_CONTEXT = new MathContext(15, RoundingMode.HALF_UP);


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

    /**
     * Scale the given time value (in ms) in relation to how large the existing screen width is compared to {@link #BASE_ON_THIS_SCREEN_WIDTH}.
     * The formula used, where val = value provided, base = {@link #BASE_ON_THIS_SCREEN_WIDTH}, actual = existing screen width
     *
     * tempval = base/val
     * result = (base * (base/actual)) / tempval;
     */
    public static int getScaledMsToScreenWidthValue(BigDecimal valueToScale) {
        return scaleForScreenSize(valueToScale, BASE_ON_THIS_SCREEN_WIDTH, CommonUtils.SCREEN_SIZE_WIDTH);
    }

    /**
     * As for {@link #getScaledMsToScreenWidthValue(BigDecimal)} except for height using {@link #BASE_ON_THIS_SCREEN_HEIGHT}
     */
    public static int getScaledMsToScreenHeightValue(BigDecimal valueToScale) {
        return scaleForScreenSize(valueToScale, BASE_ON_THIS_SCREEN_HEIGHT, CommonUtils.SCREEN_SIZE_HEIGHT);
    }


    private static int scaleForScreenSize(BigDecimal valueToScale, BigDecimal baseOnScreenSize, BigDecimal actualScreenSize) {
        BigDecimal valueAsPortionOfBaseVal = baseOnScreenSize.divide(valueToScale, SCALING_TO_SCREEN_MATH_CONTEXT);
        return baseOnScreenSize.multiply(baseOnScreenSize.divide(actualScreenSize, SCALING_TO_SCREEN_MATH_CONTEXT).divide(
                valueAsPortionOfBaseVal, SCALING_TO_SCREEN_MATH_CONTEXT)).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * Scale the given pixel value in relation to how large the existing screen width is compared to {@link #BASE_ON_THIS_SCREEN_WIDTH}.
     * The formula used, where val = value provided, base = {@link #BASE_ON_THIS_SCREEN_WIDTH}, actual = existing screen width
     *
     * tempval = base/val
     * result = actual / tempval;
     */
    public static int getScaledPixelsToScreenWidthValue(BigDecimal valueToScale) {
        return scaleForPixelScreenSize(valueToScale, BASE_ON_THIS_SCREEN_WIDTH, CommonUtils.SCREEN_SIZE_WIDTH);
    }

    /**
     * As for {@link #getScaledPixelsToScreenWidthValue(BigDecimal)} except for height using {@link #BASE_ON_THIS_SCREEN_HEIGHT}
     */
    public static int getScaledPixelsToScreenHeightValue(BigDecimal valueToScale) {
        return scaleForPixelScreenSize(valueToScale, BASE_ON_THIS_SCREEN_HEIGHT, CommonUtils.SCREEN_SIZE_HEIGHT);
    }


    private static int scaleForPixelScreenSize(BigDecimal valueToScale, BigDecimal baseOnScreenSize, BigDecimal actualScreenSize) {
        BigDecimal valueAsPortionOfBaseVal = baseOnScreenSize.divide(valueToScale, SCALING_TO_SCREEN_MATH_CONTEXT);
        return actualScreenSize.divide(valueAsPortionOfBaseVal, SCALING_TO_SCREEN_MATH_CONTEXT).setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
