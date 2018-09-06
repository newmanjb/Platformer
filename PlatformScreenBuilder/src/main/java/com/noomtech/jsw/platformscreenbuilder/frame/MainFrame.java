package com.noomtech.jsw.platformscreenbuilder.frame;

import com.datastax.driver.core.*;
import com.noomtech.jsw.platformscreen.gameobjects.GameObject;
import com.noomtech.jsw.platformscreenbuilder.building_blocks.EditorObject;
import com.noomtech.jsw.common.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainFrame extends JFrame {


    private DrawingPanel drawingPanel;
    private Session cassandraSession;


    public MainFrame() throws Exception {

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

    private List<EditorObject> load() throws Exception {
        String selectStatement = "SELECT class,id,attributes,rectangle FROM jsw.COLLISION_AREAS";
        BoundStatement b = cassandraSession.prepare(selectStatement).bind();
        ResultSet rs = cassandraSession.execute(b);
        List<EditorObject> toReturn = new ArrayList<>();
        List<Row> rows = rs.all();
        for(Row row : rows) {
            String classVal = row.getString(0);
            long id = row.getLong(1);
            Map<String,String> attributes = row.getMap(2, String.class, String.class);
            TupleValue tupleValue = row.getTupleValue(3);
            TupleValue startTuple = tupleValue.get(0,TupleValue.class);
            TupleValue endTuple = tupleValue.get(1,TupleValue.class);

            //Pixel values such as x, y, width and height have to be converted first, as they are stored as proportions
            //of the screen size (see the save functionality)
            Rectangle rectangle = Utils.convertFromProportionOfScreenSize(
                    startTuple.get(0, BigDecimal.class), startTuple.get(1, BigDecimal.class),
                    endTuple.get(0, BigDecimal.class), endTuple.get(1, BigDecimal.class));

            //The type of game object is represented as the full name of the underlying class so as it can be instantiated
            //using reflection
            GameObject gameObject = (GameObject)Class.forName(classVal).getConstructor(Rectangle.class, Map.class).newInstance(
                    rectangle, attributes);

            EditorObject editorObject = new EditorObject(gameObject, id);
            toReturn.add(editorObject);
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

            List<EditorObject> toDeleteList = drawingPanel.getToDelete();
            for(EditorObject c : toDeleteList) {
                BoundStatement boundStatement = preparedDeleteStatement.bind();
                GameObject gameObject = c.getGameObject();
                boundStatement.setString(0, gameObject.getClass().getName());
                boundStatement.setLong(1, c.getId());
                if(!cassandraSession.execute(boundStatement).wasApplied()) {
                    throw new IllegalArgumentException("Didn't work!");
                }
            }
            drawingPanel.clearDeleteList();

            List<EditorObject> collisionAreaList = drawingPanel.getCollisionAreas();
            for(EditorObject c : collisionAreaList) {
                BoundStatement boundStatement = preparedInsertStatement.bind();

                GameObject gameObject = c.getGameObject();
                if(gameObject == null) {
                    //Better to catch this here rather than save a corrupt config
                    throw new IllegalArgumentException("Game object should never be null");
                }
                String classVal;
                Map<String,String> attributesToSave = null;
                //The type of game object is represented as the full name of the underlying class so as it can be instantiated
                //using reflection
                classVal = gameObject.getClass().getName();
                Map<String,String> attributes = gameObject.getAttributes();
                if(attributes != null && !attributes.isEmpty()) {
                    attributesToSave = attributes;
                }

                boundStatement.setString(0, classVal);
                boundStatement.setLong(1, c.getId());

                if(attributesToSave == null) {
                    boundStatement.setToNull(2);
                }
                else {
                    boundStatement.setMap(2, attributesToSave);
                }

                //Pixel values such as x, y, width and height are converted to be proprtions of the scrren size first
                //before being saved e.g. if the scrren size width is 1500 and the value is 300 then the value will be
                //saved as 0.2. This way the editor will work on any screen size.
                BigDecimal[] convertedVals = Utils.convertToProportionOfScreenSize(c.getRectangle());
                TupleType t = TupleType.of(ProtocolVersion.NEWEST_SUPPORTED, CodecRegistry.DEFAULT_INSTANCE,
                        DataType.decimal(), DataType.decimal());
                TupleValue start = t.newValue(convertedVals[0], convertedVals[1]);
                TupleValue end = t.newValue(convertedVals[0].add(convertedVals[2]), convertedVals[1].add(convertedVals[3]));
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
