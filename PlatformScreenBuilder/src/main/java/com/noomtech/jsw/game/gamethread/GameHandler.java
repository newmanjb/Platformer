package com.noomtech.jsw.game.gamethread;

import javax.swing.*;


/**
 * Handles core operations such as running the game thread and repainting
 * @see GamePanel
 * @author Joshua Newman
 */
public class GameHandler implements Runnable {

    private long repaintInterval;
    private GamePanel gamePanel;

    GameHandler(GamePanel gamePanel, long repaintInterval) {
        this.repaintInterval = repaintInterval;
        this.gamePanel = gamePanel;
    }


    public void run() {
        //The game thread that repaints, sleeps and then repaints again until the game is stopped
        while(gamePanel.isRunning()) {
            try {
                SwingUtilities.invokeLater(() -> gamePanel.repaint());
                Thread.sleep(repaintInterval);
            }
            catch(InterruptedException e) {
                System.out.println("Game loop thread interrupted.  Stopping game");
            }
        }
    }
}
