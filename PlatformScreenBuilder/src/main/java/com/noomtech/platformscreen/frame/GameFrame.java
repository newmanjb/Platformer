package com.noomtech.platformscreen.frame;

import com.datastax.driver.core.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Holds the {@link GamePanel} and is responsible for loading the configuration that it uses.
 */
public class GameFrame extends JFrame {


    private final GamePanel gamePanel;
    private final Session cassandraSession;
    private final Cluster cluster;
    private final Thread gameThread;

    //@todo - save the screen size in the editor, don't hardcode it
    //@todo - refactor the GamePanel class into separate classes as it's too big
    public GameFrame() {

        Cluster.Builder b = Cluster.builder().addContactPoint("10.130.84.52");
        cluster = b.build();
        cassandraSession = cluster.connect();

        gamePanel = new GamePanel(load());

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

        gameThread = new Thread(() -> {
           gamePanel.start();
        });
        gameThread.setName("Game thread");
        gameThread.setDaemon(false);
        gameThread.start();

        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    /**
     * Loads the game data from the cassandra db
     */
    private List<Rectangle> load() {
        String selectStatement = "SELECT class,id,attributes,rectangle FROM jsw.COLLISION_AREAS";
        BoundStatement b = cassandraSession.prepare(selectStatement).bind();
        ResultSet rs = cassandraSession.execute(b);
        List<Rectangle> toReturn = new ArrayList<>();
        List<Row> rows = rs.all();
        for(Row row : rows) {
            String classVal = row.getString(0);
            long id = row.getLong(1);
            Map<String,String> attributes = row.getMap(2, String.class, String.class);
            TupleValue tupleValue = row.getTupleValue(3);
            TupleValue startTuple = tupleValue.get(0,TupleValue.class);
            Point start = new Point(startTuple.get(0,Integer.class), startTuple.get(1,Integer.class));
            TupleValue endTuple = tupleValue.get(1,TupleValue.class);
            Point end = new Point(endTuple.get(0,Integer.class), endTuple.get(1,Integer.class));
            Rectangle rectangle = new Rectangle(start.x, start.y, end.x - start.x, end.y - start.y);

            if(attributes.containsKey("type") && attributes.get("type").equalsIgnoreCase("jsw")) {
                toReturn.add(0, rectangle);
            }
            else {
                toReturn.add(rectangle);
            }
        }
        return toReturn;
    }

    private void quit() {
        gamePanel.stop();
        boolean stopped = false;
        try {
            gameThread.join(3000);
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
