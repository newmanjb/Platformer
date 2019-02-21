package com.noomtech.jsw.game;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.common.utils.SoundPlayer;
import com.noomtech.jsw.common.utils.db.DatabaseAdapter;
import com.noomtech.jsw.common.utils.db.MongoDBAdapter;
import com.noomtech.jsw.game.events.GameEventReceiver;
import com.noomtech.jsw.game.events.GamePlayListener;
import com.noomtech.jsw.game.gameobjects.GameObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * Holds the {@link GamePlayDisplay} and is responsible for coordinating the higher level operations of the game such as
 * such as moving on a level, loading the game objects or playing the appropriate sounds.
 * @see GamePlayListener
 */
public class GameFrame extends JFrame implements GamePlayListener {


    private GamePlayDisplay gameDisplay;
    private DatabaseAdapter databaseAdapter = MongoDBAdapter.getInstance();


    //@todo - save the screen size in the editor, don't hardcode it
    public GameFrame() throws Exception {

        //Get the config directory
        String config = System.getProperty("config");
        if(config == null || config.equals("")) {
            throw new IllegalArgumentException("No config specified");
        }

        GameEventReceiver.getInstance().addListener(this);

        setTitle("Game");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = (int)Math.rint(screenSize.height * 0.92);
        getContentPane().setSize(screenSize);
        getContentPane().setMinimumSize(screenSize);
        getContentPane().setPreferredSize(screenSize);
        setLayout(new GridBagLayout());

        //Build and add the game panel
        refreshGamePanel();

        setLocation(new Point(0,0));

        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);

        SoundPlayer.getInstance().startSound("inGameMusic");
    }

    private void quit() {
        System.out.println("Quit by user");
        try {databaseAdapter.shutdown();}catch(Exception e){e.printStackTrace();}
        System.exit(0);
    }

    //Called when the user has completed the current level.  Increments the level (if neccessary) and builds a new panel
    //for the next level and displays it
    public void onLevelComplete() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    SoundPlayer.getInstance().stopSound("inGameMusic");
                    SoundPlayer.getInstance().startSound("levelComplete");
                    gameDisplay.stop();
                    Thread.sleep(1000);
                    CommonUtils.setCurrentLevel(CommonUtils.getCurrentLevel() + 1);
                    SoundPlayer.getInstance().startSound("inGameMusic");
                    refreshGamePanel();
                } catch(Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    public void onPlayerFalling() {
        SoundPlayer.getInstance().startSound("playerFalling");
    }
    public void onPlayerStoppedFalling() {
        SoundPlayer.getInstance().stopSound("playerFalling");
    }
    public void onPlayerDied() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    SoundPlayer.getInstance().stopSound("inGameMusic");
                    SoundPlayer.getInstance().startSound("playerDied");
                    gameDisplay.stopMovementComponents();
                    Thread.sleep(700);
                    gameDisplay.setGameObjectsToStartingState();
                    gameDisplay.startMovementComponents();
                    SoundPlayer.getInstance().startSound("inGameMusic");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            });
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    public void onPlayerJumping() {
        SoundPlayer.getInstance().startSound("playerJumping");
    }
    public void onPlayerStoppedJumping() {
        SoundPlayer.getInstance().stopSound("playerJumping");
    }
    public void onPlayerWalking() {
        SoundPlayer.getInstance().startSound("playerWalking");
    }
    public void onPlayerStoppedWalking() {
        SoundPlayer.getInstance().stopSound("playerWalking");
    }


    private void refreshGamePanel() throws Exception {
        if(gameDisplay != null) {
            getContentPane().remove(gameDisplay);
            getContentPane().revalidate();

        }
        List<GameObject> gameObjects = databaseAdapter.loadGameObjectsForCurrentLevel();
        gameDisplay = new GamePlayDisplay(this, gameObjects);

        GridBagConstraints gbcGamePanel = new GridBagConstraints();
        gbcGamePanel.gridx = 0;
        gbcGamePanel.gridy = 0;
        gbcGamePanel.fill = GridBagConstraints.BOTH;
        gbcGamePanel.weightx = 1.0;
        gbcGamePanel.weighty = 1.0;
        getContentPane().add(gameDisplay, gbcGamePanel);
        getContentPane().revalidate();

        pack();

        gameDisplay.requestFocusInWindow();
    }


}
