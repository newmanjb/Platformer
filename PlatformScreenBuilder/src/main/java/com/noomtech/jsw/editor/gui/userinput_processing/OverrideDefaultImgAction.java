package com.noomtech.jsw.editor.gui.userinput_processing;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.editor.utils.EditorUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

//Lets the user choose an image(s) and then choose the state directory within that object type's images directory
// that they want the images to be copied to.  It will then create a new directory with a name of the selected
// object's id within this chosen state directory and copy the selected image files to this new directory i.e.
// overriding the default image(s), for this object only, with these new images.  If this id subdirectory already
//exists then all the files in this directory will be deleted before the new ones are copied over.
class OverrideDefaultImgAction implements Action {

    private final MouseMovementHandler PARENT;
    OverrideDefaultImgAction(MouseMovementHandler parent) {
        this.PARENT = parent;
    }

    private final Set<String> ACCEPTED_EXTENSIONS = new HashSet();
    {
        ACCEPTED_EXTENSIONS.add(".png");
        ACCEPTED_EXTENSIONS.add(".jpg");
        ACCEPTED_EXTENSIONS.add(".JPG");
        ACCEPTED_EXTENSIONS.add(".jpeg");
        ACCEPTED_EXTENSIONS.add(".JPEG");
    }
    private final FileFilter IMAGE_FILE_FILTER = new FileFilter() {
        public boolean accept(File f) {
            String fileName = f.getName();
            int indexOfDot = fileName.indexOf(".");
            if(indexOfDot > -1) {
                String extension = fileName.substring(fileName.lastIndexOf("."), fileName.length());
                return ACCEPTED_EXTENSIONS.contains(extension);
            }
            return f.isDirectory();
        }
        public String getDescription() {
            return "Image Files";
        }
    };
    private final FileFilter DESTINATION_FILE_FILTER = new FileFilter() {
        public boolean accept(File f) {
            if(f.isDirectory()) {
                boolean isNotIdDir = false;
                try {
                    Long.parseLong(f.getName());
                }
                catch(NumberFormatException nfe) {
                    isNotIdDir = true;
                }
                return isNotIdDir;
            }
            return false;
        }
        public String getDescription() {
            return "Image Directories";
        }
    };

    public Object getValue(String key) { return null; }
    public void putValue(String key, Object value) { }
    public void setEnabled(boolean b) { }
    public boolean isEnabled() { return true; }
    public void addPropertyChangeListener(PropertyChangeListener listener) { }
    public void removePropertyChangeListener(PropertyChangeListener listener) { }

    @Override
    public void actionPerformed(ActionEvent e) {

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setMultiSelectionEnabled(true);
        jFileChooser.setDialogTitle("Choose Image File(s)");
        jFileChooser.addChoosableFileFilter(IMAGE_FILE_FILTER);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setCurrentDirectory(EditorUtils.MY_IMAGES_FOLDER);

        if(jFileChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {

            File[] chosenImageFiles = jFileChooser.getSelectedFiles();
            String[] states = PARENT.rootObjectPressedOn.getGameObject().getGameObjectStateNames();
            String chosenStateForImage = null;
            if(states.length > 1) {

                JFileChooser stateDirChooser = new JFileChooser();
                stateDirChooser.setMultiSelectionEnabled(false);
                stateDirChooser.setCurrentDirectory(CommonUtils.getImagesFolderFor( PARENT.rootObjectPressedOn.getGameObject()));
                stateDirChooser.setDialogTitle("Choose state to override");
                stateDirChooser.setAcceptAllFileFilterUsed(false);
                stateDirChooser.setFileFilter(DESTINATION_FILE_FILTER);
                stateDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (stateDirChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
                    chosenStateForImage = stateDirChooser.getSelectedFile().getName();
                }
            }
            else {
                chosenStateForImage = states[0];
            }

            if(chosenStateForImage != null) {
                try {
                    EditorUtils.addImageOverrides( PARENT.rootObjectPressedOn.getGameObject(), chosenStateForImage, chosenImageFiles);
                    PARENT.rootObjectPressedOn.getGameObject().onImageUpdated();
                    PARENT.VIEW.repaint();
                }
                catch(IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
}