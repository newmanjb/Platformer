package com.noomtech.platformscreenbuilder.frame;

import com.noomtech.platformscreenbuilder.building_blocks.CollisionArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class DrawingPanel extends JPanel {


    private final List<CollisionArea> collisionAreas;
    private volatile Rectangle beingDrawn;
    private final List<CollisionArea> toDelete = new ArrayList<>();


    DrawingPanel(List<CollisionArea> collisionAreas) {
        this.collisionAreas = collisionAreas;
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

        for(CollisionArea c : collisionAreas) {
            Rectangle r = c.getRectangle();
            if(c.isSelected() && !c.isBeingMoved()) {
                g.setColor(Color.BLUE);
                g.fillRect(r.x,r.y,r.width,r.height);
            }

            g.setColor(getColourForOutline(c));
            g.drawRect(r.x, r.y, r.width, r.height);

        }

        if(beingDrawn != null) {
            g.setColor(Color.GREEN);
            g.drawRect(beingDrawn.x, beingDrawn.y, beingDrawn.width, beingDrawn.height);
        }
    }

    private Color getColourForOutline(CollisionArea c) {
        if(c.isBeingMoved()) {
            return Color.RED;
        }
        else {
            return Color.BLACK;
        }
    }

    void addCollisionArea(CollisionArea c) {
        collisionAreas.add(c);
    }


    private class MouseMovementHandler extends MouseAdapter {


        private Point pressedAt;
        private CollisionArea pressedOn;
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
                    if (!isOnAnyCollisionAreas(r)) {
                        beingDrawn = r;
                        repaint();
                    }
                } else {
                    pressedOn.setBeingMoved(true);
                    Rectangle newArea = new Rectangle(pressedOn.getRectangle());
                    int xDifference = whereWeAreNow.x - pressedAt.x;
                    int yDifference = whereWeAreNow.y - pressedAt.y;
                    Point p = newArea.getLocation();
                    p.x += xDifference;
                    p.y += yDifference;
                    newArea.setLocation(p);
                    if(!isOnAnyCollisionAreas(newArea)) {
                        pressedOn.setRectangle(newArea);
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
                CollisionArea newCollisionArea = new CollisionArea(new Rectangle(beingDrawn), System.currentTimeMillis());
                addCollisionArea(newCollisionArea);
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
                CollisionArea doubleClickedOn = isIn(m.getPoint());
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
            private CollisionArea collisionArea;
            AttributesPopup(CollisionArea collisionArea) {
                super((JFrame)null, "Attributes", true);
                this.collisionArea = collisionArea;
                setLayout(new BorderLayout());
                JTextField classField = new JTextField();
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
                    collisionArea.setParams(attributes);
                });

                Map<String,String> attributes = collisionArea.getParams();
                if(!attributes.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        sb.append(entry.getKey() + "=" + entry.getValue() + ",");
                    }
                    textArea.setText(sb.substring(0, sb.length() - 1));
                }
                add(classField, BorderLayout.NORTH);
                add(textArea, BorderLayout.CENTER);
                add(saveButton, BorderLayout.SOUTH);
                Dimension size = new Dimension(350,350);
                setPreferredSize(size);
                setMinimumSize(size);
                setMaximumSize(size);

                pack();
            }
        }

        private CollisionArea isIn(Point p) {
            for(CollisionArea c : collisionAreas) {
                if(c.getRectangle().contains(p)) {
                    return c;
                }
            }
            return null;
        }

        private boolean isOnAnyCollisionAreas(Rectangle r) {
            for(CollisionArea c : collisionAreas) {
                if(!c.isBeingMoved() && c.getRectangle().intersects(r)) {
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
                CollisionArea invokedOn = isIn(invokedAt);
                if(invokedOn != null) {
                    collisionAreas.remove(invokedOn);
                    toDelete.add(invokedOn);
                    repaint();
                }
            }
        }
    }

    List<CollisionArea> getCollisionAreas() {
        return new ArrayList<>(collisionAreas);
    }

    List<CollisionArea> getToDelete() {
        return toDelete;
    }

    void clearDeleteList() {
        toDelete.clear();
    }
}
