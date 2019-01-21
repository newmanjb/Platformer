package com.noomtech.jsw.editor.gui;

import java.awt.*;


/**
 * Represents anything that can be editied in the editor GUI.
 */
public interface Editable {


    Rectangle getArea();

    void setLocation(Rectangle rectangle);

    void setBeingMoved(boolean beingMoved);

    boolean isBeingMoved();

    boolean isSelected();

    void setSelected(boolean selected);

    void doPainting(Graphics g, DrawingSettings drawingSettings);

    int hashCode();

    boolean equals(Object other);
}
