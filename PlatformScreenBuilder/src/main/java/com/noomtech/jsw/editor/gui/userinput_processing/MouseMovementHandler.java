package com.noomtech.jsw.editor.gui.userinput_processing;

import com.noomtech.jsw.common.utils.db.DBupdateType;
import com.noomtech.jsw.editor.building_blocks.CollisionAreaEditableObject;
import com.noomtech.jsw.editor.building_blocks.RootObject;
import com.noomtech.jsw.editor.gui.DrawingPanel;
import com.noomtech.jsw.editor.gui.Editable;
import com.noomtech.jsw.editor.gui.Saveable;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.concrete_objects.Platform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


/**
 * Responsible for the controlling in the MVC pattern in the editor.
 * The {@link DrawingPanel} is the view.  The collection of root objects is the data.
 */
public class MouseMovementHandler extends MouseAdapter {


    private Point pressedAt;
    RootObject rootObjectPressedOn;
    private CollisionAreaEditableObject collisionAreaPressedOn;
    private int buttonPressed;
    Mode currentMode = Mode.NORMAL;
    //If something has been copied it is held here ready to be pasted
    RootObject copied;

    private static final String NEWLINE = System.getProperty("line.separator");

    enum Mode {
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

    private final Action overrideDefaultImgAction = new OverrideDefaultImgAction(this);

    final DrawingPanel VIEW;
    final List<RootObject> MODEL;


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
                    AttributesPopup attributesPopup = new AttributesPopup(doubleClickedOn, this);
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
                long newID = System.currentTimeMillis();
                GameObject defaultGameObject = new Platform(new Rectangle(VIEW.getBeingDrawn()), Collections.EMPTY_MAP, newID);
                RootObject newRootObject = new RootObject(defaultGameObject);
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
                deleteMenuItem.addActionListener(new DeleteAction(pressedOn, MouseMovementHandler.this));
                deleteMenuItem.setText("Delete");
                itemList.add(deleteMenuItem);

                if(pressedOn.supportsCopy()) {
                    JMenuItem copyMenuItem = new JMenuItem();
                    copyMenuItem.addActionListener(new CopyAction((RootObject) pressedOn, MouseMovementHandler.this));
                    copyMenuItem.setText("Copy");
                    itemList.add(copyMenuItem);
                }

                if(pressedOn instanceof RootObject) {
                    JMenuItem overrideDefaultImgItem = new JMenuItem();
                    overrideDefaultImgItem.addActionListener(overrideDefaultImgAction);
                    overrideDefaultImgItem.setText("Change Image");
                    itemList.add(overrideDefaultImgItem);
                }

                JPopupMenu popupMenu = new JPopupMenu();
                for (JMenuItem jMenuItem : itemList) {
                    popupMenu.add(jMenuItem);
                }
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private class CollisionAreaMenuPopup implements ReleaseAction {
        @Override
        public void released(Editable pressedOn, int buttonPressed, Point pressedAt, MouseEvent e) {
            Editable releasedOn = getRootObjectForPoint(e.getPoint());
            if(releasedOn == pressedOn) {
                JMenuItem jMenuItem = new JMenuItem();
                jMenuItem.addActionListener(new DeleteAction(collisionAreaPressedOn, MouseMovementHandler.this));
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
            modeItem.addActionListener(new ChangeModeAction(MouseMovementHandler.this));
            modeItem.setText(currentMode == Mode.NORMAL ? "Collision Area Mode" : "Normal Mode");
            itemList.add(modeItem);
            //Only add the paste action if its appropiate
            if(canPaste(pressedAt)) {
                JMenuItem pasteMenuItem = new JMenuItem();
                pasteMenuItem.addActionListener(new PasteAction(pressedAt, MouseMovementHandler.this));
                pasteMenuItem.setText("Paste");
                itemList.add(pasteMenuItem);
            }

            JMenuItem pasteMenuItem = new JMenuItem();
            pasteMenuItem.addActionListener(new SetBackgroundAction(MouseMovementHandler.this));
            pasteMenuItem.setText("Set Background");
            itemList.add(pasteMenuItem);

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

    void record_rootObjectUpdated(RootObject parent) {
        DBupdateType existing = UPDATES.get(parent);
        if(existing != DBupdateType.ADD) {
            UPDATES.put(parent, DBupdateType.UPDATE);
        }

        VIEW.repaint();
    }

    void record_rootObjectAdded(RootObject c) {
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

    //Force all objects to reload their images e.g. after an image has been edited or a new one has been copied to
    //one of the image directories.
    public void refreshAllImages() {
        //@todo - doing ALL root objects here.  If this ever gets slow then we'll need to add a mechanism which
        //only does the root objects whos images have been updated.
        for(RootObject rootObject : MODEL) {
            rootObject.onImageUpdated();
        }
        VIEW.repaint();
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
}