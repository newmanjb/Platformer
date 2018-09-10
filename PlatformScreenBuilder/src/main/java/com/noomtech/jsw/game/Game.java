package com.noomtech.jsw.game;

import com.noomtech.jsw.game.frame.GameFrame;
import com.noomtech.jsw.common.utils.CommonUtils;


/**
 * Main entry point.
 * @author Joshua Newman
 */
public class Game {


    public static void main(String[] args) throws Exception {
        CommonUtils.gameIsRunning = true;
        new GameFrame();
    }
}
