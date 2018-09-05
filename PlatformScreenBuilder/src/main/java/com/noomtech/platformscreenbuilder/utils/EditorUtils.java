package com.noomtech.platformscreenbuilder.utils;

import com.noomtech.platformscreen.gameobjects.objects.JSW;

import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class EditorUtils {


    public static final String SELECTABLE_GAME_OBJECT_PACKAGE = JSW.class.getPackage().getName();
    public static final String SELECTABLE_GAME_OBJECT_DIRECTORY = SELECTABLE_GAME_OBJECT_PACKAGE.replace(".", File.separator);
    //The list of game objects available for selection in the editor.
    public static final String[] SELECTABLE_GAME_OBJECTS = getGameObjectOptions();
    //The size of the screen
    public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final BigDecimal SCREEN_SIZE_WIDTH = new BigDecimal(SCREEN_SIZE.width);
    public static final BigDecimal SCREEN_SIZE_HEIGHT = new BigDecimal(SCREEN_SIZE.height);

    private static final MathContext MC_CONVERT_TO_FRACTION_ROUNDING = new MathContext(15,RoundingMode.HALF_UP);
    private static final MathContext MC_CONVERT_FROM_FRACTION_ROUNDING = new MathContext(15,RoundingMode.HALF_UP);


    private static String[] getGameObjectOptions() {
        
        //Go through the files in the game object package and construct each one's full name
        
        String classDirectory = new File(JSW.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getPath();
        String packagePath = new StringBuilder(classDirectory).append(
                File.separator).append(SELECTABLE_GAME_OBJECT_DIRECTORY).toString();
        File[] classNames = new File(packagePath).listFiles();
        String[] gameObjectClasses = new String[classNames.length];
        for (int i = 0; i < gameObjectClasses.length; i++) {
            gameObjectClasses[i] = classNames[i].getName().subSequence(0, classNames[i].getName().indexOf(".")).toString();
        }
        return gameObjectClasses;
    }

    /**
     * Convertes the width and location of the given rectangle to proportions of the screen size.
     * @return An array containing the converted values as follows [X,Y,WIDTH,HEIGHT]
     */
    public static BigDecimal[] convertToProportionOfScreenSize(Rectangle rectangleToConvert) {
        BigDecimal[] convertedVals = new BigDecimal[4];
        Point location = rectangleToConvert.getLocation();
        convertedVals[0] = new BigDecimal(location.x).divide(SCREEN_SIZE_WIDTH, MC_CONVERT_TO_FRACTION_ROUNDING);
        convertedVals[1] = new BigDecimal(location.y).divide(SCREEN_SIZE_HEIGHT, MC_CONVERT_TO_FRACTION_ROUNDING);
        convertedVals[2] = new BigDecimal(rectangleToConvert.width).divide(SCREEN_SIZE_WIDTH, MC_CONVERT_TO_FRACTION_ROUNDING);
        convertedVals[3] = new BigDecimal(rectangleToConvert.height).divide(SCREEN_SIZE_HEIGHT, MC_CONVERT_TO_FRACTION_ROUNDING);

        return convertedVals;
    }

    /**
     * The reverse of {@link #convertToProportionOfScreenSize(Rectangle)}
     */
    public static Rectangle convertFromProportionOfScreenSize(BigDecimal startX, BigDecimal startY, BigDecimal endX, BigDecimal endY) {
        Rectangle converted = new Rectangle();
        converted.x = startX.multiply(SCREEN_SIZE_WIDTH, MC_CONVERT_FROM_FRACTION_ROUNDING).toBigInteger().intValue();
        converted.y = startY.multiply(SCREEN_SIZE_HEIGHT, MC_CONVERT_FROM_FRACTION_ROUNDING).toBigInteger().intValue();
        converted.width = endX.subtract(startX).multiply(SCREEN_SIZE_WIDTH, MC_CONVERT_FROM_FRACTION_ROUNDING).toBigInteger().intValue();
        converted.height = endY.subtract(startY).multiply(SCREEN_SIZE_HEIGHT, MC_CONVERT_FROM_FRACTION_ROUNDING).toBigInteger().intValue();
        return converted;
    }
}
