package ufrn.imd.engsoft.service.tweets;

import twitter4j.*;
import twitter4j.auth.AccessToken;
import ufrn.imd.engsoft.dao.ITweetsDAO;
import ufrn.imd.engsoft.dao.TweetsDAO;
import ufrn.imd.engsoft.model.TweetInfo;
import ufrn.imd.engsoft.model.UserInfo;
import ufrn.imd.engsoft.service.helpers.CitiesReader;
import ufrn.imd.engsoft.service.helpers.TwitterKeysReader;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by Felipe on 3/13/16.
 */
@Path("/smartcity")
public class TweetService implements ITweetService
{
    private static final String _dbBaseName = "tweets_";
    private static ITweetsDAO _tweetsDAO;
    private static Twitter _twitter;
    private static AccessToken _token;
    private List<TweetInfo> _tweetInfoList;
    private Map<Long, LocalDateTime> _timeMap;
    private int _idCounter;
    private long [] _statusesId =  new long[100];
    private Set<Long> _repliedStatusIds = new HashSet<>();
    private UserInfo _userInfo;

    public TweetService()
    {
        authentication();
        _idCounter = 0;
        _statusesId =  new long[100];
        _repliedStatusIds = new HashSet<>();
    }

    private void authentication()
    {
        Properties properties = TwitterKeysReader.getTwitterKeys();

        System.setProperty("twitter4j.oauth.consumerKey", (String) properties.get("consumerKey"));
        System.setProperty("twitter4j.oauth.consumerSecret", (String) properties.get("consumerSecret"));
        System.setProperty("twitter4j.oauth.accessToken", (String) properties.get("accessToken"));
        System.setProperty("twitter4j.oauth.accessTokenSecret", (String) properties.get("accessTokenSecret"));
        _token = new AccessToken((String) properties.get("accessToken"), (String) properties.get("accessTokenSecret"));
        _twitter = new TwitterFactory().getInstance();
        try
        {
            _twitter.setOAuthConsumer((String) properties.get("consumerKey"), (String) properties.get("consumerSecret"));
            _twitter.setOAuthAccessToken(_token);
        }
        catch (Exception ignored)
        {
        }
    }

    @POST
    @Path("/tweets")
    public Response processTweets()
    {
        List<String> cities = new ArrayList<>(CitiesReader.getCities().values());

        for (int i = 0; i < cities.size(); i++)
        {
            String city = cities.get(i);
            try
            {
                processUserTimelines(city);
            }
            catch (TwitterException e)
            {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        _tweetsDAO.closeMongo();
        return Response.status(Response.Status.OK).build();
    }

    private void processUserTimelines(String username) throws TwitterException
    {
        _tweetsDAO = TweetsDAO.getInstance(_dbBaseName + username, true);
        _tweetInfoList = new ArrayList<>();
        _timeMap = new HashMap<>();

        int pageCounter = 1;
        int pageLimit = 200;

        User user = _twitter.showUser(username);

        if (user != null)
        {
            _userInfo = new UserInfo(user.getCreatedAt(), user.getScreenName(), user.getId(),
                    user.getFollowersCount(), user.getStatusesCount(), user.getLocation());
            _tweetsDAO.saveUserInfo(_userInfo);
        }

        ResponseList<Status> userTimeLine = null;
        RateLimitStatus rateLimitStatus = _twitter.getRateLimitStatus().get("/statuses/user_timeline");
        int remaining = rateLimitStatus.getRemaining();
        do
        {
            if (remaining == 0)
            {
                try
                {
                    Thread.sleep(900000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                userTimeLine = _twitter.getUserTimeline(
                        username, new Paging(pageCounter, pageLimit));
                if (userTimeLine.size() > 0)
                {
                    processUserTweets(userTimeLine);
                    pageCounter++;
                }
            }
            remaining--;
        } while (pageCounter != 17 && !userTimeLine.isEmpty() && userTimeLine != null);

        _tweetsDAO.saveTweetInfos(_tweetInfoList);
        searchMentions(username);
    }

    private void processUserTweets(ResponseList<Status> tweets)
    {
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
                if (!_repliedStatusIds.contains(inReplyToStatusId))
                {
                    _statusesId[_idCounter++] = inReplyToStatusId;
                    _repliedStatusIds.add(inReplyToStatusId);
                }
            }

            String date = localDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            TweetInfo tweetInfo = new TweetInfo(statusId, false, date, inReplyToStatusId,
                    status.getInReplyToUserId(), responseTime, status.getRetweetCount(), status.getFavoriteCount());
            _tweetInfoList.add(tweetInfo);

            if (_idCounter == 100)
            {
                processResponseTime(_statusesId);
                _idCounter = 0;
                _statusesId = new long[100];
            }
        }
    }

    private void searchMentions(String username) throws TwitterException {
        Query query = new Query("@" + username);
        long maxId = _tweetsDAO.getMaxId().getTweetId();

        query.setMaxId(maxId);
        query.setCount(100);
        Boolean hasNext = false;

        List<TweetInfo> tweets = new ArrayList<>();
        RateLimitStatus rateLimitStatus = _twitter.getRateLimitStatus().get("/search/tweets");
        int remaining = rateLimitStatus.getRemaining();
        do
        {
            try
            {
                if(remaining == 0)
                {
                    try
                    {
                        Thread.sleep(900000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    QueryResult result = _twitter.search(query);
                    if (!result.getTweets().isEmpty())
                    {
                        tweets.addAll(mapStatusToTweetInfoList(result.getTweets()));
                        TweetInfo lastTweet = tweets.get(tweets.size() - 1);
                        if (query.getMaxId() != lastTweet.getTweetId() - 1)
                        {
                            query.setMaxId(lastTweet.getTweetId() - 1);
                            hasNext = true;
                        }
                        else
                        {
                            hasNext = false;
                        }
                    }
                    else
                    {
                        hasNext = false;
                    }
                }
                remaining--;
            }
            catch (TwitterException e)
            {
                e.printStackTrace();
            }
        } while (hasNext);

        _tweetsDAO.saveTweetInfos(tweets);
    }

    private void processResponseTime(long[] statusesId)
    {
        try
        {
            ResponseList<Status> responseList = _twitter.lookup(statusesId);
            for (Status status : responseList)
            {
                _tweetInfoList.stream().filter(tweetInfo ->
                        tweetInfo.getInReplyToStatusId() == status.getId())
                        .forEach(tweetInfo ->
                {
                    LocalDateTime fromLocalDateTime = status.getCreatedAt().
                            toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalDateTime toLocalDateTime = _timeMap.get(tweetInfo.getTweetId());
                    _timeMap.remove(tweetInfo.getTweetId());
                    long minutes = fromLocalDateTime.until(toLocalDateTime, ChronoUnit.MINUTES);
                    tweetInfo.setResponseTime(minutes);
                });
            }
        }
        catch (TwitterException e)
        {
            e.printStackTrace();
        }
    }

    private List<TweetInfo> mapStatusToTweetInfoList(List<Status> statusList)
    {
        List<TweetInfo> result = new ArrayList<>();
        for (Status status : statusList)
        {
            LocalDate date = status.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            result.add(new TweetInfo(status.getId(), true, date.toString(), status.getInReplyToStatusId(),
                    status.getInReplyToUserId(), (long) 0, status.getRetweetCount(), status.getFavoriteCount()));
        }
        return result;
    }
}