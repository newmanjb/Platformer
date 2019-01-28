package com.noomtech.jsw.editor.gui.userinput_processing;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.common.utils.db.DBupdateType;
import com.noomtech.jsw.editor.building_blocks.CollisionAreaEditableObject;
import com.noomtech.jsw.editor.building_blocks.RootObject;
import com.noomtech.jsw.editor.gui.DrawingPanel;
import com.noomtech.jsw.editor.gui.Editable;
import com.noomtech.jsw.editor.gui.Saveable;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.objects.Platform;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;


/**
 * Responsible for the controlling in the MVC pattern in the editor.
 * The {@link DrawingPanel} is the view.  The collection of root objects is the data.
 */
public class MouseMovementHandler extends MouseAdapter {


    private Point pressedAt;
    private RootObject rootObjectPressedOn;
    private CollisionAreaEditableObject collisionAreaPressedOn;
    private int buttonPressed;
    private Mode currentMode = Mode.NORMAL;
    //If something has been copied it is held here ready to be pasted
    private RootObject copied;

    private enum Mode {
        NORMAL,
        COLLISION_AREA;
    }

    private final Map<Saveable, DBupdateType> UPDATES = new HashMap();

    private ReleaseAction releaseActionToPerform;
    private final ReleaseAction editorObjectMenuPopup = new EditorObjectMenuPopup();
    private final ReleaseAction collisionAreaMenuPopup = new CollisionAreaMenuPopup();
    private final ReleaseAction mainMenuPopup = new MainMenuPopup();
    private final ReleaseAction addEditable = new AddEditable();
    private final ReleaseAction updateEditable = new UpdateEditable();

    private final JMenuItem STATIC_IMG_UPDATE_MENU_ITEM;
    {
        STATIC_IMG_UPDATE_MENU_ITEM = new JMenuItem();
        STATIC_IMG_UPDATE_MENU_ITEM.setText("Update static image");
        STATIC_IMG_UPDATE_MENU_ITEM.addActionListener(new SelectStaticImgFileAction());
    }
    private final JMenuItem ANIMATION_IMG_UPDATE_MENU_ITEM;
    {
        ANIMATION_IMG_UPDATE_MENU_ITEM = new JMenuItem();
        ANIMATION_IMG_UPDATE_MENU_ITEM.setText("Update animation images");
        ANIMATION_IMG_UPDATE_MENU_ITEM.addActionListener(new SelectAnimationImgFilesAction());
    }

    private final DrawingPanel VIEW;
    private final List<RootObject> MODEL;


    public MouseMovementHandler(DrawingPanel view, List<RootObject> model) {
        this.VIEW = view;
        this.MODEL = model;
    }


