package com.noomtech.jsw.platformscreen;

import com.noomtech.jsw.platformscreen.frame.GameFrame;
import com.noomtech.jsw.common.utils.Utils;


/**
 * Main entry point.
 * @author Joshua Newman
 */
public class Game {


    public static void main(String[] args) throws Exception {
        Utils.gameIsRunning = true;
        new GameFrame();
    }
}
