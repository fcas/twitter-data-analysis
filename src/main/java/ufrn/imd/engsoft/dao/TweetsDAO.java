package ufrn.imd.engsoft.dao;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.jongo.Jongo;
import org.jongo.MongoCursor;
import ufrn.imd.engsoft.model.TweetInfo;
import ufrn.imd.engsoft.model.TweetStream;
import ufrn.imd.engsoft.model.UserInfo;

import java.util.List;

/**
 * Created by Felipe on 10/16/15.
 */
public class TweetsDAO
{
    private static TweetsDAO _instance;
    private static boolean _dropCollection;
    private static String _collectionName;
    private DB _database;
    private Jongo _jongo;
    private MongoClient _mongoClient;

    private TweetsDAO(String collectionName)
    {
        _mongoClient = new MongoClient();
        _database = _mongoClient.getDB("tweets_db");
        _jongo = new Jongo(_database);
        _collectionName = collectionName;

        if(_dropCollection)
        {
            dropCollection();
        }
    }

    private static synchronized void createInstance (String collectionName)
    {
        if (_instance == null)
        {
            _instance = new TweetsDAO(collectionName);
        }
    }

    public static TweetsDAO getInstance(String collectionName, boolean dropCollection)
    {
        if(_instance == null)
        {
            _dropCollection = dropCollection;
            createInstance (collectionName);
        }
        return _instance;
    }

    public void saveTweetInfos(List<TweetInfo> tweetInfoList)
    {
        for (TweetInfo tweetInfo : tweetInfoList)
        {
            _jongo.getCollection(_collectionName).save(tweetInfo);
        }
    }

    public void saveTweetStreams(Document tweetStream)
    {
        _jongo.getCollection(_collectionName).insert(tweetStream);
    }

    public void saveUserInfo(UserInfo userInfo)
    {
        _jongo.getCollection(_collectionName).save(userInfo);
    }

    public MongoCursor<TweetInfo> getOrderedNumericField(String fieldName, boolean isMention)
    {
        return _jongo.getCollection(_collectionName).
                find("{$and: [" +
                             "{_userName: {$exists: false}}, " +
                             "{_isMention: {$eq:" + isMention + "}}" +
                             "]}").
                projection("{" + fieldName + ": 1, _id : 0}").
                sort("{" + fieldName + ": 1}").
                as(TweetInfo.class);
    }

    public UserInfo getUserInfo()
    {
        return _jongo.getCollection(_collectionName).
                findOne("{_userName: {$exists: true}}").
                as(UserInfo.class);
    }

    public TweetInfo getMaxId()
    {
        return _jongo.getCollection(_collectionName).
                findOne().orderBy("{_id: -1}").
                as(TweetInfo.class);
    }

    public void dropCollection()
    {
        _jongo.getCollection(_collectionName).drop();
    }

    public void closeMongo()
    {
        _mongoClient.close();
    }
}
