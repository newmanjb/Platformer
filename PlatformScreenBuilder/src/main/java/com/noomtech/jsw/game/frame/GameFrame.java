package com.noomtech.jsw.game.frame;

import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.common.utils.db.DatabaseAdapter;
import com.noomtech.jsw.common.utils.db.MongoDBAdapter;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gamethread.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * Holds the {@link GamePanel} and is responsible for loading the game objects from the database that it uses.
 */
public class GameFrame extends JFrame {


    private GamePanel gamePanel;
    private DatabaseAdapter databaseAdapter = MongoDBAdapter.getInstance();


    //@todo - save the screen size in the editor, don't hardcode it
    public GameFrame() throws Exception {

        //Get the config directory
        String config = System.getProperty("config");
        if(config == null || config.equals("")) {
            throw new IllegalArgumentException("No config specified");
        }

        setTitle("Game");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = (int)Math.rint(screenSize.height * 0.92);
        getContentPane().setSize(screenSize);
        getContentPane().setMinimumSize(screenSize);
        getContentPane().setPreferredSize(screenSize);
        setLayout(new GridBagLayout());

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
        buttonPanelConstraints.gridx = 0;
        buttonPanelConstraints.gridy = 0;
        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(ae -> {quit();});
        buttonPanel.add(quitButton, buttonPanelConstraints);

        //Build and add the game panel
        refreshGamePanel();

        GridBagConstraints addButtonPanelConstraints = new GridBagConstraints();
        addButtonPanelConstraints.gridx = 0;
        addButtonPanelConstraints.gridy = 1;
        addButtonPanelConstraints.fill = GridBagConstraints.NONE;
        addButtonPanelConstraints.weightx = 0.0;
        addButtonPanelConstraints.weighty = 0.0;
        getContentPane().add(buttonPanel, addButtonPanelConstraints);

        setLocation(new Point(0,0));

        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    private void quit() {
        System.out.println("Quit by user");
        try {databaseAdapter.shutdown();}catch(Exception e){e.printStackTrace();}
        System.exit(0);
    }

    //Called when the user has completed the current level.  Increments the level (if neccessary) and builds a new panel
    //for the next level and displays it
    public void onLevelFinished() {
        CommonUtils.setCurrentLevel(CommonUtils.getCurrentLevel() + 1);
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {refreshGamePanel();} catch(Exception e) {
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

    private void refreshGamePanel() throws Exception {
        if(gamePanel != null) {
            getContentPane().remove(gamePanel);
            getContentPane().revalidate();

        }
        List<GameObject> gameObjects = databaseAdapter.loadGameObjectsForCurrentLevel();
        gamePanel = new GamePanel(this, gameObjects);

        GridBagConstraints gbcGamePanel = new GridBagConstraints();
        gbcGamePanel.gridx = 0;
        gbcGamePanel.gridy = 0;
        gbcGamePanel.fill = GridBagConstraints.BOTH;
        gbcGamePanel.weightx = 1.0;
        gbcGamePanel.weighty = 1.0;
        getContentPane().add(gamePanel, gbcGamePanel);
        getContentPane().revalidate();

        pack();

        gamePanel.requestFocusInWindow();
    }
}
