package com.noomtech.jsw.game;

import com.noomtech.jsw.common.utils.CommonUtils;


/**
 * Main entry point.
 *
 * The game itself is a simple Manic Miner/Jet Set Willy style game i.e. with controls of "left", "right" and "jump" in a screen full of
 * platforms.
 *
 * There is quite a lot of code around obtaining a high level of precision on the controls, as this is important in a platform game.
 *
 * The game objects are loaded from a database.  The objects themselves are built using the editor.
 *
 * The application runs against a config directory which must be specified as a JVM arg called "config"
 * (see {@link com.noomtech.jsw.common.utils.CommonUtils}).
 *
 * @see com.noomtech.jsw.editor.Main
 * @author Joshua Newman
 */
public class Game_Main {


    public static void main(String[] args) throws Exception {

        CommonUtils.gameIsRunning = true;
        new GameFrame();
    }
}
