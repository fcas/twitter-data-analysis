package ufrn.imd.engsoft.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Felipe on 3/1/16.
 */
public class UserInfo implements Serializable
{
    private Date _userCreatedAt;
    private String _userName;
    private long _id;
    private long _followersCount;
    private long _statusesCount;
    private String _location;

    public UserInfo() {}

    public UserInfo(Date userCreatedAt, String userName, long id, long followersCount, long statusesCount, String location)
    {
        _userCreatedAt = userCreatedAt;
        _userName = userName;
        _id = id;
        _followersCount = followersCount;
        _statusesCount = statusesCount;
        _location = location;
    }

    public String getUsername()
    {
        return _userName;
    }

    public void setUsername(String userName)
    {
        _userName = userName;
    }

    public long getId()
    {
        return _id;
    }

    public void setId(long userId)
    {
        _id = userId;
    }

    public long getFollowersCount()
    {
        return _followersCount;
    }

    public void setFollowersCount(long followersCount)
    {
        _followersCount = followersCount;
    }

    public long getStatusesCount()
    {
        return _statusesCount;
    }

    public void setStatusesCount(long statusesCount)
    {
        _statusesCount = statusesCount;
    }

    public String getLocation()
    {
        return _location;
    }

    public void setLocation(String location)
    {
        _location = location;
    }

    public Date getUserCreatedAt()
    {
        return _userCreatedAt;
    }

    public void setUserCreatedAt(Date userCreatedAt)
    {
        _userCreatedAt = userCreatedAt;
    }
}
