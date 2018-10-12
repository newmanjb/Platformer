package com.noomtech.jsw.common.utils.db;

import com.noomtech.jsw.editor.building_blocks.EditorObject;
import com.noomtech.jsw.editor.frame.DrawingPanel;
import com.noomtech.jsw.game.gameobjects.GameObject;

import java.util.List;


/**
 * Facilitates an adapter pattern where different databases can be used to load and save the game data.
 */
public interface DatabaseAdapter {


    List<GameObject> loadGameObjects() throws Exception;

    List<EditorObject> loadEditorObjects() throws Exception;

    void save(DrawingPanel drawingPanel) throws Exception;

    void shutdown() throws Exception;
}
