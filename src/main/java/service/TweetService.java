package service;

import dao.TweetsDAO;
import model.TweetInfo;
import model.UserInfo;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Felipe on 3/13/16.
 */
public class TweetService implements ITweetService {

    private static final String _dbBaseName = "tweetsInfo_";
    private static AccessToken _token;
    private static TweetsDAO _tweetsDAO;
    private static Twitter _twitter;
    private String _accessToken;
    private String _accessTokenSecret;
    private String _consumerKey;
    private String _consumerSecret;
    private List<TweetInfo> _tweetInfoList;
    private String _username;

    public TweetService(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String username)
    {
        _accessToken = accessToken;
        _accessTokenSecret = accessTokenSecret;
        _consumerKey = consumerKey;
        _consumerSecret = consumerSecret;
        _username = username;
        _tweetsDAO = TweetsDAO.getInstance(_dbBaseName + _username);
        _tweetInfoList = new ArrayList<TweetInfo>();
        authentication();
    }

    private void authentication()
    {
        System.setProperty("twitter4j.oauth.consumerKey", _consumerKey);
        System.setProperty("twitter4j.oauth.consumerSecret", _consumerSecret);
        System.setProperty("twitter4j.oauth.accessToken", _accessToken);
        System.setProperty("twitter4j.oauth.accessTokenSecret", _accessTokenSecret);
        _token = new AccessToken(_accessToken, _accessTokenSecret);
        _twitter = new TwitterFactory().getInstance();
        try
        {
            _twitter.setOAuthConsumer(_consumerKey, _consumerSecret);
            _twitter.setOAuthAccessToken(_token);
        } catch (Exception e)
        {

        }
    }

    public void processUserTimeLine()
    {
        int pageCounter = 1;
        int pageLimit = 200;
        do
        {
            try
            {
                User user = _twitter.showUser(_username);
                UserInfo userInfo = new UserInfo(user.getCreatedAt(), user.getScreenName(), String.valueOf(user.getId()),
                        user.getFollowersCount(), user.getStatusesCount(), user.getLocation());
                ResponseList<Status> userTimeLine = _twitter.getUserTimeline(
                        userInfo.getUserName(), new Paging(pageCounter, pageLimit));
                if (userTimeLine.size() > 0)
                {
                    processTweets(userTimeLine, userInfo);
                    pageCounter++;
                }
            } catch (TwitterException e)
            {
                e.printStackTrace();
            }
        } while (pageCounter < 17);
        _tweetsDAO.save(_tweetInfoList);
    }

    private void processTweets(ResponseList<Status> tweets, UserInfo userInfo)
    {
        for (Status status : tweets)
        {
            TweetInfo tweetInfo = new TweetInfo(userInfo, status.getCreatedAt(), String.valueOf(status.getId()),
                    status.getInReplyToStatusId(), status.getInReplyToUserId(), status.getRetweetCount(), status.getFavoriteCount());
            _tweetInfoList.add(tweetInfo);
        }
    }

}
