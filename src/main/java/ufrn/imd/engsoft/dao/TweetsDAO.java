package ufrn.imd.engsoft.dao;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import ufrn.imd.engsoft.model.TweetInfo;
import org.jongo.Jongo;

import java.util.List;

/**
 * Created by Felipe on 10/16/15.
 */
public class TweetsDAO {

    private static TweetsDAO _instance;
    private String _collectionName;
    private DB _database;
    private Jongo _jongo;
    private MongoClient _mongoClient;

    private TweetsDAO(String _collectionName) {
        _mongoClient = new MongoClient();
        _database = _mongoClient.getDB("tweets-db");
        _jongo = new Jongo(_database);
        this._collectionName = _collectionName;
        dropCollection();
    }

    private static synchronized void createInstance (String collectionName)
    {
        if (_instance == null)
        {
            _instance = new TweetsDAO(collectionName);
        }
    }

    public static TweetsDAO getInstance(String collectionName){

        if(_instance == null)
        {
            createInstance (collectionName);

        }
        return _instance;

    }

    public void save(List<TweetInfo> tweetInfoList){
        _jongo.getCollection(_collectionName).save(tweetInfoList);
    }

    public void dropCollection(){
        _jongo.getCollection(_collectionName).drop();
    }

    public void closeMongo(){
        _mongoClient.close();
    }
}
