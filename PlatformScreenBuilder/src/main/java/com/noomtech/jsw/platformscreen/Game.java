package com.noomtech.jsw.platformscreen;

import com.noomtech.jsw.platformscreen.frame.GameFrame;
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
