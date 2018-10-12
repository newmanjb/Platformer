package com.noomtech.jsw.editor.frame;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.game.gameobjects.*;
import com.noomtech.jsw.game.gameobjects.objects.*;
import com.noomtech.jsw.editor.building_blocks.EditorObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class DrawingPanel extends JPanel {


    private final List<EditorObject> editorObjects;
    private volatile Rectangle beingDrawn;
    private final List<EditorObject> toDelete = new ArrayList<>();


    DrawingPanel(List<EditorObject> editorObjects) {
        this.editorObjects = editorObjects;
        MouseMovementHandler mouseMovementHandler = new MouseMovementHandler();
        this.addMouseListener(mouseMovementHandler);
        this.addMouseMotionListener(mouseMovementHandler);
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.WHITE);
        Dimension size = getSize();
        g.fillRect(0,0,size.width,size.height);

        for(EditorObject c : editorObjects) {
            Rectangle r = c.getArea();
            if(c.isSelected() && !c.isBeingMoved()) {
                g.setColor(Color.RED);
                g.drawRect(r.x,r.y,r.width,r.height);
            }
            else {
                c.getGameObject().doPainting(g);
            }
        }

        if(beingDrawn != null) {
            g.setColor(Color.GREEN);
            g.drawRect(beingDrawn.x, beingDrawn.y, beingDrawn.width, beingDrawn.height);
        }
    }

    private Color getColourForOutline(EditorObject c) {
        if(c.isBeingMoved()) {
            return Color.RED;
        }
        else {
            return Color.BLACK;
        }
    }

    void addEditorObject(EditorObject c) {
        editorObjects.add(c);
    }


    private class MouseMovementHandler extends MouseAdapter {


        private Point pressedAt;
        private EditorObject pressedOn;
        private int buttonPressed;


        private MouseMovementHandler() {
        }


        public void mouseDragged(MouseEvent e){
            if(buttonPressed == MouseEvent.BUTTON1) {
                Point whereWeAreNow = e.getPoint();
                if (pressedOn == null) {
                    int xOrigin = Math.min(pressedAt.x, whereWeAreNow.x);
                    int yOrigin = Math.min(pressedAt.y, whereWeAreNow.y);
                    int xEnd = Math.max(pressedAt.x, whereWeAreNow.x);
                    int yEnd = Math.max(pressedAt.y, whereWeAreNow.y);
                    int width = xEnd - xOrigin;
                    int height = yEnd - yOrigin;
                    Rectangle r = new Rectangle(xOrigin, yOrigin, width, height);
                    if (!isOnAnyEditorObjects(r)) {
                        beingDrawn = r;
                        repaint();
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
                        repaint();
                    }
                }
            }
        }

        public void mousePressed(MouseEvent e){
            buttonPressed = e.getButton();
            pressedAt = e.getPoint();
            pressedOn = isIn(pressedAt);
        }

        public void mouseReleased(MouseEvent e) {

            if(beingDrawn != null) {
                GameObject defaultGameObject;
                EditorObject newEditorObject;
                try {
                    defaultGameObject = new Platform(new Rectangle(beingDrawn), Collections.EMPTY_MAP);
                }
                catch(IOException ex) {
                    throw new IllegalArgumentException("Couldn't load game object's resources" + ex);
                }

                newEditorObject = new EditorObject(defaultGameObject, System.currentTimeMillis());
                addEditorObject(newEditorObject);
                beingDrawn = null;

            }
            if(pressedOn != null) {

                if(e.isPopupTrigger()) {
                    JMenuItem jMenuItem = new JMenuItem();
                    jMenuItem.addActionListener(new DeleteAction(e.getPoint()));
                    jMenuItem.setText("Delete");
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(jMenuItem);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }

                pressedOn.setBeingMoved(false);
                pressedOn = null;
            }
            buttonPressed = -1;
            pressedAt = null;

            repaint();
        }

        public void mouseClicked(MouseEvent m) {
            if (m.getClickCount() == 2) {
                EditorObject doubleClickedOn = isIn(m.getPoint());
                if(doubleClickedOn != null) {
                    AttributesPopup attributesPopup = new AttributesPopup(doubleClickedOn);
                    doubleClickedOn.setSelected(true);
                    repaint();
                    attributesPopup.setLocation(m.getPoint());
                    attributesPopup.setVisible(true);
                    doubleClickedOn.setSelected(false);
                    repaint();
                }
            }
        }

        class AttributesPopup extends JDialog {
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
                    editorObjects.remove(editorObject);
                    editorObjects.add(newEditorObject);
                    toDelete.add(editorObject);

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
            for(EditorObject c : editorObjects) {
                if(c.getArea().contains(p)) {
                    return c;
                }
            }
            return null;
        }

        private boolean isOnAnyEditorObjects(Rectangle r) {
            for(EditorObject c : editorObjects) {
                if(!c.isBeingMoved() && c.getArea().intersects(r)) {
                    return true;
                }
            }
            return false;
        }


        private class DeleteAction implements Action {

            private Point invokedAt;
            private DeleteAction(Point invokedAt) {
                this.invokedAt = invokedAt;
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
                EditorObject invokedOn = isIn(invokedAt);
                if(invokedOn != null) {
                    editorObjects.remove(invokedOn);
                    toDelete.add(invokedOn);
                    repaint();
                }
            }
        }
    }

    public List<EditorObject> getEditorObjects() {
        return new ArrayList<>(editorObjects);
    }

    public List<EditorObject> getToDelete() {
        return toDelete;
    }

    public void clearDeleteList() {
        toDelete.clear();
    }
}
