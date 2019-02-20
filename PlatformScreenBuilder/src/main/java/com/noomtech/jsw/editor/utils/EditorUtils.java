package com.noomtech.jsw.editor.utils;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.common.utils.GlobalConfig;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.concrete_objects.JSW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.noomtech.jsw.common.utils.CommonUtils.PROPERTY_NAME_CONFIG;

public class EditorUtils {


    private static final String CONFIG_FOLDER_PATH = System.getProperty(PROPERTY_NAME_CONFIG);

    public static final String SELECTABLE_GAME_OBJECT_PACKAGE = JSW.class.getPackage().getName();
    public static final String SELECTABLE_GAME_OBJECT_DIRECTORY = SELECTABLE_GAME_OBJECT_PACKAGE.replace(".", File.separator);
    //The list of game objects available for selection in the editor.
    public static final String[] SELECTABLE_GAME_OBJECTS;
    static {
        //Go through the files in the game object package and construct each one's full name
        String classDirectory = new File(JSW.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getPath();
        String packagePath = new StringBuilder(classDirectory).append(
                File.separator).append(SELECTABLE_GAME_OBJECT_DIRECTORY).toString();
        File[] classNames = new File(packagePath).listFiles();
        String[] gameObjectClasses = new String[classNames.length];
        for (int i = 0; i < gameObjectClasses.length; i++) {
            gameObjectClasses[i] = classNames[i].getName().subSequence(0, classNames[i].getName().indexOf(".")).toString();
        }
        SELECTABLE_GAME_OBJECTS = gameObjectClasses;
    }

    //Should point to a folder where the user puts their images.  For convenience in the GUI.
    public static final File MY_IMAGES_FOLDER = new File(GlobalConfig.getInstance().getProperty("my_images_folder"));


    public static void setBackgroundFile(File imageFile) throws IOException {
        File backgroundFileDestination = new File(CONFIG_FOLDER_PATH + File.separator + "levels" + File.separator +
                getGameObjectCollectionNameForLevel(CommonUtils.getCurrentLevel()) + File.separator + "images" + File.separator + "background");
        if(!backgroundFileDestination.exists()) {
            if(!backgroundFileDestination.mkdirs()) {
                throw new IllegalStateException("Could not create: " + backgroundFileDestination);
            }
        }
        else {
            //Delete the existing background file
            File[] existingFiles = backgroundFileDestination.listFiles();
            if(existingFiles.length != 1) {
                throw new IllegalStateException("Only supposed to have one background file.  We have " +
                        existingFiles.length + " files in " + backgroundFileDestination);
            }
            for(File existingFile : existingFiles) {
                existingFile.delete();
            }
        }

        Files.copy(Paths.get(imageFile.toURI()), Paths.get(backgroundFileDestination.getAbsolutePath() + File.separator +
                imageFile.getName()));
    }


    /**
     * Copy the image files provided to the state directory for the state provided of the game object provided.  The image files
     * will be stored in a subdirectory named after the game object's id e.g. for a platform on level 2 with id 12345, having the images for its
     * "nada" state overridden with an image file img1.png would result in the following file being created
     *
     * CONFIG_DIR/levels/level2/images/Platform/nada/12345/img1.png
     *
     * If the subdirectory 12345 already existed then the file in it would be deleted before the new one was copied over.
     */
    public static void addImageOverrides(GameObject gameObject, String stateToOverride, File[] images) throws IOException {
        File overrideDirectory = new File(CommonUtils.getImagesFolderString() + gameObject.getImageFolderName() +
                File.separator + stateToOverride + File.separator + gameObject.getId());
        if(overrideDirectory.exists()) {
            //Remove everything in there
            for(File f : overrideDirectory.listFiles()) {
                f.delete();
            }
        }
        else {
            overrideDirectory.mkdir();
        }

        String overrideDirectoryString = overrideDirectory.getAbsolutePath();
        for(File imageFile : images) {
            Files.copy(Paths.get(imageFile.toURI()), Paths.get(overrideDirectoryString + File.separator +
                    imageFile.getName()));
        }
    }

    public static final String getGameObjectCollectionNameForLevel(int level) {
        return "level" + level;
    }
}
