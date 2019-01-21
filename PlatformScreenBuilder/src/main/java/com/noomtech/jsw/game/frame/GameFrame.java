package com.noomtech.jsw.game.frame;

import com.noomtech.jsw.common.utils.db.DatabaseAdapter;
import com.noomtech.jsw.common.utils.db.MongoDBAdapter;
import com.noomtech.jsw.game.gameobjects.GameObject;
import com.noomtech.jsw.game.gamethread.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;


/**
 * Holds the {@link GamePanel} and is responsible for loading the configuration that it uses.
 */
public class GameFrame extends JFrame {


    private final GamePanel gamePanel;
    private DatabaseAdapter databaseAdapter;


    //@todo - save the screen size in the editor, don't hardcode it
    public GameFrame() throws Exception {

        databaseAdapter = MongoDBAdapter.getInstance();
        List<GameObject> gameObjects = databaseAdapter.loadGameObjects();

//        Useful for drawing test platforms and nasties if we can't connect to the db
//
//        List<GameObject> gameObjects = new ArrayList<>();
//        JSW jsw = new JSW(new Rectangle(800, 800, 100, 100), null);
//        StaticLethalObject s1 = new StaticLethalObject(new Rectangle(450, 850, 50, 50), null);
//        FinishingObject f1 = new FinishingObject(new Rectangle(1200, 850, 50, 50), null);
//        Platform p1 = new Platform(new Rectangle(400, 900, 1000, 50), null);
//        Platform p2 = new Platform(new Rectangle(450, 840, 40, 10), null);
//        Platform p3 = new Platform(new Rectangle(1200, 840, 40, 10), null);
//        Platform p4 = new Platform(new Rectangle(600, 730, 200, 20), null);
//
//        gameObjects.add(jsw);
//        gameObjects.add(p1);
//        gameObjects.add(p2);
//        gameObjects.add(p3);
//        gameObjects.add(p4);
//        gameObjects.add(s1);
//        gameObjects.add(f1);

        gamePanel = new GamePanel(
                gameObjects);

        setTitle("Game");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = (int)Math.rint(screenSize.height * 0.92);
        getContentPane().setSize(screenSize);
        getContentPane().setMinimumSize(screenSize);
        getContentPane().setPreferredSize(screenSize);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc0 = new GridBagConstraints();
        gbc0.gridx = 0;
        gbc0.gridy = 0;
        gbc0.fill = GridBagConstraints.BOTH;
        gbc0.weightx = 1.0;
        gbc0.weighty = 1.0;
        getContentPane().add(gamePanel, gbc0);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(ae -> {quit();});
        buttonPanel.add(quitButton, gbc1);

        gbc0.gridy = 1;
        gbc0.fill = GridBagConstraints.NONE;
        gbc0.weightx = 0.0;
        gbc0.weighty = 0.0;
        getContentPane().add(buttonPanel, gbc0);

        setLocation(new Point(0,0));

        gamePanel.start();

        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    private void quit() {
        try {
            gamePanel.stop();
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }

        boolean stopped = false;
        try {
            databaseAdapter.shutdown();
            stopped = true;
        }
        catch(Exception e) {
            System.out.println("Exception while shutting down");
            e.printStackTrace();
        }

        if(!stopped) {
            System.out.println("Game didn't shut down properly");
        }

        setVisible(false);
        dispose();
    }
}
