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

    //Should be called after the image for the object has been updated so as the object can rebuild itself
    void onImageUpdated();

    /**
     * @return false if this object cannot be copied
     */
    boolean supportsCopy();

    /**
     * @return A deep-copy of this object
     */
    Object copy() throws Exception;
}
