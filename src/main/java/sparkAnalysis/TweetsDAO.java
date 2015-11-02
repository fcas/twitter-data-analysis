package sparkAnalysis;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

/**
 * Created by Felipe on 10/16/15.
 */
public class TweetsDAO {

    public MongoClient mongoClient;
    public MongoDatabase database;
    private static TweetsDAO instance;
    private String collectionName;

    private TweetsDAO(String collectionName) {
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("tweets-db");
        this.collectionName = collectionName;
        dropCollection();
    }

    public static TweetsDAO getInstance(String collectionName){

        if(instance == null)
        {
            inicializaInstancia(collectionName);

        }
        return instance;

    }

    private static synchronized void inicializaInstancia(String collectionName)
    {
        if (instance == null)
        {
            instance = new TweetsDAO(collectionName);
        }
    }


    public void insertMany(List<Document> document){
        if(!document.isEmpty()){
            database.getCollection(collectionName).insertMany(document, new SingleResultCallback<Void>() {
                @Override
                public void onResult(Void result, Throwable t) {
                }
            });
        }
    }

    public void dropCollection(){
        database.getCollection(collectionName).drop(new SingleResultCallback<Void>() {
            @Override
            public void onResult(Void result, Throwable throwable) {
            }
        });
    }

    public void closeMongo(){
        mongoClient.close();
    }
}
