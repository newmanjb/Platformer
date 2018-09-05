package com.noomtech.platformscreen.frame;

import com.datastax.driver.core.*;
import com.noomtech.platformscreen.gameobjects.*;
import com.noomtech.platformscreen.gamethread.GamePanel;
import com.noomtech.platformscreenbuilder.utils.EditorUtils;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;


/**
 * Holds the {@link GamePanel} and is responsible for loading the configuration that it uses.
 */
public class GameFrame extends JFrame {


    private final GamePanel gamePanel;
    private final Session cassandraSession;
    private final Cluster cluster;

    //@todo - save the screen size in the editor, don't hardcode it
    public GameFrame() throws Exception {

        Cluster.Builder b = Cluster.builder().addContactPoint("10.130.84.52");
        cluster = b.build();
        cassandraSession = cluster.connect();

        List<GameObject> gameObjects = load();
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

    /**
     * Loads the game data from the cassandra db.
     */
    private List<GameObject> load() throws Exception {

        String selectStatement = "SELECT class,id,attributes,rectangle FROM jsw.COLLISION_AREAS";
        BoundStatement b = cassandraSession.prepare(selectStatement).bind();
        ResultSet rs = cassandraSession.execute(b);
        List<Row> rows = rs.all();
        List<GameObject> toReturn = new ArrayList<>();
        for(Row row : rows) {

            TupleValue tupleValue = row.getTupleValue(3);
            TupleValue startTuple = tupleValue.get(0,TupleValue.class);
            TupleValue endTuple = tupleValue.get(1,TupleValue.class);
            //Pixel values such as x, y, width and height are converted to be proprtions of the scrren size first
            //before being saved e.g. if the scrren size width is 1500 and the value is 300 then the value will be
            //saved as 0.2. So they must be converted back before being used to draw objects.  This way the game is portable
            //across different resolutions and monitors.
            Rectangle rectangle = EditorUtils.convertFromProportionOfScreenSize(startTuple.get(0,BigDecimal.class), startTuple.get(1,BigDecimal.class),
                    endTuple.get(0,BigDecimal.class), endTuple.get(1,BigDecimal.class));
            String classVal = row.getString(0);
            Map<String,String> attributes = row.getMap(2, String.class, String.class);
            //The type of game object is represented as the full name of the underlying class so as it can be instantiated
            //using reflection
            GameObject gameObject = (GameObject)Class.forName(classVal).getConstructor(Rectangle.class).newInstance(rectangle);
            gameObject.setAttributes(attributes);
            toReturn.add(gameObject);
        }

        return toReturn;
    }

    private void quit() {
        gamePanel.stop();
        boolean stopped = false;
        try {
            cassandraSession.close();
            cluster.close();
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
