package com.noomtech.jsw.editor.gui.userinput_processing;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

class ChangeModeAction implements Action {

    private MouseMovementHandler PARENT;
    ChangeModeAction(MouseMovementHandler parent) {
        this.PARENT = parent;
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
        if(PARENT.currentMode == MouseMovementHandler.Mode.NORMAL) {
            PARENT.currentMode = MouseMovementHandler.Mode.COLLISION_AREA;
            PARENT.VIEW.setDrawCollisionAreas(true);
        }
        else if(PARENT.currentMode == MouseMovementHandler.Mode.COLLISION_AREA){
            PARENT.currentMode = MouseMovementHandler.Mode.NORMAL;
            PARENT.VIEW.setDrawCollisionAreas(false);
        }
        else {
            throw new UnsupportedOperationException("Unknown mode " + PARENT.currentMode);
        }
    }
}