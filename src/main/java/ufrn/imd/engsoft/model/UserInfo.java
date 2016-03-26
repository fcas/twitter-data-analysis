package ufrn.imd.engsoft.model;

import java.util.Date;

/**
 * Created by Felipe on 3/1/16.
 */
public class UserInfo
{
    private Date _userCreatedAt;
    private String _userName;
    private long _id;
    private long _followersCount;
    private long _statusesCount;
    private String _location;

    public UserInfo(Date userCreatedAt, String userName, long id, long followersCount, long statusesCount, String location)
    {
        _userCreatedAt = userCreatedAt;
        _userName = userName;
        _id = id;
        _followersCount = followersCount;
        _statusesCount = statusesCount;
        _location = location;
    }

    public String get_userName()
    {
        return _userName;
    }

    public void set_userName(String userName)
    {
        _userName = userName;
    }

    public long get_id()
    {
        return _id;
    }

    public void set_id(long userId)
    {
        _id = userId;
    }

    public long get_followersCount()
    {
        return _followersCount;
    }

    public void set_followersCount(long followersCount)
    {
        _followersCount = followersCount;
    }

    public long get_statusesCount()
    {
        return _statusesCount;
    }

    public void set_statusesCount(long statusesCount)
    {
        _statusesCount = statusesCount;
    }

    public String get_location()
    {
        return _location;
    }

    public void set_location(String location)
    {
        _location = location;
    }

    public Date get_userCreatedAt()
    {
        return _userCreatedAt;
    }

    public void set_userCreatedAt(Date userCreatedAt)
    {
        _userCreatedAt = userCreatedAt;
    }
}
