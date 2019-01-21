package com.noomtech.jsw.editor.building_blocks;

import com.noomtech.jsw.editor.gui.DrawingSettings;
import com.noomtech.jsw.editor.gui.Editable;

import java.awt.*;

public class CollisionAreaEditableObject implements Editable {

    private Rectangle collisionArea;
    private boolean beingMoved;
    private boolean isSelected;

    public CollisionAreaEditableObject(Rectangle rectangle) {
        this.collisionArea = rectangle;
    }

    @Override
    public Rectangle getArea() {
        return collisionArea;
    }

    @Override
    public void setLocation(Rectangle rectangle) {
        collisionArea = rectangle;
    }

    @Override
    public void setBeingMoved(boolean beingMoved) {
        this.beingMoved = beingMoved;
    }

    @Override
    public boolean isBeingMoved() {
        return beingMoved;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean isSelected) { this.isSelected = isSelected; }

    @Override
    public void doPainting(Graphics g, DrawingSettings drawingSettings) {
        if(drawingSettings.getDrawCollisionAreas()) {
            g.setColor(Color.BLACK);
            g.drawRect(collisionArea.x, collisionArea.y, collisionArea.width, collisionArea.height);
        }
    }
}
