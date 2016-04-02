package ufrn.imd.engsoft.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by Felipe on 3/1/16.
 */
@XmlRootElement(name="TweetInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class TweetInfo implements Serializable
{
    private Long _id;
    private String _tweetCreatedAt;
    private Long _inReplyToStatusId;
    private Long _inReplyToUserId;
    private Long _retweets;
    private Long _favorites;
    private Long _responseTime;
    private boolean _isMention;

    public TweetInfo() {}

    public TweetInfo(long tweetId, boolean isMention, String tweetCreatedAt,  long inReplyToStatusId,
                     long inReplyToUserId, long responseTime, long retweets, long favorites)
    {
        _id = tweetId;
        _tweetCreatedAt = tweetCreatedAt;
        _isMention = isMention;
        _inReplyToStatusId = inReplyToStatusId;
        _inReplyToUserId = inReplyToUserId;
        _responseTime = responseTime;
        _retweets = retweets;
        _favorites = favorites;
    }

    public Long getTweetId()
    {
        return _id;
    }

    public void setTweetId(long tweetId)
    {
        _id = tweetId;
    }

    public String getTweetCreatedAt()
    {
        return _tweetCreatedAt;
    }

    public void setTweetCreatedAt(String tweetCreatedAt)
    {
        _tweetCreatedAt = tweetCreatedAt;
    }

    public boolean isMention() {
        return _isMention;
    }

    public void setIsMention(boolean isMention)
    {
        _isMention = isMention;
    }

    public Long getInReplyToStatusId()
    {
        return _inReplyToStatusId;
    }

    public void setInReplyToStatusId(long inReplyToStatusId)
    {
        _inReplyToStatusId = inReplyToStatusId;
    }

    public Long getInReplyToUserId()
    {
        return _inReplyToUserId;
    }

    public void setInReplyToUserId(long inReplyToUserId)
    {
        _inReplyToUserId = inReplyToUserId;
    }

    public Long getResponseTime()
    {
        return _responseTime;
    }

    public void setResponseTime(Long responseTime)
    {
        _responseTime = responseTime;
    }

    public Long getRetweets()
    {
        return _retweets;
    }

    public void setRetweets(long retweets)
    {
        _retweets = retweets;
    }

    public Long getFavorites()
    {
        return _favorites;
    }

    public void setFavorites(long favorites)
    {
        _favorites = favorites;
    }
}
