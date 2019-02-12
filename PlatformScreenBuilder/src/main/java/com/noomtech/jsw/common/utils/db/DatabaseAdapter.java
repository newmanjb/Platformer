package com.noomtech.jsw.common.utils.db;

import com.noomtech.jsw.editor.building_blocks.RootObject;
import com.noomtech.jsw.editor.gui.DrawingPanel;
import com.noomtech.jsw.editor.gui.userinput_processing.MouseMovementHandler;
import com.noomtech.jsw.game.gameobjects.GameObject;

import java.util.List;


/**
 * Facilitates an adapter pattern where different databases can be used to load and save the game data.
 */
public interface DatabaseAdapter {


    List<GameObject> loadGameObjectsForCurrentLevel() throws Exception;

    List<RootObject> loadEditorObjectsForLevel(int level) throws Exception;

    void save(MouseMovementHandler controller) throws Exception;

    void shutdown() throws Exception;
}
