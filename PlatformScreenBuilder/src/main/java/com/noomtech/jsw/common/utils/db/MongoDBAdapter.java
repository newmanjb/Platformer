package com.noomtech.jsw.common.utils.db;


import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.editor.building_blocks.EditorObject;
import com.noomtech.jsw.editor.frame.DrawingPanel;
import com.noomtech.jsw.game.gameobjects.GameObject;
import org.bson.Document;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * {@link DatabaseAdapter} for mongo db.
 */
public class MongoDBAdapter implements DatabaseAdapter {


    private final MongoDatabase database;
    private static final String COLLECTION_NAME = "jsw";

    private MongoDBAdapter() {
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        database = mongo.getDatabase("jsw");
    }
    private static final class InstanceHolder {
        private static final MongoDBAdapter mongoDBAdapter = new MongoDBAdapter();
    }


    public static final DatabaseAdapter getInstance() {
        return InstanceHolder.mongoDBAdapter;
    }


    @Override
    public List<GameObject> loadGameObjects() throws Exception {

        MongoCollection<Document> collection = getCollection();
        FindIterable<Document> documents = collection.find();
        List<GameObject> gameObjects = new ArrayList<>();
        documents.forEach(new Consumer<Document>() {
            public void accept(Document d) {
                try {
                    gameObjects.add(buildFromDocument(d));
                }
                catch(Exception e) {
                    e.printStackTrace();
                    throw new IllegalStateException(e);
                }
            }
        });

        return gameObjects;
    }

    @Override
    public List<EditorObject> loadEditorObjects() throws Exception {

        MongoCollection<Document> collection = getCollection();
        FindIterable<Document> documents = collection.find();
        List<EditorObject> editorObjects = new ArrayList<>();
        documents.forEach(new Consumer<Document>() {
            public void accept(Document d) {
                try {
                    editorObjects.add(new EditorObject(buildFromDocument(d), d.getLong("_id")));
                }
                catch(Exception e) {
                    e.printStackTrace();
                    throw new IllegalStateException(e);
                }
            }
        });

        return editorObjects;
    }

    private GameObject buildFromDocument(Document d) throws Exception {
        String classVal = d.getString("class");
        List<String> rectangleVals = d.get("rectangle", List.class);
        List<BigDecimal> rectangleBds = rectangleVals.stream().map(str -> {return new BigDecimal(str);}).collect(Collectors.toList());
        //Convert back from proportions of the screen size
        Rectangle rectangle = CommonUtils.convertFromProportionOfScreenSize(rectangleBds.get(0), rectangleBds.get(1), rectangleBds.get(2), rectangleBds.get(3));
        Map<String, String> attributes = (Map<String,String>)d.get("attributes");
        //The type of game object is represented as the full name of the underlying class so as it can be instantiated
        //using reflection
        GameObject gameObject = (GameObject) Class.forName(classVal).getConstructor(Rectangle.class, Map.class).newInstance(
                rectangle, attributes);
        return gameObject;
    }

    @Override
    public void save(DrawingPanel drawingPanel) {

        MongoCollection<Document> collection = getCollection();
        List<EditorObject> toDeleteList = drawingPanel.getToDelete();
        for(EditorObject toDelete : toDeleteList) {
            DeleteResult deleteResult = collection.deleteOne(Filters.eq("_id", toDelete.getId()));
            if(!deleteResult.wasAcknowledged()) {
                throw new IllegalStateException("Could not delete editor object " + toDelete);
            }
        }

        drawingPanel.clearDeleteList();

        List<EditorObject> toSaveList = drawingPanel.getEditorObjects();

        UpdateOptions uo = new UpdateOptions();
        uo.upsert(true);
        for(EditorObject toSave :  toSaveList) {
            GameObject go = toSave.getGameObject();
            if(go == null) {
                //Better to catch this here rather than save a corrupt config
                throw new IllegalArgumentException("Cannot save a null game object");
            }

            Document documentToSave = new Document();
            //The type of game object is represented as the full name of the underlying class so as it can be instantiated
            //using reflection
            documentToSave.put("class", go.getClass().getName());
            documentToSave.put("_id", toSave.getId());
            documentToSave.put("attributes", toSave.getGameObject().getAttributes());

            //Pixel values such as x, y, width and height are converted to be proportions of the scrren size first
            //before being saved e.g. if the screen size width is 1500 and the value is 300 then the value will be
            //saved as 0.2. This way the editor will work on any screen size.
            BigDecimal[] convertedVals = CommonUtils.convertToProportionOfScreenSize(toSave.getArea());
            //The top-left and bottom-right coordinates of the rectangle are saved
            convertedVals[2] = convertedVals[2].add(convertedVals[0]);
            convertedVals[3] = convertedVals[3].add(convertedVals[1]);

            //Can't find a codec for BigDecimal so save the large numbers as strings
            List<String> stringVals = Arrays.stream(convertedVals).map(bd->{return bd.toPlainString();}).collect(Collectors.toList());
            documentToSave.put("rectangle", stringVals);

            //"Upsert"
            collection.replaceOne(Filters.eq("_id", toSave.getId()), documentToSave, ReplaceOptions.createReplaceOptions(uo));
        }
    }

    @Override
    public void shutdown() {

    }


    private MongoCollection<Document> getCollection() {
        MongoIterable<String> collections = database.listCollectionNames();
        String collectionNameFromDB;
        boolean found = false;
        Iterator<String> iter = collections.iterator();
        while((collectionNameFromDB = iter.next()) != null && !found) {
            found = collectionNameFromDB.equals(COLLECTION_NAME);
        }

        if(!found) {
            database.createCollection(COLLECTION_NAME);
        }
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        return collection;
    }
}
