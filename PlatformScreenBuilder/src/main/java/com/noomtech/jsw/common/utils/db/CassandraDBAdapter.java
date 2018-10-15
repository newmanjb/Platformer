package com.noomtech.jsw.common.utils.db;

import com.datastax.driver.core.*;
import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.editor.building_blocks.EditorObject;
import com.noomtech.jsw.editor.frame.DrawingPanel;
import com.noomtech.jsw.game.gameobjects.GameObject;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * {@link DatabaseAdapter} for cassandra.
 */
public class CassandraDBAdapter implements DatabaseAdapter {


    private final Session cassandraSession;
    private final Cluster cluster;
    private final PreparedStatement preparedInsertStatement;
    private final PreparedStatement preparedDeleteStatement;


    private CassandraDBAdapter() {
        Cluster.Builder b = Cluster.builder().addContactPoint("10.130.84.52");
        cluster = b.build();
        cassandraSession = cluster.connect();

        preparedInsertStatement = cassandraSession.prepare("INSERT INTO jsw.COLLISION_AREAS (class,id,attributes,rectangle) " +
                "VALUES (?,?,?,?);");
        preparedDeleteStatement = cassandraSession.prepare("DELETE FROM jsw.COLLISION_AREAS WHERE class=? AND id=?");
    }

    private final static class InstanceHolder {
        private static final CassandraDBAdapter instance = new CassandraDBAdapter();
    }


    public static DatabaseAdapter getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public List<GameObject> loadGameObjects() throws Exception {
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
            Rectangle rectangle = CommonUtils.convertFromProportionOfScreenSize(startTuple.get(0, BigDecimal.class), startTuple.get(1,BigDecimal.class),
                    endTuple.get(0,BigDecimal.class), endTuple.get(1,BigDecimal.class));
            String classVal = row.getString(0);
            Map<String,String> attributes = row.getMap(2, String.class, String.class);
            //The type of game object is represented as the full name of the underlying class so as it can be instantiated
            //using reflection
            GameObject gameObject = (GameObject)Class.forName(classVal).getConstructor(
                    Rectangle.class, Map.class).newInstance(rectangle, attributes);
            toReturn.add(gameObject);
        }

        return toReturn;
    }

    @Override
    public void save(DrawingPanel drawingPanel) {
        List<EditorObject> toDeleteList = drawingPanel.getToDelete();
        for(EditorObject c : toDeleteList) {
            BoundStatement boundStatement = preparedDeleteStatement.bind();
            GameObject gameObject = c.getGameObject();
            boundStatement.setString(0, gameObject.getClass().getName());
            boundStatement.setLong(1, c.getId());
            if(!cassandraSession.execute(boundStatement).wasApplied()) {
                throw new IllegalArgumentException("Could not delete editor object " + c);
            }
        }
        drawingPanel.clearDeleteList();

        List<EditorObject> collisionAreaList = drawingPanel.getEditorObjects();
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
            BigDecimal[] convertedVals = CommonUtils.convertToProportionOfScreenSize(c.getArea());
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

    public List<EditorObject> loadEditorObjects() throws Exception {
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
            Rectangle rectangle = CommonUtils.convertFromProportionOfScreenSize(
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

    @Override
    public void shutdown() {
        cassandraSession.close();
        cluster.close();
    }
}
