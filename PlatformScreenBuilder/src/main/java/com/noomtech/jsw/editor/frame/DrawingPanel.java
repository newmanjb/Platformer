package com.noomtech.jsw.editor.frame;

import com.noomtech.jsw.editor.building_blocks.EditorObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DrawingPanel extends JPanel {


    private final List<EditorObject> editorObjects;
    private final List<EditorObject> toDelete = new ArrayList<>();
    private volatile Rectangle beingDrawn;


    DrawingPanel(List<EditorObject> editorObjects) {
        this.editorObjects = editorObjects;
        MouseMovementHandler mouseMovementHandler = new MouseMovementHandler(this);
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

    void removeEditorObject(EditorObject toRemove) {
        editorObjects.remove(toRemove);
    }

    void addToDeleteList(EditorObject objectToDelete) {
        toDelete.add(objectToDelete);
    }

    Rectangle getBeingDrawn() {
        return beingDrawn;
    }

    void setBeingDrawn(Rectangle beingDrawn) {
        this.beingDrawn = beingDrawn;
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
