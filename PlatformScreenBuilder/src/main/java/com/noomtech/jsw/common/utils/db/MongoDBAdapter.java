package com.noomtech.jsw.common.utils.db;


import com.noomtech.jsw.editor.building_blocks.EditorObject;
import com.noomtech.jsw.editor.frame.DrawingPanel;
import com.noomtech.jsw.game.gameobjects.GameObject;

import java.util.List;

/**
 * {@link DatabaseAdapter} for mongo db.
 */
public class MongoDBAdapter implements DatabaseAdapter {


    @Override
    public List<GameObject> loadGameObjects() throws Exception {
        return null;
    }

    @Override
    public List<EditorObject> loadEditorObjects() throws Exception {
        return null;
    }

    @Override
    public void save(DrawingPanel drawingPanel) throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
