package com.noomtech.jsw.common.utils;

import com.noomtech.jsw.game.gameobjects.GameObject;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


public class CommonUtils {

    public static final String PROPERTY_NAME_CONFIG = "config";

    private static final String CONFIG_FOLDER_PATH = System.getProperty(PROPERTY_NAME_CONFIG);

    //The image directory for the current level
    private static File imagesFolderFile;
    private static String imagesFolderString;

    //This is used as the global reference to what level we're on
    public static int getCurrentLevel() {
        return currentLevel;
    }
    public static void setCurrentLevel(int newLevel) {
        currentLevel = newLevel;
        imagesFolderFile = new File(CONFIG_FOLDER_PATH + File.separator + "levels" +
                File.separator + "level" + currentLevel + File.separator + "images");
        imagesFolderString = imagesFolderFile.getAbsolutePath() +
                File.separator;
        currentGameObjectsCollection = "level" + currentLevel;
    }
    private static int currentLevel = System.getProperty("start_at_level") == null ? 1 : Integer.parseInt(System.getProperty("start_at_level"));
    static {
        setCurrentLevel(currentLevel);
    }
    //The collection that holds the game objects for the current level
    public static String currentGameObjectsCollection;

    public static volatile boolean gameIsRunning;

    private static final MathContext MC_CONVERT_TO_FRACTION_ROUNDING = new MathContext(15,RoundingMode.HALF_UP);
    private static final MathContext MC_CONVERT_FROM_FRACTION_ROUNDING = new MathContext(15,RoundingMode.HALF_UP);
    //The size of the screen
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final BigDecimal SCREEN_SIZE_WIDTH = new BigDecimal(SCREEN_SIZE.width);
    public static final BigDecimal SCREEN_SIZE_HEIGHT = new BigDecimal(SCREEN_SIZE.height);


    /**
     * Gets the image files from the {@link #imagesFolderString} directory
     * corresponding to the given animation categories for the given animation directory.  There will always be a set of image files
     * in the directory that function as the default image files for all objects of this particular type.  This can be
     * overridden for an individual object using its id.
      * @param rootDirName The root directory for the game object's images.  This should be directly under
     *                               the {@link CommonUtils#imagesFolderString}
     * @param gameObjectStateNames The names of the animation categories that the files are being collected for.  Each animation
     *                            category name must correspond to a directory under the animation directory name that is provided
     *                            for the first parameter.
     * @param id The id of the object that the images correspond to.  If there is a subdirectory with this id as the name within the
     *           parent directory (the directory that contains the default image files for all objects of that type) then files in this
     *           directory will be returned instead of the defaults.
     * @return A map containing lists of Files keyed under the name of the animation category directory they were found in e.g.
     *         {"Left"->[FileL1,FileL2],"Right"->[filer1,filer2},"Jump"->[FileJ1,FileJ2,FileJ3]}.  These files must be named such
     *         that, when the names are ordered in DESC order, they correspond to the animation cycle e.g. if the frames for
     *         the moving left cycle are image_ghs.jpg, image_23te.jpg and imagee_d2h.jpg in that order then they should be renamed to
     *         image_1.jpg, image_2.jpg and image_3.jpg respectively, so image_1.jpg is the first frame displayed in the cycle
     *         and image_3.jpg is the last.
     */
    public static Map<String,File[]> getGameObjectStateImages(String rootDirName, String[] gameObjectStateNames, long id) {
        File f = new File(imagesFolderString + rootDirName);
        if(!f.exists() || !f.isDirectory()) {
            throw new IllegalArgumentException("Invalid file for " + f.getPath());
        }

        File[] categoryDirectories = f.listFiles();
        if(categoryDirectories.length == 0) {
            throw new IllegalArgumentException("No files in " + f.getPath());
        }

        Map<String, File[]> stateToFileMap = new HashMap<>(categoryDirectories.length);
        String rootDirPath = f.getPath();
        for(String gameObjectStateName : gameObjectStateNames) {
            File[] imageFiles =
                    getImagesFromAbsolutePath(rootDirPath + File.separator + gameObjectStateName, id);
            stateToFileMap.put(gameObjectStateName, imageFiles);
        }

        return stateToFileMap;
    }

    private static File[] getImagesFromAbsolutePath(String absolutePath, long id) {
        File directoryFile = new File(absolutePath);
        if(!directoryFile.exists() || !directoryFile.isDirectory()) {
            throw new IllegalArgumentException(directoryFile.getPath() + " is invalid");
        }

        String idString = Long.toString(id);
        File overrideDir = new File(absolutePath + File.separator + idString);
        if(overrideDir.exists()) {
            return overrideDir.listFiles();
        }
        else {
            File[] files = directoryFile.listFiles();
            files = Arrays.stream(files).filter(f -> {return f.isFile();}).sorted((a, b) -> {
                        return a.getName().compareTo(b.getName());
                    }
            ).collect(Collectors.toList()).toArray(new File[0]);

            return files;
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

    public static File getImagesFolderFor(GameObject go) {
        return new File(imagesFolderString + go.getImageFolderName());
    }

    public static BufferedImage getBackgroundImage() throws IOException {
        File backgroundFileFolder = new File(imagesFolderString + File.separator + "background");
        if(!backgroundFileFolder.exists()) {
            return null;
        }
        else if(backgroundFileFolder.listFiles().length != 1) {
            throw new IllegalStateException("Only supposed to have one background file.");
        }
        return ImageIO.read(backgroundFileFolder.listFiles()[0]);
    }

    public static String getImagesFolderString() {
        return imagesFolderString;
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
     * Returns a map of {@link GameClip} instances keyed on their name.  The clips are taken from the
     * CONFIG DIRECTORY/sounds directory.  The name is simply the file name without the extension.  If the file name
     * ends in "___c" then this is taken as an indication that the clip should be played continuously, meaning that the
     * resulting {@link GameClip}'s {@link GameClip#playContinuously()} method will return true.  The "___c" will also be
     * removed from the file name in order to make the clip's name e.g. "playerDies___c.wav" will result in
     * "playerDies" as will "playerDies.wav".
     * @see GameClip
     * @see SoundPlayer
     */
    public static Map<String,GameClip> getSounds() {
        File soundsDir = new File(CONFIG_FOLDER_PATH + File.separator + "sounds");
        if(!soundsDir.exists()) {
            return Collections.emptyMap();
        }

        File[] files = soundsDir.listFiles();
        Map<String,GameClip> soundMap = new HashMap<>();
        Arrays.stream(files).forEach(file -> {
            try {
                String fileNameNoExtension = file.getName().substring(0, file.getName().indexOf("."));
                String[] splits = fileNameNoExtension.split("___");
                String clipName = splits[0];
                boolean playContinously = splits.length > 1;
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                soundMap.put(clipName, new GameClip(clip, playContinously));
            }
            catch(Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Could not load sound clip");
            }
        });

        return soundMap;
    }
}
