package ufrn.imd.engsoft.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Felipe on 3/1/16.
 */
@XmlRootElement(name="TweetInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class TweetInfo implements Serializable
{
    private Long _id;
    private UserInfo _userInfo;
    private Date _tweetCreatedAt;
    private Long _inReplyToStatusId;
    private Long _inReplyToUserId;
    private Long _retweets;
    private Long _favorites;

    public TweetInfo() {}

    public TweetInfo(UserInfo userInfo, Date tweetCreatedAt, long tweetId, long inReplyToStatusId, long inReplyToUserId, long retweets, long favorites)
    {
        _userInfo = userInfo;
        _tweetCreatedAt = tweetCreatedAt;
        _id = tweetId;
        _inReplyToStatusId = inReplyToStatusId;
        _inReplyToUserId = inReplyToUserId;
        _retweets = retweets;
        _favorites = favorites;
    }

    public UserInfo getUserInfo()
    {
        return _userInfo;
    }

    public void setUserInfo(UserInfo userInfo)
    {
        _userInfo = userInfo;
    }

    public Date getTweetCreatedAt()
    {
        return _tweetCreatedAt;
    }

    public void setTweetCreatedAt(Date tweetCreatedAt)
    {
        _tweetCreatedAt = tweetCreatedAt;
    }

    public Long getTweetId()
    {
        return _id;
    }

    public void setTweetId(long tweetId)
    {
        _id = tweetId;
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
