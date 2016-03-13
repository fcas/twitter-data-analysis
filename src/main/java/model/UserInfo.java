package model;


import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Date;

/**
 * Created by Felipe on 3/1/16.
 */
public class UserInfo {

    private Date userCreatedAt;
    private String userName;
    private String userId;
    private long followersCount;
    private long statusesCount;
    private String location;

    public UserInfo(Date userCreatedAt, String userName, String userId, long followersCount, long statusesCount, String location) {
        this.userCreatedAt = userCreatedAt;
        this.userName = userName;
        this.userId = userId;
        this.followersCount = followersCount;
        this.statusesCount = statusesCount;
        this.location = location;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(long followersCount) {
        this.followersCount = followersCount;
    }

    public long getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(long statusesCount) {
        this.statusesCount = statusesCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getUserCreatedAt() {
        return userCreatedAt;
    }

    public void setUserCreatedAt(Date userCreatedAt) {
        this.userCreatedAt = userCreatedAt;
    }
}
