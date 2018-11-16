package com.noomtech.jsw.editor.frame;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.editor.building_blocks.EditorObject;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gameobjects.objects.Platform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


class MouseMovementHandler extends MouseAdapter {


    private Point pressedAt;
    private EditorObject pressedOn;
    private int buttonPressed;
    private enum MODE {
        NORMAL,
        COLLISION_AREA;
    }

    private final ReleaseAction editorObjectMenuPopup = new EditorObjectMenuPopup();
    private final ReleaseAction addGameObject = new AddGameObject();
    private ReleaseAction releaseAction;

    private DrawingPanel drawingPanel;


    MouseMovementHandler(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
    }


    public void mouseDragged(MouseEvent e){
        if(buttonPressed == MouseEvent.BUTTON1) {
            Point whereWeAreNow = e.getPoint();
            if (pressedOn == null) {
                releaseAction = addGameObject;
                int xOrigin = Math.min(pressedAt.x, whereWeAreNow.x);
                int yOrigin = Math.min(pressedAt.y, whereWeAreNow.y);
                int xEnd = Math.max(pressedAt.x, whereWeAreNow.x);
                int yEnd = Math.max(pressedAt.y, whereWeAreNow.y);
                int width = xEnd - xOrigin;
                int height = yEnd - yOrigin;
                Rectangle r = new Rectangle(xOrigin, yOrigin, width, height);
                if (!isOnAnyEditorObjects(r)) {
                    drawingPanel.setBeingDrawn(r);
                    drawingPanel.repaint();
                }
            } else {
                pressedOn.setBeingMoved(true);
                Rectangle newArea = new Rectangle(pressedOn.getArea());
                int xDifference = whereWeAreNow.x - pressedAt.x;
                int yDifference = whereWeAreNow.y - pressedAt.y;
                Point p = newArea.getLocation();
                p.x += xDifference;
                p.y += yDifference;
                newArea.setLocation(p);
                if(!isOnAnyEditorObjects(newArea)) {
                    pressedOn.setArea(newArea);
                    pressedAt.x += xDifference;
                    pressedAt.y += yDifference;
                    drawingPanel.repaint();
                }
            }
        }
    }

    public void mousePressed(MouseEvent e){
        buttonPressed = e.getButton();
        pressedAt = e.getPoint();
        pressedOn = isIn(pressedAt);

        if(buttonPressed == MouseEvent.BUTTON3 && pressedOn != null) {
            releaseAction = editorObjectMenuPopup;
        }
    }

    public void mouseReleased(MouseEvent e) {

        if(releaseAction != null) {
            releaseAction.released(pressedOn, buttonPressed, pressedAt, e);
        }

        if(pressedOn != null) {
            pressedOn.setBeingMoved(false);
        }

        pressedOn = null;
        buttonPressed = -1;
        pressedAt = null;
        releaseAction = null;
        drawingPanel.setBeingDrawn(null);

        drawingPanel.repaint();
    }

    public void mouseClicked(MouseEvent m) {
        if (m.getClickCount() == 2) {
            EditorObject doubleClickedOn = isIn(m.getPoint());
            if(doubleClickedOn != null) {
                AttributesPopup attributesPopup = new AttributesPopup(doubleClickedOn);
                doubleClickedOn.setSelected(true);
                drawingPanel.repaint();
                attributesPopup.setLocation(m.getPoint());
                attributesPopup.setVisible(true);
                doubleClickedOn.setSelected(false);
                drawingPanel.repaint();
            }
        }
    }

    private interface ReleaseAction {
        void released(EditorObject pressedOn, int buttonPressed, Point pressedAt, MouseEvent e);
    }

    private class AddGameObject implements ReleaseAction {
        @Override
        public void released(EditorObject pressedOn, int buttonPressed, Point pressedAt, MouseEvent e) {
            GameObject defaultGameObject;
            EditorObject newEditorObject;
            try {
                defaultGameObject = new Platform(new Rectangle(drawingPanel.getBeingDrawn()), Collections.EMPTY_MAP);
            }
            catch(IOException ex) {
                throw new IllegalArgumentException("Couldn't load game object's resources" + ex);
            }

            newEditorObject = new EditorObject(defaultGameObject, System.currentTimeMillis());
            drawingPanel.addEditorObject(newEditorObject);
        }
    }

    private class EditorObjectMenuPopup implements ReleaseAction {
        @Override
        public void released(EditorObject pressedOn, int buttonPressed, Point pressedAt, MouseEvent e) {
            EditorObject releasedOn = isIn(e.getPoint());
            if(releasedOn == pressedOn) {
                JMenuItem jMenuItem = new JMenuItem();
                jMenuItem.addActionListener(new DeleteAction(pressedOn));
                jMenuItem.setText("Delete");
                JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.add(jMenuItem);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }


    private class AttributesPopup extends JDialog {
        AttributesPopup(final EditorObject editorObject) {
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
                GameObject g;
                try {
                    g = (GameObject)Class.forName(theClass).getConstructor(Rectangle.class, Map.class).newInstance(
                            editorObject.getArea(), attributes);
                }
                catch(Exception c) {
                    throw new IllegalArgumentException("Couldn't create game object", c);
                }

                EditorObject newEditorObject = new EditorObject(g, editorObject.getId());
                drawingPanel.removeEditorObject(editorObject);
                drawingPanel.addEditorObject(newEditorObject);
                drawingPanel.addToDeleteList(editorObject);

                setVisible(false);
                dispose();
            });

            GameObject gameObject = editorObject.getGameObject();
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

    private EditorObject isIn(Point p) {
        for(EditorObject c : drawingPanel.getEditorObjects()) {
            if(c.getArea().contains(p)) {
                return c;
            }
        }
        return null;
    }

    private boolean isOnAnyEditorObjects(Rectangle r) {
        for(EditorObject c : drawingPanel.getEditorObjects()) {
            if(!c.isBeingMoved() && c.getArea().intersects(r)) {
                return true;
            }
        }
        return false;
    }


    private class DeleteAction implements Action {

        private EditorObject toDelete;
        private DeleteAction(EditorObject toDelete) {
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
            drawingPanel.removeEditorObject(toDelete);
            drawingPanel.addToDeleteList(toDelete);
            drawingPanel.repaint();
        }
    }
}