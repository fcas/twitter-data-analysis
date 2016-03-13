package model;

import java.util.Date;

/**
 * Created by Felipe on 3/1/16.
 */
public class TweetInfo {

    private String tweetId;
    private UserInfo userInfo;
    private Date tweetCreatedAt;
    private long inReplyToStatusId;
    private long inReplyToUserId;
    private long retweetCount;
    private long favoriteCount;

    public TweetInfo(UserInfo userInfo, Date tweetCreatedAt, String tweetId, long inReplyToStatusId, long inReplyToUserId, long retweetCount, long favoriteCount) {
        this.userInfo = userInfo;
        this.tweetCreatedAt = tweetCreatedAt;
        this.tweetId = tweetId;
        this.inReplyToStatusId = inReplyToStatusId;
        this.inReplyToUserId = inReplyToUserId;
        this.retweetCount = retweetCount;
        this.favoriteCount = favoriteCount;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public Date getTweetCreatedAt() {
        return tweetCreatedAt;
    }

    public void setTweetCreatedAt(Date tweetCreatedAt) {
        this.tweetCreatedAt = tweetCreatedAt;
    }

    public String getTweetId() {
        return tweetId;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public long getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(long inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    public long getInReplyToUserId() {
        return inReplyToUserId;
    }

    public void setInReplyToUserId(long inReplyToUserId) {
        this.inReplyToUserId = inReplyToUserId;
    }

    public long getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(long retweetCount) {
        this.retweetCount = retweetCount;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
}
