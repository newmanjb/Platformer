package com.noomtech.jsw.editor;

import com.noomtech.jsw.editor.gui.MainFrame;
import com.noomtech.jsw.game.Game_Main;


/**
 * The entry point for the editor.
 *
 * The game objects are loaded from and saved to a database.
 *
 * The application runs against a config directory which must be specified as a JVM arg called "config".
 * This is also where all the images for the game objects will be stored.
 *
 * @see com.noomtech.jsw.common.utils.db.DatabaseAdapter
 * @see Game_Main
 * @see com.noomtech.jsw.common.utils.CommonUtils
 */
public class Main {


    public static void main(String[] args) throws Exception {
        new MainFrame();
    }
}
