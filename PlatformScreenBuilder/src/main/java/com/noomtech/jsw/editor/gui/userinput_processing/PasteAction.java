package com.noomtech.jsw.editor.gui.userinput_processing;

import com.noomtech.jsw.editor.building_blocks.RootObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

 class PasteAction implements Action {
    private Point pastedAt;
    private MouseMovementHandler PARENT;

    PasteAction(Point pastedAt, MouseMovementHandler parent) {
        this.pastedAt = pastedAt;
        this.PARENT = parent;
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
            RootObject copy = (RootObject) PARENT.copied.copy();
            copy.setLocation(new Rectangle(pastedAt.x, pastedAt.y, copy.getArea().width, copy.getArea().height));
            PARENT.MODEL.add(copy);
            PARENT.record_rootObjectAdded(copy);
        }
        catch(Exception ex) {
            System.out.println("Problem copying object:  " + ex);
        }
    }
}