    public void mouseDragged(MouseEvent e){
        if(buttonPressed == MouseEvent.BUTTON1 && !(rootObjectPressedOn == null && currentMode == Mode.COLLISION_AREA)) {
            Point whereWeAreNow = e.getPoint();
            Editable editable;
            if(currentMode == Mode.NORMAL) {
                editable = rootObjectPressedOn;
            }
            else if(currentMode == Mode.COLLISION_AREA) {
                editable = collisionAreaPressedOn;
            }
            else {
                throw new IllegalArgumentException("Unsupported mode " + currentMode);
            }

            if(editable == null) {
                releaseActionToPerform = addEditable;
                int xOrigin = Math.min(pressedAt.x, whereWeAreNow.x);
                int yOrigin = Math.min(pressedAt.y, whereWeAreNow.y);
                int xEnd = Math.max(pressedAt.x, whereWeAreNow.x);
                int yEnd = Math.max(pressedAt.y, whereWeAreNow.y);
                int width = xEnd - xOrigin;
                int height = yEnd - yOrigin;
                Rectangle r = new Rectangle(xOrigin, yOrigin, width, height);
                if (canDrawOrMove(r)) {
                    VIEW.setBeingDrawn(r);
                }
            } else {

                releaseActionToPerform = updateEditable;
                editable.setBeingMoved(true);
                Rectangle newArea = new Rectangle(editable.getArea());
                int xDifference = whereWeAreNow.x - pressedAt.x;
                int yDifference = whereWeAreNow.y - pressedAt.y;
                Point p = newArea.getLocation();
                p.x += xDifference;
                p.y += yDifference;
                newArea.setLocation(p);
                if(canDrawOrMove(newArea)) {
                    editable.setLocation(newArea);
                    pressedAt.x += xDifference;
                    pressedAt.y += yDifference;
                    VIEW.repaint();
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {

        rootObjectPressedOn = null;
        collisionAreaPressedOn = null;
        buttonPressed = -1;
        pressedAt = null;
        releaseActionToPerform = null;

        buttonPressed = e.getButton();
        pressedAt = e.getPoint();
        rootObjectPressedOn = getRootObjectForPoint(pressedAt);
        if (rootObjectPressedOn != null) {
            collisionAreaPressedOn = getCollisionAreaForPoint(pressedAt, rootObjectPressedOn);
        }

        releaseActionToPerform = null;
        if (buttonPressed == MouseEvent.BUTTON3) {
            if (rootObjectPressedOn == null) {
                releaseActionToPerform = mainMenuPopup;
            } else if (currentMode == Mode.NORMAL) {
                releaseActionToPerform = editorObjectMenuPopup;
            } else if (currentMode == Mode.COLLISION_AREA) {
                if(collisionAreaPressedOn != null) {
                    releaseActionToPerform = collisionAreaMenuPopup;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {

        if(releaseActionToPerform != null) {
            releaseActionToPerform.released(rootObjectPressedOn, buttonPressed, pressedAt, e);
        }

        if(rootObjectPressedOn != null) {
            rootObjectPressedOn.setBeingMoved(false);
        }
        if(collisionAreaPressedOn != null) {
            collisionAreaPressedOn.setBeingMoved(false);
        }
        VIEW.setBeingDrawn(null);
    }

    public void mouseClicked(MouseEvent m) {
        if (m.getClickCount() == 2) {
            if(currentMode == Mode.NORMAL) {
                RootObject doubleClickedOn = getRootObjectForPoint(m.getPoint());
                if (doubleClickedOn != null) {
                    AttributesPopup attributesPopup = new AttributesPopup(doubleClickedOn);
                    VIEW.setSelected(doubleClickedOn, true);
                    attributesPopup.setLocation(m.getPoint());
                    attributesPopup.setVisible(true);
                    VIEW.setSelected(doubleClickedOn, false);
                }
            }
        }
    }

    private interface ReleaseAction {
        void released(Editable pressedOn, int buttonPressed, Point pressedAt, MouseEvent e);
    }


    private class AddEditable implements ReleaseAction {
        @Override
        public void released(Editable pressedOn, int buttonPressed, Point pressedAt, MouseEvent e) {
            if(currentMode == Mode.NORMAL) {
                GameObject defaultGameObject;
                RootObject newRootObject;
                try {
                    defaultGameObject = new Platform(new Rectangle(VIEW.getBeingDrawn()), Collections.EMPTY_MAP);
                } catch (IOException ex) {
                    throw new IllegalArgumentException("Couldn't load game object's resources" + ex);
                }

                newRootObject = new RootObject(defaultGameObject, System.currentTimeMillis());
                MODEL.add(newRootObject);
                record_rootObjectAdded(newRootObject);
            }
            else if(currentMode == Mode.COLLISION_AREA) {
                rootObjectPressedOn.addCollisionArea(new CollisionAreaEditableObject(VIEW.getBeingDrawn()));
                record_rootObjectUpdated(rootObjectPressedOn);
            }
            else {
                throw new IllegalArgumentException("Unknown mode " + currentMode);
            }
        }
    }

    private class UpdateEditable implements ReleaseAction {
        @Override
        public void released(Editable pressedOn, int buttonPressed, Point pressedAt, MouseEvent e) {
            if(currentMode == Mode.COLLISION_AREA) {
                rootObjectPressedOn.updateCollisionAreas();
            }
            else if(currentMode != Mode.NORMAL) {
                throw new IllegalArgumentException("Unknown mode " + currentMode);
            }

            record_rootObjectUpdated(rootObjectPressedOn);
        }
    }

    private class EditorObjectMenuPopup implements ReleaseAction {
        @Override
        public void released(Editable pressedOn, int buttonPressed, Point pressedAt, MouseEvent e) {
            RootObject releasedOn = getRootObjectForPoint(e.getPoint());
            if (releasedOn == pressedOn) {
                List<JMenuItem> itemList = new ArrayList<>();
                JMenuItem deleteMenuItem = new JMenuItem();
                deleteMenuItem.addActionListener(new DeleteAction(pressedOn));
                deleteMenuItem.setText("Delete");
                itemList.add(deleteMenuItem);

                if(pressedOn.supportsCopy()) {
                    JMenuItem copyMenuItem = new JMenuItem();
                    copyMenuItem.addActionListener(new CopyAction((RootObject) pressedOn));
                    copyMenuItem.setText("Copy");
                    itemList.add(copyMenuItem);
                }

                JPopupMenu popupMenu = new JPopupMenu();
                for (JMenuItem jMenuItem : itemList) {
                    popupMenu.add(jMenuItem);
                }
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private class CopyAction implements Action {
        private RootObject pressedOn;
        private CopyAction(RootObject pressedOn) {
            this.pressedOn = pressedOn;
        }
        @Override
        public Object getValue(String key) {
            return null;
        }
        @Override
        public void putValue(String key, Object value) {
        }
        @Override
        public void setEnabled(boolean b) {
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            copied = pressedOn;
        }
    }


    private class PasteAction implements Action {
        private Point pastedAt;

        public PasteAction(Point pastedAt) {
            this.pastedAt = pastedAt;
        }

        @Override
        public Object getValue(String key) {
            return null;
        }
        @Override
        public void putValue(String key, Object value) {
        }
        @Override
        public void setEnabled(boolean b) {
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                RootObject copy = (RootObject) copied.copy();
                copy.setLocation(new Rectangle(pastedAt.x, pastedAt.y, copy.getArea().width, copy.getArea().height));
                MODEL.add(copy);
                record_rootObjectAdded(copy);
            }
            catch(Exception ex) {
                System.out.println("Problem copying object:  " + ex);
            }
        }
    }

    private class CollisionAreaMenuPopup implements ReleaseAction {
        @Override
        public void released(Editable pressedOn, int buttonPressed, Point pressedAt, MouseEvent e) {
            Editable releasedOn = getRootObjectForPoint(e.getPoint());
            if(releasedOn == pressedOn) {
                JMenuItem jMenuItem = new JMenuItem();
                jMenuItem.addActionListener(new DeleteAction(collisionAreaPressedOn));
                jMenuItem.setText("Delete");
                JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.add(jMenuItem);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private class MainMenuPopup implements ReleaseAction {
        @Override
        public void released(Editable pressedOn, int buttonPressed, Point pressedAt, MouseEvent e) {
            List<JMenuItem> itemList = new ArrayList<>();
            JMenuItem modeItem = new JMenuItem();
            modeItem.addActionListener(new ChangeModeAction());
            modeItem.setText(currentMode == Mode.NORMAL ? "Collision Area Mode" : "Normal Mode");
            itemList.add(modeItem);
            //Only add the paste action if its appropiate
            if(canPaste(pressedAt)) {
                JMenuItem pasteMenuItem = new JMenuItem();
                pasteMenuItem.addActionListener(new PasteAction(pressedAt));
                pasteMenuItem.setText("Paste");
                itemList.add(pasteMenuItem);
            }

            itemList.add(STATIC_IMG_UPDATE_MENU_ITEM);
            itemList.add(ANIMATION_IMG_UPDATE_MENU_ITEM);

            JPopupMenu popupMenu = new JPopupMenu();
            for(JMenuItem jMenuItem : itemList) {
                popupMenu.add(jMenuItem);
            }

            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        private boolean canPaste(Point at) {
            if(copied != null && currentMode == Mode.NORMAL) {
                Rectangle potentialPasteArea = new Rectangle(copied.getArea());
                potentialPasteArea.setLocation(at);
                Dimension screenSize = VIEW.getScreenSize();
                //Make sure that when it's pasted it doesn't clash with anything else
                if(!(potentialPasteArea.x + potentialPasteArea.width >= screenSize.width ||
                        potentialPasteArea.y + potentialPasteArea.height >= screenSize.height)) {
                    return !VIEW.intersectsWithAnyRootObjects(potentialPasteArea);
                }
            }
            return false;
        }
    }



    private class AttributesPopup extends JDialog {
        AttributesPopup(final RootObject rootObject) {
            super((JFrame)null, "Attributes", true);
            setLayout(new BorderLayout());
            JComboBox<String> classBox = new JComboBox(CommonUtils.SELECTABLE_GAME_OBJECTS);
            JTextArea textArea = new JTextArea();
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(ae -> {
                String attributeString = textArea.getText();
                Map<String,String> attributes = new HashMap<>();
                if(attributeString != null && !attributeString.equals("")) {
                    String[] keyValPairs = attributeString.split(",");
                    for(String keyValPair : keyValPairs) {
                        String[] keyValArray = keyValPair.split("=");
                        attributes.put(keyValArray[0], keyValArray[1]);
                    }
                }
                //The type of game object is represented as the full name of the underlying class so as it can be instantiated
                //using reflection when it's loaded
                String theClass = CommonUtils.SELECTABLE_GAME_OBJECT_PACKAGE + "." + classBox.getSelectedItem();
                GameObject newGameObject;
                GameObject oldGameObject = rootObject.getGameObject();
                try {
                    newGameObject = (GameObject)Class.forName(theClass).getConstructor(Rectangle.class, Map.class).newInstance(
                            rootObject.getArea(), attributes);
                    newGameObject.setCollisionAreas(oldGameObject.getCollisionAreas());
                    newGameObject.setLocation(oldGameObject.getX(), oldGameObject.getY());
                }
                catch(Exception c) {
                    throw new IllegalArgumentException("Couldn't create game object", c);
                }

                rootObject.setGameObject(newGameObject);
                record_rootObjectUpdated(rootObject);

                setVisible(false);
                dispose();
            });

            GameObject gameObject = rootObject.getGameObject();
            if(gameObject != null) {
                Map<String, String> attributes = gameObject.getAttributes();
                if (!attributes.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        sb.append(entry.getKey() + "=" + entry.getValue() + ",");
                    }
                    textArea.setText(sb.substring(0, sb.length() - 1));
                }

                classBox.setSelectedItem(gameObject.getClass().getSimpleName());
            }
            else {
                classBox.setSelectedItem(classBox.getItemAt(0));
            }

            add(classBox, BorderLayout.NORTH);
            add(textArea, BorderLayout.CENTER);
            add(saveButton, BorderLayout.SOUTH);
            Dimension size = new Dimension(350,350);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);

            pack();
        }
    }

    private RootObject getRootObjectForPoint(Point p) {
        for(RootObject c : MODEL) {
            if(c.getArea().contains(p)) {
                return c;
            }
        }
        return null;
    }

    public Map<Saveable, DBupdateType> getUpdates() {
        return UPDATES;
    }

    private void record_rootObjectUpdated(RootObject parent) {
        DBupdateType existing = UPDATES.get(parent);
        if(existing != DBupdateType.ADD) {
            UPDATES.put(parent, DBupdateType.UPDATE);
        }

        VIEW.repaint();
    }

    private void record_rootObjectAdded(RootObject c) {
        UPDATES.put(c, DBupdateType.ADD);
        VIEW.repaint();
    }

    public void record_editableRemoved(RootObject parent, Editable toRemove) {
        DBupdateType existing = UPDATES.get(parent);
        if(parent == toRemove) {
            if(existing != DBupdateType.ADD) {
                UPDATES.put(parent, DBupdateType.DELETE);
            }
            else {
                UPDATES.remove(parent);
            }
        }
        else if(existing == null) {
            //We're removing a child object from a parent, so the parent only needs updating
            UPDATES.put(parent, DBupdateType.UPDATE);
        }
        VIEW.repaint();
    }

    public void clearUpdates() {
        UPDATES.clear();
    }

    private boolean canDrawOrMove(Rectangle r) {
        if(currentMode == Mode.NORMAL) {
            for (Editable c : MODEL) {
                if (!c.isBeingMoved() && c.getArea().intersects(r)) {
                    return false;
                }
            }
            return true;
        }
        else if(currentMode == Mode.COLLISION_AREA) {
            boolean returnVal = true;
            if(!rootObjectPressedOn.getArea().contains(r)) {
                return false;
            }
            else {
                for(CollisionAreaEditableObject c : rootObjectPressedOn.getCollisionAreas()) {
                    if(c != collisionAreaPressedOn) {
                        if(c.getArea().intersects(r)) {
                            return false;
                        }
                    }
                }
                return returnVal;
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported mode " + currentMode);
        }
    }

    private CollisionAreaEditableObject getCollisionAreaForPoint(Point p, RootObject on) {
        for(CollisionAreaEditableObject collisionArea : on.getCollisionAreas()) {
            if(collisionArea.getArea().contains(p)) {
                return collisionArea;
            }
        }

        return null;
    }

    private class DeleteAction implements Action {

        private Editable toDelete;
        private DeleteAction(Editable toDelete) {
            this.toDelete = toDelete;
        }

        public Object getValue(String key) {
            return null;
        }
        public void putValue(String key, Object value) { }
        public void setEnabled(boolean b) {
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(currentMode == Mode.NORMAL) {
                MODEL.remove(toDelete);
                record_editableRemoved(rootObjectPressedOn, rootObjectPressedOn);
            }
            else if(currentMode == Mode.COLLISION_AREA){
                rootObjectPressedOn.removeCollisionArea((CollisionAreaEditableObject)toDelete);
                record_editableRemoved(rootObjectPressedOn, toDelete);
            }
            else {
                throw new IllegalStateException("Unsupported mode");
            }
        }
    }

    private class ChangeModeAction implements Action {


        public Object getValue(String key) {
            return null;
        }
        public void putValue(String key, Object value) { }
        public void setEnabled(boolean b) {
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(currentMode == Mode.NORMAL) {
                currentMode = Mode.COLLISION_AREA;
                VIEW.setDrawCollisionAreas(true);
            }
            else if(currentMode == Mode.COLLISION_AREA){
                currentMode = Mode.NORMAL;
                VIEW.setDrawCollisionAreas(false);
            }
            else {
                throw new UnsupportedOperationException("Unknown mode " + currentMode);
            }
        }
    }


    private class SelectStaticImgFileAction extends UpdateImgFileAction {
        private SelectStaticImgFileAction() {
            super(CommonUtils.STATIC_OBJECT_IMAGES_FOLDER_FILE, false);
        }
    }
    private class SelectAnimationImgFilesAction extends UpdateImgFileAction {
        private SelectAnimationImgFilesAction() {
            super(CommonUtils.ANIMATION_FRAMES_FOLDER_FILE, true);
        }
    }

    private class UpdateImgFileAction implements Action {


        private File destDir;
        private boolean allowMultiSelection;
        private UpdateImgFileAction(File destDir, boolean allowMultiSelection) {
            this.destDir = destDir;
            this.allowMultiSelection = allowMultiSelection;
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
                return f.isDirectory();
            }
            public String getDescription() {
                return "Image Directories";
            }
        };

        public Object getValue(String key) {
            return null;
        }
        public void putValue(String key, Object value) { }
        public void setEnabled(boolean b) {
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setMultiSelectionEnabled(allowMultiSelection);
            jFileChooser.setDialogTitle("Choose Image File" + (allowMultiSelection ? "s" : ""));
            jFileChooser.addChoosableFileFilter(IMAGE_FILE_FILTER);
            jFileChooser.setAcceptAllFileFilterUsed(false);

            if(jFileChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
                File[] chosenFiles = allowMultiSelection ? jFileChooser.getSelectedFiles() : new File[]{jFileChooser.getSelectedFile()};
                JFileChooser destFileChooser = new JFileChooser();
                destFileChooser.setMultiSelectionEnabled(false);
                destFileChooser.setCurrentDirectory(destDir);
                destFileChooser.setDialogTitle("Choose Destination");
                destFileChooser.setAcceptAllFileFilterUsed(false);
                destFileChooser.setFileFilter(DESTINATION_FILE_FILTER);
                destFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (destFileChooser.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
                    File chosenDestination = destFileChooser.getSelectedFile();
                    try {
                        CommonUtils.changeFilesInImageDir(chosenDestination, chosenFiles);
                        VIEW.onImageForRootObjectUpdated();
                    }
                    catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}