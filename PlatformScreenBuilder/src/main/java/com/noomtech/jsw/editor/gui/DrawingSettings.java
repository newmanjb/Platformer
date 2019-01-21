package com.noomtech.jsw.editor.gui;

public class DrawingSettings implements Cloneable {

    private boolean drawCollisionAreas;

    public DrawingSettings drawCollisionAreas(boolean b) {

        DrawingSettings newSettings = getClonedDrawingSettings();
        newSettings.drawCollisionAreas = b;
        return newSettings;
    }

    private DrawingSettings getClonedDrawingSettings() {
        DrawingSettings newSettings = null;
        try {
            newSettings = (DrawingSettings) clone();
        }
        catch(CloneNotSupportedException c) {
            System.out.println("Must implement Cloneable!");
            System.exit(1);
        }
        return newSettings;
    }

    public boolean getDrawCollisionAreas() {
        return drawCollisionAreas;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
