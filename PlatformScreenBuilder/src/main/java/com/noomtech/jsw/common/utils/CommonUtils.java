package com.noomtech.jsw.common.utils;

import com.noomtech.jsw.game.gameobjects.objects.JSW;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.Point;
import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class CommonUtils {


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

    private static final String ANIMATION_FRAMES_FOLDER = CommonUtils.class.getClassLoader().getResource("animationFrames").getFile() +
            File.separator;
    private static final String STATIC_OBJECT_IMAGES_FOLDER = CommonUtils.class.getClassLoader().getResource(
            "imagesForStaticObjects").getFile() + File.separator;
    private static final String IMAGE_FILE_PREFIX = "___";

    public static volatile boolean gameIsRunning;


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
        converted.x = startX.multiply(SCREEN_SIZE_WIDTH, MC_CONVERT_FROM_FRACTION_ROUNDING).setScale(0, RoundingMode.HALF_UP).intValue();
        converted.y = startY.multiply(SCREEN_SIZE_HEIGHT, MC_CONVERT_FROM_FRACTION_ROUNDING).setScale(0, RoundingMode.HALF_UP).intValue();
        converted.width = endX.subtract(startX).multiply(SCREEN_SIZE_WIDTH, MC_CONVERT_FROM_FRACTION_ROUNDING).setScale(0, RoundingMode.HALF_UP).intValue();
        converted.height = endY.subtract(startY).multiply(SCREEN_SIZE_HEIGHT, MC_CONVERT_FROM_FRACTION_ROUNDING).setScale(0, RoundingMode.HALF_UP).intValue();
        return converted;
    }

    /**
     * Searches the {@link #ANIMATION_FRAMES_FOLDER} directory, which should be on the classpath, for the images files
     * corresponding to the given animation categories for the given animation directory.  Only files who's names start with
     * {@link #IMAGE_FILE_PREFIX} will be returned in the map.  All others will be ignored.
      * @param animationDirectoryName The root directory for the game object's images.  This should be directly under
     *                               the {@link CommonUtils#ANIMATION_FRAMES_FOLDER}
     * @param animationCategories The names of the animation categories that the files are being collected for.  Each animation
     *                            category name must correspond to a directory under the animation directory name that is provided
     *                            for the first parameter.
     * @return A map containing lists of Files keyed under the name of the animation category directory they were found in e.g.
     *         {"Left"->[FileL1,FileL2],"Right"->[filer1,filer2},"Jump"->[FileJ1,FileJ2,FileJ3]}.  These files must be named such
     *         that, when the names are ordered in DESC order, they correspond to the animation cycle e.g. if the frames for
     *         the moving left cycle are image_ghs.jpg, image_23te.jpg and imagee_d2h.jpg in that order then they should be renamed to
     *         image_1.jpg, image_2.jpg and image_3.jpg respectively, so image_1.jpg is the first frame displayed in the cycle
     *         and image_3.jpg is the last.
     */
    public static Map<String,File[]> getAnimationImages(String animationDirectoryName, String[] animationCategories) {
        File f = new File(ANIMATION_FRAMES_FOLDER + animationDirectoryName);
        if(!f.exists() || !f.isDirectory()) {
            throw new IllegalArgumentException("Invalid file for " + f.getPath());
        }

        File[] categoryDirectories = f.listFiles();
        if(categoryDirectories.length == 0) {
            throw new IllegalArgumentException("No files in " + f.getPath());
        }

        Map<String, File[]> animCategoryToFileList = new HashMap<>(categoryDirectories.length);
        String animCategoriesDirPath = f.getPath();
        for(String animCategory : animationCategories) {
            File[] animFrameFiles = getImagesFromAbsolutePath(animCategoriesDirPath + File.separator + animCategory);
            animCategoryToFileList.put(animCategory, animFrameFiles);
        }

        return animCategoryToFileList;
    }

    /**
     * Returns the files under {@link #STATIC_OBJECT_IMAGES_FOLDER}  + {@link File#separator} +
     * directory name provided.  The file names must be prefixed with {@link #IMAGE_FILE_PREFIX}.  Any that are not
     * are ignored.
     * The {@link #STATIC_OBJECT_IMAGES_FOLDER} should be on the classpath.
     */
    public static File[] getImagesForStaticObjects(String directoryName) {
        return getImagesFromAbsolutePath(STATIC_OBJECT_IMAGES_FOLDER + directoryName);
    }

    private static File[] getImagesFromAbsolutePath(String absolutePath) {
        File directoryFile = new File(absolutePath);
        if(!directoryFile.exists() || !directoryFile.isDirectory()) {
            throw new IllegalArgumentException(directoryFile.getPath() + " is invalid");
        }

        File[] filteredFiles = Arrays.stream(directoryFile.listFiles()).filter(file -> {return file.getName().startsWith("___");}).collect(Collectors.toList()).toArray(new File[]{});
        if(filteredFiles.length == 0) {
            throw new IllegalArgumentException("No files starting with '" + IMAGE_FILE_PREFIX + "' in " + directoryFile.getPath());
        }
        Arrays.sort(filteredFiles, (a,b)->{return a.getName().compareTo(b.getName());});

        return filteredFiles;

    }

    /**
     * Add the new image files to the given image directory.  The old files starting with {@link #IMAGE_FILE_PREFIX} will have the
     * prefix replaced in their names, and the new files will have the prefix added to their names.
     * @param destDirectory
     * @param newFiles
     */
    public static void addNewFilesToImageDir(File destDirectory, File[] newFiles) {
        List<File> filteredFilesList = Arrays.stream(destDirectory.listFiles()).filter(file -> {return file.getName().startsWith("___");}).collect(Collectors.toList());

        if(filteredFilesList.size() > 0) {
            for(File f : filteredFilesList) {
                f.renameTo(new File(f.getParent() + File.separator + f.getName().replace(IMAGE_FILE_PREFIX, "")));
            }
        }

        for(File f : newFiles) {
            f.renameTo(new File(destDirectory.getPath() + File.separator + IMAGE_FILE_PREFIX + f.getName()));
        }
    }
}
