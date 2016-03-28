package ufrn.imd.engsoft.service.tweets;

import twitter4j.*;
import twitter4j.auth.AccessToken;
import ufrn.imd.engsoft.dao.TweetsDAO;
import ufrn.imd.engsoft.model.TweetInfo;
import ufrn.imd.engsoft.model.UserInfo;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Felipe on 3/13/16.
 */
@Path("/smartcity")
public class TweetService implements ITweetService
{
    private static final String _configurationFileName = "config.properties";
    private static final String _dbBaseName = "tweets_";
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
            input = getClass().getClassLoader().getResourceAsStream(_configurationFileName);
            if (input == null)
            {
                System.out.println("Sorry, unable to find " + _configurationFileName);
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
        AccessToken _token = new AccessToken(_accessToken, _accessTokenSecret);
        _twitter = new TwitterFactory().getInstance();
        try
        {
            _twitter.setOAuthConsumer(_consumerKey, _consumerSecret);
            _twitter.setOAuthAccessToken(_token);
        }
        catch (IllegalStateException e)
        {

        }
        catch (Exception e)
        {

        }
    }

    @POST
    @Path("/tweets/{username}")
    public Response processUserTimeLine(@PathParam("username") String username)
    {
        _tweetsDAO = TweetsDAO.getInstance(_dbBaseName + username, true);

        int pageCounter = 1;
        int pageLimit = 200;

        User user = null;
        try
        {
            user = _twitter.showUser(username);
        }
        catch (TwitterException e)
        {
            e.printStackTrace();
        }

        UserInfo userInfo = null;
        if (user != null)
        {
            userInfo = new UserInfo(user.getCreatedAt(), user.getScreenName(), user.getId(),
                    user.getFollowersCount(), user.getStatusesCount(), user.getLocation());
        }

        do
        {
            try
            {
                ResponseList<Status> userTimeLine = _twitter.getUserTimeline(
                        username, new Paging(pageCounter, pageLimit));
                if (userTimeLine.size() > 0)
                {
                    processTweets(userTimeLine);
                    pageCounter++;
                }
            } catch (TwitterException e)
            {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            }
        } while (pageCounter != 17);



        _tweetsDAO.saveUserInfo(userInfo);
        _tweetsDAO.saveTweetInfos(_tweetInfoList);
        _tweetsDAO.closeMongo();

        return Response.status(Response.Status.OK).build();
    }

    private void searchMentions(String username){
        /* TO DO */
    }

    private void processTweets(ResponseList<Status> tweets)
    {
        for (Status status : tweets)
        {
            LocalDate date = status.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            TweetInfo tweetInfo = new TweetInfo(date.toString(), status.getId(), status.getInReplyToStatusId(),
                    status.getInReplyToUserId(), status.getRetweetCount(), status.getFavoriteCount());
            _tweetInfoList.add(tweetInfo);
        }
    }
}