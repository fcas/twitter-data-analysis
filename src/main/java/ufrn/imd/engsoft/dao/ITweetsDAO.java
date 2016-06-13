package ufrn.imd.engsoft.dao;

import org.jongo.MongoCursor;
import ufrn.imd.engsoft.model.Sentiment;
import ufrn.imd.engsoft.model.TweetInfo;
import ufrn.imd.engsoft.model.UserInfo;

import java.util.List;

/**
 * Created by Felipe on 20/05/16.
 */
public interface ITweetsDAO
{
    void saveTweetInfos(List<TweetInfo> tweetInfoList);

    void saveUserInfo(UserInfo userInfo);

    void dropCollection();

    void closeMongo();

    MongoCursor<TweetInfo> getOrderedNumericField(String fieldName, boolean isMention);

    MongoCursor<Sentiment> getSentiments(String collectionNameSufix, String sentimentPolarity);

    UserInfo getUserInfo();

    TweetInfo getMaxId();
}
