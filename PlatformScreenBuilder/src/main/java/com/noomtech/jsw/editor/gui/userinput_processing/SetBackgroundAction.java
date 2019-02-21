package com.noomtech.jsw.editor.gui.userinput_processing;

import com.noomtech.jsw.editor.utils.EditorUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

class SetBackgroundAction implements Action {


    private MouseMovementHandler PARENT;
    SetBackgroundAction(MouseMovementHandler parent) {
        this.PARENT = parent;
    }
    private final Set<String> ACCEPTED_EXTENSIONS = new HashSet();
    {
        ACCEPTED_EXTENSIONS.add(".png");
        ACCEPTED_EXTENSIONS.add(".PNG");
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

    public Object getValue(String key) { return null; }
    public void putValue(String key, Object value) { }
    public void setEnabled(boolean b) { }
    public boolean isEnabled() { return true; }
    public void addPropertyChangeListener(PropertyChangeListener listener) { }
    public void removePropertyChangeListener(PropertyChangeListener listener) { }

    @Override
    public void actionPerformed(ActionEvent e) {

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setDialogTitle("Choose Image File For Background");
        jFileChooser.addChoosableFileFilter(IMAGE_FILE_FILTER);
        jFileChooser.setCurrentDirectory(EditorUtils.MY_IMAGES_FOLDER);

        if(jFileChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
            try {
                File newImageFile = jFileChooser.getSelectedFile();
                EditorUtils.setBackgroundFile(newImageFile);
                PARENT.VIEW.refreshBackgroundFile();
            }
            catch(Exception ex) {
                System.out.println("Cannot set background");
                ex.printStackTrace();
            }
        }
    }
}