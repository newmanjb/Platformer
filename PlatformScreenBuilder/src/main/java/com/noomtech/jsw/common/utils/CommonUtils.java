package com.noomtech.jsw.common.utils;

import com.noomtech.jsw.game.gameobjects.objects.JSW;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CommonUtils {


    //Paths related to the config directory
    private static final String CONFIG_FOLDER_PATH = System.getProperty("config");
    private static final File CONFIG_FOLDER_FILE = new File(CONFIG_FOLDER_PATH);

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

    //Each image file that is used must start with this prefix otherwise it will be ignored
    private static final String IMAGE_FILE_PREFIX = "___";

    //The image directories.  These will all be in the config.

    public static final File ANIMATION_FRAMES_FOLDER_FILE = new File(CONFIG_FOLDER_PATH + File.separator + "animationFrames");
    private static final String ANIMATION_FRAMES_FOLDER_STRING = ANIMATION_FRAMES_FOLDER_FILE.getAbsolutePath() +
            File.separator;
    public static final File STATIC_OBJECT_IMAGES_FOLDER_FILE = new File(CONFIG_FOLDER_PATH + File.separator + "imagesForStaticObjects");
    private static final String STATIC_OBJECT_IMAGES_FOLDER_STRING = STATIC_OBJECT_IMAGES_FOLDER_FILE.getAbsolutePath() + File.separator;

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
     * Searches the {@link #ANIMATION_FRAMES_FOLDER_STRING} directory for the images files
     * corresponding to the given animation categories for the given animation directory.  Only files who's names start with
     * {@link #IMAGE_FILE_PREFIX} will be returned in the map.  All others will be ignored.
      * @param animationDirectoryName The root directory for the game object's images.  This should be directly under
     *                               the {@link CommonUtils#ANIMATION_FRAMES_FOLDER_STRING}
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
        File f = new File(ANIMATION_FRAMES_FOLDER_STRING + animationDirectoryName);
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
     * Returns the files under {@link #STATIC_OBJECT_IMAGES_FOLDER_STRING}  + {@link File#separator} +
     * directory name provided.  The file names must be prefixed with {@link #IMAGE_FILE_PREFIX}.  Any that are not
     * are ignored.
     */
    public static File[] getImagesForStaticObjects(String directoryName) {
        return getImagesFromAbsolutePath(STATIC_OBJECT_IMAGES_FOLDER_STRING + directoryName);
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
     * Add the new image files to the given image directory.  Any existing files in the directory will be deleted
     * and the new files will have the {@link #IMAGE_FILE_PREFIX} added to the start of their names.
     * @param chosenDestDir
     * @param newFiles
     */
    public static void changeFilesInImageDir(File chosenDestDir, File ... newFiles) throws IOException  {

        File[] filesToDelete = chosenDestDir.listFiles();
        for(File toDelete : filesToDelete) {
            Files.delete(Paths.get(toDelete.toURI()));
        }

        for(File newFile : newFiles) {
            Files.copy(Paths.get(newFile.toURI()), Paths.get( chosenDestDir.getAbsolutePath() +
                    File.separator + IMAGE_FILE_PREFIX + newFile.getName()));
        }
    }

    public static String getAttribute(Map<String,String> attributes, String key, String defaultVal) {
        String existing = attributes.get(key);
        if(existing == null) {
            if(defaultVal == null) {
                throw new IllegalStateException("Could not find attribute under key: " + key);
            }
            return defaultVal;
        }
        return existing;
    }
}
