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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    private Map<Long, LocalDateTime> _timeMap;

    public TweetService()
    {
        setTwitterKeys();
        authentication();
        _tweetInfoList = new ArrayList<TweetInfo>();
        _timeMap = new HashMap<Long, LocalDateTime>();
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
                }
                catch (IOException e)
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

        if (user != null)
        {
            UserInfo userInfo = new UserInfo(user.getCreatedAt(), user.getScreenName(), user.getId(),
                    user.getFollowersCount(), user.getStatusesCount(), user.getLocation());
            _tweetsDAO.saveUserInfo(userInfo);
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
            }
            catch (TwitterException e)
            {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            }
        } while (pageCounter != 17);

        _tweetsDAO.saveTweetInfos(_tweetInfoList);
        searchMentions(username);

        return Response.status(Response.Status.OK).build();
    }

    private void processTweets(ResponseList<Status> tweets)
    {
        int idCounter = 0;
        long [] statusesId =  new long[100];
        Set<Long> repliedStatusIds = new HashSet<Long>();

        for (Status status : tweets)
        {
            LocalDateTime localDateTime = status.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            long statusId = status.getId();
            long inReplyToStatusId = status.getInReplyToStatusId();
            long responseTime = (long) 0;

            if (inReplyToStatusId > 0)
            {
                _timeMap.put(statusId, localDateTime);
                // A statusId maybe has more than one reply
                if (!repliedStatusIds.contains(inReplyToStatusId))
                {
                    statusesId[idCounter++] = inReplyToStatusId;
                    repliedStatusIds.add(inReplyToStatusId);
                }
            }

            String date = localDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            TweetInfo tweetInfo = new TweetInfo(statusId, false, date, inReplyToStatusId,
                    status.getInReplyToUserId(), responseTime, status.getRetweetCount(), status.getFavoriteCount());
            _tweetInfoList.add(tweetInfo);

            if (idCounter == 100)
            {
                processResponseTime(statusesId);
                idCounter = 0;
                statusesId = new long[100];
            }
        }
    }

    private void searchMentions(String username)
    {
        Query query = new Query("@" + username);
        long maxId = _tweetsDAO.getMaxId().getTweetId();

        query.setMaxId(maxId);
        query.setCount(100);
        Boolean hasNext = true;

        List<TweetInfo> tweets = new ArrayList<TweetInfo>();
        do
        {
            try
            {
                QueryResult result = _twitter.search(query);
                tweets.addAll(mapStatusToTweetInfoList(result.getTweets()));
                TweetInfo lastTweet = tweets.get(tweets.size() - 1);
                if (query.getMaxId() != lastTweet.getTweetId() - 1)
                {
                    query.setMaxId(lastTweet.getTweetId() - 1);
                }
                else
                {
                    hasNext = false;
                }
            }
            catch (TwitterException e)
            {
                e.printStackTrace();
            }
        } while (hasNext);

        _tweetsDAO.saveTweetInfos(tweets);
        _tweetsDAO.closeMongo();
    }

    private void processResponseTime(long[] statusesId)
    {
        try
        {
            ResponseList<Status> responseList = _twitter.lookup(statusesId);
            for (Status status : responseList)
            {
                for (TweetInfo tweetInfo : _tweetInfoList)
                {
                    if (tweetInfo.getInReplyToStatusId() == status.getId())
                    {
                        LocalDateTime fromLocalDateTime = status.getCreatedAt().
                                toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        LocalDateTime toLocalDateTime = _timeMap.get(tweetInfo.getTweetId());
                        _timeMap.remove(tweetInfo.getTweetId());
                        long minutes = fromLocalDateTime.until(toLocalDateTime, ChronoUnit.MINUTES);
                        tweetInfo.setResponseTime(minutes);
                    }
                }
            }
        }
        catch (TwitterException e)
        {
            e.printStackTrace();
        }
    }

    private List<TweetInfo> mapStatusToTweetInfoList(List<Status> statusList)
    {
        List<TweetInfo> result = new ArrayList<TweetInfo>();
        for (Status status : statusList)
        {
            LocalDate date = status.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            result.add(new TweetInfo(status.getId(), true, date.toString(), status.getInReplyToStatusId(),
                    status.getInReplyToUserId(), (long) 0, status.getRetweetCount(), status.getFavoriteCount()));
        }
        return result;
    }
}