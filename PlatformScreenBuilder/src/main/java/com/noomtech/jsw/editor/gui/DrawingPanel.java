package com.noomtech.jsw.editor.gui;

import com.noomtech.jsw.editor.building_blocks.RootObject;
import com.noomtech.jsw.editor.gui.userinput_processing.MouseMovementHandler;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * Responsible for the painting.  This is the "view" in the MVC pattern used in the editor.
 * The {@link MouseMovementHandler} is the controller.  The collection of root objects is the data.
 * @todo - updating the image for the game objects in the editor is permanent i.e. doesn't make a difference if you
 * quit without saving
 */
public class DrawingPanel extends JPanel {


    private final List<RootObject> rootObjects;

    private volatile Rectangle beingDrawn;

    private DrawingSettings drawingSettings = new DrawingSettings();


    DrawingPanel(List<RootObject> rootObjects) {
        this.rootObjects = rootObjects;
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.WHITE);
        Dimension size = getSize();
        g.fillRect(0,0,size.width,size.height);

        for(Editable c : rootObjects) {
            Rectangle r = c.getArea();
            if(c.isSelected() && !c.isBeingMoved()) {
                g.setColor(Color.RED);
                g.drawRect(r.x,r.y,r.width,r.height);
            }
            else {
                c.doPainting(g, drawingSettings);
            }
        }

        if(beingDrawn != null) {
            g.setColor(Color.GREEN);
            g.drawRect(beingDrawn.x, beingDrawn.y, beingDrawn.width, beingDrawn.height);
        }
    }

    public void setDrawCollisionAreas(boolean b) {
        drawingSettings = drawingSettings.drawCollisionAreas(b);
        repaint();
    }

    public void setSelected(Editable editable, boolean selected) {
        editable.setSelected(selected);
        repaint();
    }

    public Rectangle getBeingDrawn() {
        return beingDrawn;
    }

    public void setBeingDrawn(Rectangle beingDrawn) {
        this.beingDrawn = beingDrawn;
        repaint();
    }

    public boolean intersectsWithAnyRootObjects(Rectangle rectangle) {
        for(RootObject rootObject : rootObjects) {
            if(rectangle.intersects(rootObject.getArea())) {
                return true;
            }
        }
        return false;
    }

    public void refreshAllImages() {
        //@todo - doing ALL root objects here.  If this ever gets slow then we'll need to add a mechanism which
        //only does the root objects whos images have been updated.
        for(RootObject rootObject : rootObjects) {
            rootObject.onImageUpdated();
        }
        repaint();
    }

    public Dimension getScreenSize() {
        return getSize();
    }
}
