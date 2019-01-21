package com.noomtech.jsw.common.utils.db;


import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.noomtech.jsw.common.utils.CommonUtils;
import com.noomtech.jsw.editor.building_blocks.RootObject;
import com.noomtech.jsw.editor.gui.Saveable;
import com.noomtech.jsw.editor.gui.userinput_processing.MouseMovementHandler;
import com.noomtech.jsw.game.gameobjects.GameObject;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
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
    private Map<String,MongoCollection<Document>> collectionMap = new HashMap<>();

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

        MongoCollection<Document> collection = getCollection(DBConstants.COLLECTION_NAME_GAME_OBJECTS);
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
    public List<RootObject> loadEditorObjects() throws Exception {

        MongoCollection<Document> collection = getCollection(DBConstants.COLLECTION_NAME_GAME_OBJECTS);
        FindIterable<Document> documents = collection.find();
        List<RootObject> rootObjects = new ArrayList<>();
        documents.forEach(new Consumer<Document>() {
            public void accept(Document d) {
                try {
                    rootObjects.add(new RootObject(buildFromDocument(d), d.getLong("_id")));
                }
                catch(Exception e) {
                    e.printStackTrace();
                    throw new IllegalStateException(e);
                }
            }
        });

        return rootObjects;
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


        List<List<String>> collisionAreasFromDB = d.get("collisionAreas", List.class);
        Rectangle[] collisionAreas = new Rectangle[collisionAreasFromDB.size()];
        for(int i = 0 ; i < collisionAreas.length; i++) {
            List<String> rectangleValsFromDB = collisionAreasFromDB.get(i);
            List<BigDecimal> bds = rectangleValsFromDB.stream().map(s -> new BigDecimal(s)).collect(Collectors.toList());
            collisionAreas[i] = CommonUtils.convertFromProportionOfScreenSize(bds.get(0), bds.get(1), bds.get(2), bds.get(3));
        }
        gameObject.setCollisionAreas(collisionAreas);

        return gameObject;
    }

    @Override
    public void save(MouseMovementHandler controller) {

        Map<Saveable, DBupdateType> updatesMap = controller.getUpdates();
        Map<String,List<BsonInt64>> toDeleteMap = new HashMap();
        Set<Saveable> toUpsert = new HashSet();

        for(Map.Entry<Saveable,DBupdateType> entry : updatesMap.entrySet()) {

            Saveable saveable = entry.getKey();
            DBupdateType dBupdateType = entry.getValue();
            if(dBupdateType == DBupdateType.DELETE) {

                List<BsonInt64> l = toDeleteMap.get(saveable.getCollectionName());
                if (l == null) {
                    l = new ArrayList<>();
                    toDeleteMap.put(saveable.getCollectionName(), l);
                }
                l.add(new BsonInt64(saveable.getId()));
            }
            else if(dBupdateType == DBupdateType.ADD || dBupdateType == DBupdateType.UPDATE) {
                toUpsert.add(saveable);
            }
            else {
                throw new IllegalArgumentException("Unsupported update type: " + dBupdateType);
            }
        }

        UpdateOptions uo = new UpdateOptions();
        uo.upsert(true);
        for(Saveable saveable : toUpsert) {

            MongoCollection<Document> collection = getCollection(saveable.getCollectionName());
            if(saveable instanceof RootObject) {

                GameObject go = ((RootObject) saveable).getGameObject();
                if (go == null) {
                    //Better to catch this here rather than save a corrupt config
                    throw new IllegalArgumentException("Cannot save a null game object");
                }

                List<String> gameObjectArea = getConvertedRectangleValsInSaveableFormat(go.getImageArea());

                Rectangle[] collisionAreas = go.getCollisionAreas();
                List<List<String>> collisionAreasToSave = new ArrayList<>();
                for(Rectangle collisionArea : collisionAreas) {
                    collisionAreasToSave.add(getConvertedRectangleValsInSaveableFormat(collisionArea));
                }


                Document documentToSave = new Document();
                //The type of game object is represented as the full name of the underlying class so as it can be instantiated
                //using reflection
                documentToSave.put("class", go.getClass().getName());
                documentToSave.put("_id", saveable.getId());
                documentToSave.put("attributes", go.getAttributes());
                documentToSave.put("rectangle", gameObjectArea);
                documentToSave.put("collisionAreas", collisionAreasToSave);

                //"Upsert"
                collection.replaceOne(Filters.eq("_id", saveable.getId()), documentToSave, ReplaceOptions.createReplaceOptions(uo));
            }

        }

        for(String collectionName : toDeleteMap.keySet()) {
            List<BsonInt64> list = toDeleteMap.get(collectionName);
            BsonInt64[] l = new BsonInt64[list.size()];
            MongoCollection<Document> collection = getCollection(collectionName);
            BsonDocument query = new BsonDocument();
            BsonDocument in = new BsonDocument();
            in.append("$in", new BsonArray(list));
            query.append("_id", in);
            collection.deleteMany(query).getDeletedCount();
        }
    }



    @Override
    public void shutdown() {

    }

    //Pixel values such as x, y, width and height are converted to be proportions of the scrren size first
    //before being saved e.g. if the screen size width is 1500 and the value is 300 then the value will be
    //saved as 0.2. This way the editor will work on any screen size.
    private List<String> getConvertedRectangleValsInSaveableFormat(Rectangle rectangle) {
        BigDecimal[] convertedVals = CommonUtils.convertToProportionOfScreenSize(rectangle);
        //The top-left and bottom-right coordinates of the rectangle are saved
        convertedVals[2] = convertedVals[2].add(convertedVals[0]);
        convertedVals[3] = convertedVals[3].add(convertedVals[1]);

        //Can't find a codec for BigDecimal so convert the large numbers to strings
        return Arrays.stream(convertedVals).map(bd -> {
            return bd.toPlainString();
        }).collect(Collectors.toList());
    }

    private MongoCollection<Document> getCollection(String name) {



        if(!collectionMap.containsKey(name)) {
            MongoIterable<String> collections = database.listCollectionNames();
            String collectionNameFromDB;
            boolean found = false;
            Iterator<String> iter = collections.iterator();
            while (iter.hasNext() && (collectionNameFromDB = iter.next()) != null && !found) {
                found = collectionNameFromDB.equals(name);
            }

            if (!found) {
                database.createCollection(name);
            }
            MongoCollection<Document> collection = database.getCollection(name);
            collectionMap.put(name, collection);
            return collection;
        }
        else {
            return collectionMap.get(name);
        }
    }


}
