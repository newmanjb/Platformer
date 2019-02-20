package com.noomtech.jsw.editor.utils;

import com.noomtech.jsw.common.utils.CommonUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.noomtech.jsw.common.utils.CommonUtils.PROPERTY_NAME_CONFIG;

public class EditorUtils {


    private static final String CONFIG_FOLDER_PATH = System.getProperty(PROPERTY_NAME_CONFIG);


    public static void setBackgroundFile(File imageFile) throws IOException {
        File backgroundFileDestination = new File(CONFIG_FOLDER_PATH + File.separator + "levels" + File.separator +
                CommonUtils.getGameObjectCollectionNameForLevel(CommonUtils.getCurrentLevel()) + File.separator + "images" + File.separator + "background");
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
}
