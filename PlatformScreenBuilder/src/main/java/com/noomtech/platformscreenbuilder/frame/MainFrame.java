package com.noomtech.platformscreenbuilder.frame;

import com.datastax.driver.core.*;
import com.noomtech.platformscreenbuilder.building_blocks.CollisionArea;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;


//@todo - 1: Save the size of the drawing panel
public class MainFrame extends JFrame {


    private DrawingPanel drawingPanel;
    private Session cassandraSession;


    public MainFrame() {

        Cluster.Builder b = Cluster.builder().addContactPoint("10.130.84.52");
        Cluster cluster = b.build();
        cassandraSession = cluster.connect();

        setTitle("Collision Editor");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = (int)Math.rint(screenSize.height * 0.92);
        getContentPane().setSize(screenSize);
        getContentPane().setMinimumSize(screenSize);
        getContentPane().setPreferredSize(screenSize);
        drawingPanel = new DrawingPanel(load());
        setLayout(new GridBagLayout());
        GridBagConstraints gbc0 = new GridBagConstraints();
        gbc0.gridx = 0;
        gbc0.gridy = 0;
        gbc0.fill = GridBagConstraints.BOTH;
        gbc0.weightx = 1.0;
        gbc0.weighty = 1.0;
        getContentPane().add(drawingPanel, gbc0);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new SaveActionListener());
        buttonPanel.add(saveButton, gbc1);

        gbc0.gridy = 1;
        gbc0.fill = GridBagConstraints.NONE;
        gbc0.weightx = 0.0;
        gbc0.weighty = 0.0;
        getContentPane().add(buttonPanel, gbc0);

        setLocation(new Point(0,0));

        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    private List<CollisionArea> load() {
        String selectStatement = "SELECT class,id,attributes,rectangle FROM jsw.COLLISION_AREAS";
        BoundStatement b = cassandraSession.prepare(selectStatement).bind();
        ResultSet rs = cassandraSession.execute(b);
        List<CollisionArea> toReturn = new ArrayList<>();
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
            toReturn.add(new CollisionArea(rectangle, attributes, classVal, id));
        }
        return toReturn;
    }

    private class SaveActionListener implements ActionListener {
        private final PreparedStatement preparedInsertStatement;
        private final PreparedStatement preparedDeleteStatement;
        private SaveActionListener() {
            preparedInsertStatement = cassandraSession.prepare("INSERT INTO jsw.COLLISION_AREAS (class,id,attributes,rectangle) " +
                    "VALUES (?,?,?,?);");
            preparedDeleteStatement = cassandraSession.prepare("DELETE FROM jsw.COLLISION_AREAS WHERE class=? AND id=?");
        }
        public void actionPerformed(ActionEvent e) {

            List<CollisionArea> toDeleteList = drawingPanel.getToDelete();
            for(CollisionArea c : toDeleteList) {
                BoundStatement boundStatement = preparedDeleteStatement.bind();
                boundStatement.setString(0, c.getTheClass());
                boundStatement.setLong(1, c.getId());
                if(!cassandraSession.execute(boundStatement).wasApplied()) {
                    throw new IllegalArgumentException("Didn't work!");
                }
            }
            drawingPanel.clearDeleteList();

            List<CollisionArea> collisionAreaList = drawingPanel.getCollisionAreas();
            for(CollisionArea c : collisionAreaList) {
                BoundStatement boundStatement = preparedInsertStatement.bind();
                boundStatement.setString(0, c.getTheClass());
                boundStatement.setLong(1, c.getId());
                boundStatement.setMap(2,c.getParams());
                Rectangle r = c.getRectangle();
                TupleType t = TupleType.of(ProtocolVersion.NEWEST_SUPPORTED, CodecRegistry.DEFAULT_INSTANCE,
                        DataType.cint(), DataType.cint());
                TupleValue start = t.newValue(r.x, r.y);
                TupleValue end = t.newValue(r.x + r.width, r.y + r.height);
                TupleType t1 = TupleType.of(ProtocolVersion.NEWEST_SUPPORTED, CodecRegistry.DEFAULT_INSTANCE,
                        t, t);
                TupleValue rectangleVal = t1.newValue(start,end);
                boundStatement.setTupleValue(3, rectangleVal);

                if(!cassandraSession.execute(boundStatement).wasApplied()) {
                    throw new IllegalArgumentException("Didn't work!");
                }
            }
        }
    }
}
