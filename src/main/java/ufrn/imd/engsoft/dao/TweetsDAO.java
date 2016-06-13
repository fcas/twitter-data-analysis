package ufrn.imd.engsoft.dao;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.MongoCursor;
import ufrn.imd.engsoft.model.Sentiment;
import ufrn.imd.engsoft.model.TweetInfo;
import ufrn.imd.engsoft.model.UserInfo;

import java.util.List;

/**
 * Created by Felipe on 10/16/15.
 */
public class TweetsDAO implements ITweetsDAO
{
    private static ITweetsDAO _instance;
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

    public static ITweetsDAO getInstance(String collectionName, boolean dropCollection)
    {
        _dropCollection = dropCollection;
        _instance = new TweetsDAO(collectionName);
        return _instance;
    }

    public void saveTweetInfos(List<TweetInfo> tweetInfoList)
    {
        for (TweetInfo tweetInfo : tweetInfoList)
        {
            _jongo.getCollection(_collectionName).save(tweetInfo);
        }
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

    public MongoCursor<Sentiment> getSentiments(String collectionNameSufix, String sentimentPolarity)
    {
        return _jongo.getCollection(_collectionName).
                find("{$and: [" +
                        "{_userMentions: {$in:['" + collectionNameSufix + "']}}, " +
                        "{_sentiment: {$eq:'" + sentimentPolarity + "'}}" +
                        "]}").
                as(Sentiment.class);
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
