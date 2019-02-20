package com.noomtech.jsw.editor.gui.userinput_processing;

import com.noomtech.jsw.editor.building_blocks.CollisionAreaEditableObject;
import com.noomtech.jsw.editor.gui.Editable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

class DeleteAction implements Action {

    private Editable toDelete;
    private MouseMovementHandler PARENT;
    DeleteAction(Editable toDelete, MouseMovementHandler parent) {
        this.toDelete = toDelete;
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
            PARENT.MODEL.remove(toDelete);
            PARENT.record_editableRemoved(PARENT.rootObjectPressedOn, PARENT.rootObjectPressedOn);
        }
        else if(PARENT.currentMode == MouseMovementHandler.Mode.COLLISION_AREA){
            PARENT.rootObjectPressedOn.removeCollisionArea((CollisionAreaEditableObject)toDelete);
            PARENT.record_editableRemoved(PARENT.rootObjectPressedOn, toDelete);
        }
        else {
            throw new IllegalStateException("Unsupported mode");
        }
    }
}
