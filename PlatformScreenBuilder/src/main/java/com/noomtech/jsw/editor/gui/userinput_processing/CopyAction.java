package com.noomtech.jsw.editor.gui.userinput_processing;

import com.noomtech.jsw.editor.building_blocks.RootObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

class CopyAction implements Action {

    private RootObject pressedOn;
    private MouseMovementHandler parent;
    CopyAction(RootObject pressedOn, MouseMovementHandler parent) {
        this.pressedOn = pressedOn;
        this.parent = parent;
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
        parent.copied = pressedOn;
    }
}
