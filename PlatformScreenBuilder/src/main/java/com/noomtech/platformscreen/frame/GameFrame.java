package com.noomtech.platformscreen.frame;

import com.datastax.driver.core.*;
import com.noomtech.platformscreenbuilder.building_blocks.CollisionArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GameFrame extends JFrame {


    private final GamePanel gamePanel;
    private final Session cassandraSession;
    private final Thread gameThread;


    public GameFrame() {

        Cluster.Builder b = Cluster.builder().addContactPoint("10.130.84.52");
        Cluster cluster = b.build();
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
            stopped = true;
        }
        catch(Exception e) {
            System.out.println("Interrupted while waiting for game thread!!  Shouldn't happen.");
        }

        if(!stopped) {
            System.out.println("Game didn't shut down properly");
        }

        setVisible(false);
        dispose();
    }
}
