package ufrn.imd.engsoft.service;

import ufrn.imd.engsoft.dao.TweetsDAO;
import ufrn.imd.engsoft.model.TweetInfo;
import ufrn.imd.engsoft.model.UserInfo;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Felipe on 3/13/16.
 */
@Path("/smartcity")
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

    public TweetService()
    {
        setTwitterKeys();
        _tweetInfoList = new ArrayList<TweetInfo>();
        authentication();
    }

    private void setTwitterKeys()
    {
        Properties prop = new Properties();
        InputStream input = null;

        try
        {
            String filename = "config.properties";
            input = getClass().getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                System.out.println("Sorry, unable to find " + filename);
                return;
            }

            prop.load(input);

            _accessToken = prop.getProperty("accessToken");
            _accessTokenSecret = prop.getProperty("accessTokenSecret");
            _consumerKey = prop.getProperty("consumerKey");
            _consumerSecret = prop.getProperty("consumerSecret");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
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

    @GET
    @Path("/metrics/{username}")
    @Produces("application/json")
    public Response processUserTimeLine(@PathParam("username") String username)
    {
        _tweetsDAO = TweetsDAO.getInstance(_dbBaseName + username);

        int pageCounter = 1;
        int pageLimit = 200;
        do
        {
            try
            {
                User user = _twitter.showUser(username);
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
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            }
        } while (pageCounter != 17);
        _tweetsDAO.save(_tweetInfoList);
        return Response.status(Response.Status.OK).build();
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